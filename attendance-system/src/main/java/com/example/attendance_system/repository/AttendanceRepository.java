package com.example.attendance_system.repository;

import com.example.attendancesystem.model.Attendance;
import com.example.attendancesystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUser(User user);
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByUserAndDate(User user, LocalDate date);
}
