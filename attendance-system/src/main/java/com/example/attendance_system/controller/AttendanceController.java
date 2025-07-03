package com.example.attendance_system.controller;

import com.example.attendancesystem.model.Attendance;
import com.example.attendancesystem.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    // Employee marks attendance
    @PostMapping("/employee/attendance")
    public Attendance markAttendance(Authentication authentication, @RequestParam boolean present) {
        String username = authentication.getName();
        return attendanceService.markAttendance(username, present);
    }

    // Employee views their attendance
    @GetMapping("/employee/attendance")
    public List<Attendance> getOwnAttendance(Authentication authentication) {
        String username = authentication.getName();
        return attendanceService.getAttendanceForUser(username);
    }

    // Admin views all attendance records
    @GetMapping("/admin/attendance")
    public List<Attendance> getAllAttendance() {
        return attendanceService.getAllAttendance();
    }
}
