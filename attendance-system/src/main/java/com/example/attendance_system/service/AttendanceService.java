package com.example.attendance_system.service;

import com.example.attendancesystem.model.Attendance;
import com.example.attendancesystem.model.User;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public Attendance markAttendance(String username, boolean present) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) throw new RuntimeException("User not found");
        User user = userOptional.get();

        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByUserAndDate(user, today)
                .stream().findFirst();
        if (existing.isPresent()) {
            Attendance att = existing.get();
            att.setPresent(present);
            return attendanceRepository.save(att);
        } else {
            Attendance att = Attendance.builder()
                    .user(user)
                    .date(today)
                    .present(present)
                    .build();
            return attendanceRepository.save(att);
        }
    }

    public List<Attendance> getAttendanceForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return attendanceRepository.findByUser(user);
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }
}