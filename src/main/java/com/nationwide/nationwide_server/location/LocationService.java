package com.nationwide.nationwide_server.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {
    private final RestClient kakaoLocationRestClient;
    private final LocationProperties locationProperties;

    public Optional<LocationCoordinate> geocodeRoadAddress(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        if (!locationProperties.isConfigured()) {
            log.warn("Kakao location REST API key is not configured. Skipping geocoding.");
            return Optional.empty();
        }

        try {
            KakaoAddressSearchResponse response = kakaoLocationRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", address)
                            .build())
                    .header("Authorization", "KakaoAK " + locationProperties.getRestApiKey())
                    .retrieve()
                    .body(KakaoAddressSearchResponse.class);

            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                return Optional.empty();
            }

            KakaoAddressDocument document = response.documents().getFirst();
            Double latitude = parseDouble(document.y());
            Double longitude = parseDouble(document.x());

            if (latitude == null || longitude == null) {
                return Optional.empty();
            }

            String normalizedAddress = document.roadAddress() != null
                    && document.roadAddress().addressName() != null
                    && !document.roadAddress().addressName().isBlank()
                    ? document.roadAddress().addressName()
                    : document.addressName();

            return Optional.of(new LocationCoordinate(latitude, longitude, normalizedAddress));
        } catch (Exception exception) {
            log.error("Failed to geocode address: {}", address, exception);
            return Optional.empty();
        }
    }

    public Double calculateDistanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }

        double earthRadiusKm = 6371.0;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(earthRadiusKm * c * 10.0) / 10.0;
    }

    private Double parseDouble(String value) {
        try {
            return value == null || value.isBlank() ? null : Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record KakaoAddressSearchResponse(List<KakaoAddressDocument> documents) {
    }

    private record KakaoAddressDocument(
            @JsonProperty("address_name")
            String addressName,
            String x,
            String y,
            @JsonProperty("road_address")
            KakaoRoadAddress roadAddress
    ) {
    }

    private record KakaoRoadAddress(
            @JsonProperty("address_name")
            String addressName
    ) {
    }
}
