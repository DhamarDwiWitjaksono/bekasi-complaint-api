package com.bekasi.complaint.service.impl;

import com.bekasi.complaint.dto.request.CreateReportRequest;
import com.bekasi.complaint.dto.response.ReportResponse;
import com.bekasi.complaint.entity.Report;
import com.bekasi.complaint.entity.User;
import com.bekasi.complaint.enums.ReportStatus;
import com.bekasi.complaint.exception.BadRequestException;
import com.bekasi.complaint.exception.ResourceNotFoundException;
import com.bekasi.complaint.repository.ReportRepository;
import com.bekasi.complaint.repository.UserRepository;
import com.bekasi.complaint.service.FileStorageService;
import com.bekasi.complaint.service.GeocodingService;
import com.bekasi.complaint.service.ReportService;
import com.bekasi.complaint.util.GeocodingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final GeocodingService geocodingService;

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String BASE_IMAGE_URL = "http://localhost:8080/api/images";

    @Override
    @Transactional
    public ReportResponse createReport(CreateReportRequest request, MultipartFile image, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Validate and store image (rejects if >= 2MB)
        String imagePath = fileStorageService.storeImage(image);

        // Geocode and validate location is inside Bekasi
        GeocodingResult geocodingResult;
        try {
            geocodingResult = geocodingService.reverseGeocode(request.getLatitude(), request.getLongitude());
        } catch (Exception e) {
            // Cleanup stored image if geocoding fails
            fileStorageService.deleteImage(imagePath);
            throw e;
        }

        Report report = Report.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .imagePath(imagePath)
                .latitude(geocodingResult.getLatitude())
                .longitude(geocodingResult.getLongitude())
                .address(geocodingResult.getAddress())
                .googleMapsUrl(geocodingResult.getGoogleMapsUrl())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .user(user)
                .build();

        Report savedReport = reportRepository.save(report);
        log.info("Report created: {} by user: {}", savedReport.getId(), userId);
        return ReportResponse.fromEntity(savedReport, BASE_IMAGE_URL);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(report -> ReportResponse.fromEntity(report, BASE_IMAGE_URL))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING).stream()
                .map(report -> ReportResponse.fromEntity(report, BASE_IMAGE_URL))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getInProcessReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.IN_PROCESS).stream()
                .map(report -> ReportResponse.fromEntity(report, BASE_IMAGE_URL))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getCompletedReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.COMPLETED).stream()
                .map(report -> ReportResponse.fromEntity(report, BASE_IMAGE_URL))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getRejectedReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.REJECTED).stream()
                .map(report -> ReportResponse.fromEntity(report, BASE_IMAGE_URL))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return ReportResponse.fromEntity(report, BASE_IMAGE_URL);
    }

    @Override
    @Transactional
    public ReportResponse approveReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING reports can be approved. Current status: " + report.getStatus());
        }

        report.setStatus(ReportStatus.IN_PROCESS);
        Report updated = reportRepository.save(report);
        log.info("Report {} approved, status changed to IN_PROCESS", id);
        return ReportResponse.fromEntity(updated, BASE_IMAGE_URL);
    }

    @Override
    @Transactional
    public ReportResponse rejectReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING reports can be rejected. Current status: " + report.getStatus());
        }

        report.setStatus(ReportStatus.REJECTED);
        Report updated = reportRepository.save(report);
        log.info("Report {} rejected", id);
        return ReportResponse.fromEntity(updated, BASE_IMAGE_URL);
    }

    @Override
    @Transactional
    public ReportResponse completeReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        if (report.getStatus() != ReportStatus.IN_PROCESS) {
            throw new BadRequestException(
                    "Only IN_PROCESS reports can be marked as completed. Current status: " + report.getStatus());
        }

        report.setStatus(ReportStatus.COMPLETED);
        Report updated = reportRepository.save(report);
        log.info("Report {} completed", id);
        return ReportResponse.fromEntity(updated, BASE_IMAGE_URL);
    }
}
