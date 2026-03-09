package com.bekasi.complaint.dto.response;

import com.bekasi.complaint.entity.Report;
import com.bekasi.complaint.enums.ReportCategory;
import com.bekasi.complaint.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

    private Long id;
    private String title;
    private ReportCategory category;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String address;
    private String googleMapsUrl;
    private String description;
    private ReportStatus status;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReportResponse fromEntity(Report report, String baseImageUrl) {
        return ReportResponse.builder()
                .id(report.getId())
                .title(report.getTitle())
                .category(report.getCategory())
                .imageUrl(baseImageUrl + "/" + report.getImagePath())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .address(report.getAddress())
                .googleMapsUrl(report.getGoogleMapsUrl())
                .description(report.getDescription())
                .status(report.getStatus())
                .userId(report.getUser().getId())
                .userName(report.getUser().getName())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
