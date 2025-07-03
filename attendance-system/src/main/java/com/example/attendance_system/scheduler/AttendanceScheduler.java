package com.example.attendance_system.scheduler;

import com.example.attendancesystem.model.Attendance;
import com.example.attendancesystem.model.User;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.UserRepository;
import com.example.attendancesystem.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AttendanceScheduler {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // 1. Notify absent employees daily (run at 6:00 PM)
    @Scheduled(cron = "0 0 18 * * *")
    public void notifyAbsentEmployees() {
        LocalDate today = LocalDate.now();
        List<User> employees = userRepository.findAll()
                .stream()
                .filter(u -> u.getRoles().contains("EMPLOYEE"))
                .collect(Collectors.toList());

        for (User employee : employees) {
            boolean attendedToday = attendanceRepository
                    .findByUserAndDate(employee, today)
                    .stream()
                    .findFirst()
                    .map(Attendance::isPresent)
                    .orElse(false);

            if (!attendedToday) {
                emailService.sendEmail(
                        employee.getEmail(),
                        "Absence Alert: " + today,
                        "Dear " + employee.getUsername() + ",\nYou were marked absent today."
                );
            }
        }
    }

    // 2. Weekly admin attendance summary (run Mondays at 7:00 AM)
    @Scheduled(cron = "0 0 7 * * MON")
    public void sendWeeklyReportToAdmin() {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(7);

        List<Attendance> weekly = attendanceRepository.findAll().stream()
                .filter(a -> !a.getDate().isBefore(weekStart) && !a.getDate().isAfter(now))
                .collect(Collectors.toList());

        StringBuilder report = new StringBuilder("Weekly Attendance Report:\n");
        Map<String, Long> summary = weekly.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getUsername(), Collectors.counting()));

        summary.forEach((user, count) -> report.append(user).append(": ").append(count).append(" days present\n"));

        // Find admin(s)
        userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("ADMIN"))
                .forEach(admin -> emailService.sendEmail(
                        admin.getEmail(),
                        "Weekly Attendance Report",
                        report.toString()
                ));
    }

    // 3. Monthly attendance warning (run on 1st of every month at 8:00 AM)
    @Scheduled(cron = "0 0 8 1 * *")
    public void sendMonthlyWarningToEmployees() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate start = lastMonth.atDay(1);
        LocalDate end = lastMonth.atEndOfMonth();

        List<User> employees = userRepository.findAll()
                .stream()
                .filter(u -> u.getRoles().contains("EMPLOYEE"))
                .collect(Collectors.toList());

        for (User employee : employees) {
            List<Attendance> records = attendanceRepository.findByUser(employee).stream()
                    .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                    .collect(Collectors.toList());
            long presentDays = records.stream().filter(Attendance::isPresent).count();
            long workingDays = end.getDayOfMonth(); // adjust if needed for weekends/holidays

            double percent = workingDays > 0 ? ((double) presentDays / workingDays) * 100 : 100;
            if (percent < 75.0) {
                emailService.sendEmail(
                        employee.getEmail(),
                        "Attendance Warning for " + lastMonth,
                        "Dear " + employee.getUsername() + ",\nYour attendance was " + String.format("%.2f", percent) +
                                "%. Please ensure it improves to avoid action."
                );
            }
        }
    }
}
