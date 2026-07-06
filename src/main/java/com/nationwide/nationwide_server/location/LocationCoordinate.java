package com.nationwide.nationwide_server.location;

public record LocationCoordinate(
        Double latitude,
        Double longitude,
        String normalizedAddress
) {
}
