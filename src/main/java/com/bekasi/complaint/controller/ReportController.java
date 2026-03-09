package com.bekasi.complaint.controller;

import com.bekasi.complaint.dto.request.CreateReportRequest;
import com.bekasi.complaint.dto.response.ApiResponse;
import com.bekasi.complaint.dto.response.ReportResponse;
import com.bekasi.complaint.enums.ReportStatus;
import com.bekasi.complaint.security.UserDetailsImpl;
import com.bekasi.complaint.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // -------------------------------------------------------------------------
    // Helper: cek apakah user punya role ADMIN atau OFFICER
    // -------------------------------------------------------------------------
    private boolean isPrivileged(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_OFFICER"));
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    /**
     * POST /api/reports
     * Hanya USER yang bisa membuat laporan.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @Valid @RequestPart("data") CreateReportRequest request,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ReportResponse report = reportService.createReport(request, image, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report created successfully", report));
    }

    // -------------------------------------------------------------------------
    // READ — semua endpoint GET wajib login
    // -------------------------------------------------------------------------

    /**
     * GET /api/reports
     * - USER      → hanya laporan milik sendiri
     * - ADMIN/OFFICER → semua laporan semua user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ReportResponse> reports = reportService.getReports(
                userDetails.getId(), isPrivileged(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", reports));
    }

    /**
     * GET /api/reports/{id}
     * - USER      → hanya bisa akses laporan milik sendiri (404 jika bukan miliknya)
     * - ADMIN/OFFICER → bisa akses laporan siapapun
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ReportResponse report = reportService.getReportById(
                id, userDetails.getId(), isPrivileged(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Report retrieved successfully", report));
    }

    /**
     * GET /api/reports/status/pending
     * - USER      → laporan PENDING milik sendiri
     * - ADMIN/OFFICER → semua laporan PENDING
     */
    @GetMapping("/status/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getPendingReports(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ReportResponse> reports = reportService.getReportsByStatus(
                ReportStatus.PENDING, userDetails.getId(), isPrivileged(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Pending reports retrieved successfully", reports));
    }

    /**
     * GET /api/reports/status/in-process
     * - USER      → laporan IN_PROCESS milik sendiri
     * - ADMIN/OFFICER → semua laporan IN_PROCESS
     */
    @GetMapping("/status/in-process")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getInProcessReports(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ReportResponse> reports = reportService.getReportsByStatus(
                ReportStatus.IN_PROCESS, userDetails.getId(), isPrivileged(userDetails));
        return ResponseEntity.ok(ApiResponse.success("In-process reports retrieved successfully", reports));
    }

    /**
     * GET /api/reports/status/completed
     * - USER      → laporan COMPLETED milik sendiri
     * - ADMIN/OFFICER → semua laporan COMPLETED
     */
    @GetMapping("/status/completed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getCompletedReports(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ReportResponse> reports = reportService.getReportsByStatus(
                ReportStatus.COMPLETED, userDetails.getId(), isPrivileged(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Completed reports retrieved successfully", reports));
    }

    /**
     * GET /api/reports/status/rejected
     * - USER      → laporan REJECTED milik sendiri
     * - ADMIN/OFFICER → semua laporan REJECTED
     */
    @GetMapping("/status/rejected")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getRejectedReports(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ReportResponse> reports = reportService.getReportsByStatus(
                ReportStatus.REJECTED, userDetails.getId(), isPrivileged(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Rejected reports retrieved successfully", reports));
    }

    // -------------------------------------------------------------------------
    // STATUS TRANSITIONS
    // -------------------------------------------------------------------------

    /**
     * PATCH /api/reports/{id}/approve
     * ADMIN: ubah status PENDING → IN_PROCESS
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> approveReport(@PathVariable Long id) {
        ReportResponse report = reportService.approveReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report approved and is now in process", report));
    }

    /**
     * PATCH /api/reports/{id}/reject
     * ADMIN: ubah status PENDING → REJECTED
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> rejectReport(@PathVariable Long id) {
        ReportResponse report = reportService.rejectReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report has been rejected", report));
    }

    /**
     * PATCH /api/reports/{id}/complete
     * OFFICER: ubah status IN_PROCESS → COMPLETED
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<ApiResponse<ReportResponse>> completeReport(@PathVariable Long id) {
        ReportResponse report = reportService.completeReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report has been marked as completed", report));
    }
}