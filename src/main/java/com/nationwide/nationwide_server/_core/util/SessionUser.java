package com.nationwide.nationwide_server._core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionUser {
    private Long id;
    private String loginId;
    private String name;
    private String profileImage;
}
