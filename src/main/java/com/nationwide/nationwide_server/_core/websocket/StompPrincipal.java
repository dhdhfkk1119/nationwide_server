package com.nationwide.nationwide_server._core.websocket;

import java.security.Principal;

public record StompPrincipal(String name) implements Principal {
    @Override
    public String getName() {
        return name;
    }
}
