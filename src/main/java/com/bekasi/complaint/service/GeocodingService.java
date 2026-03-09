package com.bekasi.complaint.service;

import com.bekasi.complaint.exception.BadRequestException;
import com.bekasi.complaint.exception.LocationOutsideBekasiException;
import com.bekasi.complaint.util.GeocodingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocodingService {

    private final WebClient geocodingWebClient;

    @Value("${app.bekasi.lat.min}")
    private double bekasiLatMin;

    @Value("${app.bekasi.lat.max}")
    private double bekasiLatMax;

    @Value("${app.bekasi.lon.min}")
    private double bekasiLonMin;

    @Value("${app.bekasi.lon.max}")
    private double bekasiLonMax;

    /**
     * Performs reverse geocoding (lat/lon → address) using Nominatim (OpenStreetMap).
     * Validates that the location is inside Bekasi City boundaries.
     */
    public GeocodingResult reverseGeocode(Double latitude, Double longitude) {
        validateCoordinatesInsideBekasi(latitude, longitude);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = geocodingWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reverse")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("format", "json")
                            .queryParam("addressdetails", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.containsKey("error")) {
                throw new BadRequestException("Unable to geocode the provided location. Please check the coordinates.");
            }

            String displayName = (String) response.get("display_name");
            if (displayName == null || displayName.isBlank()) {
                throw new BadRequestException("No address found for the provided coordinates.");
            }

            // Validate address contains Bekasi
            validateAddressInBekasi(displayName);

            String googleMapsUrl = buildGoogleMapsUrl(latitude, longitude);

            return GeocodingResult.builder()
                    .address(displayName)
                    .latitude(latitude)
                    .longitude(longitude)
                    .googleMapsUrl(googleMapsUrl)
                    .build();

        } catch (LocationOutsideBekasiException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Geocoding error: {}", e.getMessage());
            throw new BadRequestException("Geocoding service is currently unavailable. Please try again later.");
        }
    }

    /**
     * Validates coordinates are within Bekasi City bounding box.
     */
    private void validateCoordinatesInsideBekasi(Double latitude, Double longitude) {
        if (latitude < bekasiLatMin || latitude > bekasiLatMax ||
                longitude < bekasiLonMin || longitude > bekasiLonMax) {
            throw new LocationOutsideBekasiException(
                    String.format("The provided location (%.6f, %.6f) is outside Bekasi City boundaries. " +
                            "Reports can only be submitted for locations within Bekasi City.", latitude, longitude));
        }
    }

    /**
     * Validates geocoded address contains Bekasi reference.
     */
    private void validateAddressInBekasi(String address) {
        String lowerAddress = address.toLowerCase();
        if (!lowerAddress.contains("bekasi")) {
            throw new LocationOutsideBekasiException(
                    "The provided location does not appear to be within Bekasi City. " +
                    "Please provide a location within Bekasi City limits.");
        }
    }

    /**
     * Builds a Google Maps URL for the given coordinates.
     */
    private String buildGoogleMapsUrl(Double latitude, Double longitude) {
        return String.format("https://www.google.com/maps?q=%.6f,%.6f", latitude, longitude);
    }
}
