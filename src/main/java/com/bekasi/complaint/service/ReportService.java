package com.bekasi.complaint.service;

import com.bekasi.complaint.dto.request.CreateReportRequest;
import com.bekasi.complaint.dto.response.ReportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReportService {
    ReportResponse createReport(CreateReportRequest request, MultipartFile image, Long userId);
    List<ReportResponse> getAllReports();
    List<ReportResponse> getPendingReports();
    List<ReportResponse> getInProcessReports();
    List<ReportResponse> getCompletedReports();
    List<ReportResponse> getRejectedReports();
    ReportResponse getReportById(Long id);
    ReportResponse approveReport(Long id);
    ReportResponse rejectReport(Long id);
    ReportResponse completeReport(Long id);
}
