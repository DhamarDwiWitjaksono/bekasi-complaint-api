package com.bekasi.complaint.service;

import com.bekasi.complaint.dto.request.CreateReportRequest;
import com.bekasi.complaint.dto.response.ReportResponse;
import com.bekasi.complaint.enums.ReportStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReportService {
    ReportResponse createReport(CreateReportRequest request, MultipartFile image, Long userId);

    /**
     * Get all reports (ADMIN/OFFICER) or only own reports (USER).
     * @param requesterId  ID user yang melakukan request
     * @param isPrivileged true jika ADMIN atau OFFICER
     */
    List<ReportResponse> getReports(Long requesterId, boolean isPrivileged);

    /**
     * Get reports filtered by status (ADMIN/OFFICER) or only own reports filtered by status (USER).
     */
    List<ReportResponse> getReportsByStatus(ReportStatus status, Long requesterId, boolean isPrivileged);

    ReportResponse getReportById(Long id, Long requesterId, boolean isPrivileged);
    ReportResponse approveReport(Long id);
    ReportResponse rejectReport(Long id);
    ReportResponse completeReport(Long id);
}