package com.bekasi.complaint.controller;

import com.bekasi.complaint.dto.request.CreateReportRequest;
import com.bekasi.complaint.dto.response.ApiResponse;
import com.bekasi.complaint.dto.response.ReportResponse;
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

    /**
     * POST /api/reports
     * Create a new complaint report. Requires USER role.
     * Accepts multipart/form-data with image upload.
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

    /**
     * GET /api/reports
     * Get all reports (publicly accessible).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getAllReports() {
        List<ReportResponse> reports = reportService.getAllReports();
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", reports));
    }

    /**
     * GET /api/reports/{id}
     * Get a single report by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportById(@PathVariable Long id) {
        ReportResponse report = reportService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success("Report retrieved successfully", report));
    }

    /**
     * GET /api/reports/status/pending
     * Get all pending reports.
     */
    @GetMapping("/status/pending")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getPendingReports() {
        List<ReportResponse> reports = reportService.getPendingReports();
        return ResponseEntity.ok(ApiResponse.success("Pending reports retrieved successfully", reports));
    }

    /**
     * GET /api/reports/status/in-process
     * Get all in-process reports.
     */
    @GetMapping("/status/in-process")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getInProcessReports() {
        List<ReportResponse> reports = reportService.getInProcessReports();
        return ResponseEntity.ok(ApiResponse.success("In-process reports retrieved successfully", reports));
    }

    /**
     * GET /api/reports/status/completed
     * Get all completed reports.
     */
    @GetMapping("/status/completed")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getCompletedReports() {
        List<ReportResponse> reports = reportService.getCompletedReports();
        return ResponseEntity.ok(ApiResponse.success("Completed reports retrieved successfully", reports));
    }

    /**
     * GET /api/reports/status/rejected
     * Get all rejected reports.
     */
    @GetMapping("/status/rejected")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getRejectedReports() {
        List<ReportResponse> reports = reportService.getRejectedReports();
        return ResponseEntity.ok(ApiResponse.success("Rejected reports retrieved successfully", reports));
    }

    /**
     * PATCH /api/reports/{id}/approve
     * Approve a report (set to IN_PROCESS). Requires ADMIN role.
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> approveReport(@PathVariable Long id) {
        ReportResponse report = reportService.approveReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report approved and is now in process", report));
    }

    /**
     * PATCH /api/reports/{id}/reject
     * Reject a report. Requires ADMIN role.
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> rejectReport(@PathVariable Long id) {
        ReportResponse report = reportService.rejectReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report has been rejected", report));
    }

    /**
     * PATCH /api/reports/{id}/complete
     * Mark a report as completed. Requires OFFICER role.
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<ApiResponse<ReportResponse>> completeReport(@PathVariable Long id) {
        ReportResponse report = reportService.completeReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report has been marked as completed", report));
    }
}
