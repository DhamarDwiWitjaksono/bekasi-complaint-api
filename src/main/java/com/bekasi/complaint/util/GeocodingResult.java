package com.bekasi.complaint.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeocodingResult {
    private String address;
    private Double latitude;
    private Double longitude;
    private String googleMapsUrl;
}
