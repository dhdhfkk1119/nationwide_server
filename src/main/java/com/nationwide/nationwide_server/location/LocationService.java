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
    private final RestClient naverMapRestClient;
    private final NaverMapProperties naverMapProperties;

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
                log.warn("Kakao address search returned no results for address: {}", address);
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

    // 주소 문자열을 네이버 지도로 좌표 변환한다. 카카오 주소 검색이 특정 주소를 못 찾는 경우가 있어 "직접 설정" 흐름에서 사용한다.
    public Optional<LocationCoordinate> geocodeAddressViaNaver(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        if (!naverMapProperties.isConfigured()) {
            log.warn("Naver Map API key is not configured. Skipping geocoding.");
            return Optional.empty();
        }

        try {
            NaverGeocodeSearchResponse response = naverMapRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/map-geocode/v2/geocode")
                            .queryParam("query", address)
                            .build())
                    .header("X-NCP-APIGW-API-KEY-ID", naverMapProperties.getClientId())
                    .header("X-NCP-APIGW-API-KEY", naverMapProperties.getClientSecret())
                    .retrieve()
                    .body(NaverGeocodeSearchResponse.class);

            if (response == null || response.addresses() == null || response.addresses().isEmpty()) {
                log.warn("Naver geocode returned no results for address: {}", address);
                return Optional.empty();
            }

            NaverGeocodeAddress result = response.addresses().getFirst();
            Double latitude = parseDouble(result.y());
            Double longitude = parseDouble(result.x());

            if (latitude == null || longitude == null) {
                return Optional.empty();
            }

            String normalizedAddress = result.roadAddress() != null && !result.roadAddress().isBlank()
                    ? result.roadAddress()
                    : result.jibunAddress();

            return Optional.of(new LocationCoordinate(latitude, longitude, normalizedAddress));
        } catch (org.springframework.web.client.RestClientResponseException restException) {
            log.error("Failed to geocode address via Naver: {} - status={}, body={}",
                    address, restException.getStatusCode(), restException.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception exception) {
            log.error("Failed to geocode address via Naver: {}", address, exception);
            return Optional.empty();
        }
    }

    public Optional<ReverseGeocodeResult> reverseGeocode(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return Optional.empty();
        }

        if (!naverMapProperties.isConfigured()) {
            log.warn("Naver Map API key is not configured. Skipping reverse geocoding.");
            return Optional.empty();
        }

        try {
            NaverReverseGeocodeResponse response = naverMapRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/map-reversegeocode/v2/gc")
                            .queryParam("coords", longitude + "," + latitude)
                            .queryParam("output", "json")
                            .queryParam("orders", "roadaddr,addr")
                            .build())
                    .header("X-NCP-APIGW-API-KEY-ID", naverMapProperties.getClientId())
                    .header("X-NCP-APIGW-API-KEY", naverMapProperties.getClientSecret())
                    .retrieve()
                    .body(NaverReverseGeocodeResponse.class);

            if (response == null || response.results() == null || response.results().isEmpty()) {
                return Optional.empty();
            }

            // 도로명주소(roadaddr) 결과를 우선 사용하고, 없으면(시골 지역 등) 지번주소(addr)로 대체한다.
            NaverGeocodeResult result = response.results().stream()
                    .filter(item -> "roadaddr".equals(item.name()))
                    .findFirst()
                    .orElse(response.results().getFirst());
            boolean isRoadAddress = "roadaddr".equals(result.name());

            String address = joinWithSpace(
                    result.region() != null && result.region().area1() != null ? result.region().area1().name() : null,
                    result.region() != null && result.region().area2() != null ? result.region().area2().name() : null
            );

            String landNumber = result.land() != null
                    ? joinWithHyphen(result.land().number1(), result.land().number2())
                    : null;

            String address1;
            if (isRoadAddress) {
                address1 = joinWithSpace(result.land() != null ? result.land().name() : null, landNumber);
            } else {
                // 지번주소는 도로명이 없으므로 동/읍/면 이름과 "OO번지"로 표시해야 위치를 구분할 수 있다.
                String dong = result.region() != null && result.region().area3() != null
                        ? result.region().area3().name()
                        : null;
                address1 = joinWithSpace(
                        dong,
                        landNumber != null && !landNumber.isBlank() ? landNumber + "번지" : null
                );
            }

            String address2 = result.land() != null && result.land().addition0() != null
                    ? result.land().addition0().value()
                    : null;

            if (address.isBlank() && address1.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new ReverseGeocodeResult(
                    address.isBlank() ? null : address,
                    address1.isBlank() ? null : address1,
                    (address2 == null || address2.isBlank()) ? null : address2
            ));
        } catch (org.springframework.web.client.RestClientResponseException restException) {
            log.error("Failed to reverse geocode coordinates: {}, {} - status={}, body={}",
                    latitude, longitude, restException.getStatusCode(), restException.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception exception) {
            log.error("Failed to reverse geocode coordinates: {}, {}", latitude, longitude, exception);
            return Optional.empty();
        }
    }

    private String joinWithSpace(String first, String second) {
        StringBuilder builder = new StringBuilder();
        if (first != null && !first.isBlank()) {
            builder.append(first.trim());
        }
        if (second != null && !second.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(second.trim());
        }
        return builder.toString().trim();
    }

    private String joinWithHyphen(String first, String second) {
        if (first == null || first.isBlank()) {
            return null;
        }
        if (second == null || second.isBlank()) {
            return first.trim();
        }
        return first.trim() + "-" + second.trim();
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

    public record ReverseGeocodeResult(String address, String address1, String address2) {
    }

    private record NaverGeocodeSearchResponse(List<NaverGeocodeAddress> addresses) {
    }

    private record NaverGeocodeAddress(String roadAddress, String jibunAddress, String x, String y) {
    }

    private record NaverReverseGeocodeResponse(List<NaverGeocodeResult> results) {
    }

    private record NaverGeocodeResult(String name, NaverRegion region, NaverLand land) {
    }

    private record NaverRegion(NaverArea area1, NaverArea area2, NaverArea area3) {
    }

    private record NaverArea(String name) {
    }

    private record NaverLand(String name, String number1, String number2, NaverAddition addition0) {
    }

    private record NaverAddition(String value) {
    }
}
