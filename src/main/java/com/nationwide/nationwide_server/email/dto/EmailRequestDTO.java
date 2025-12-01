package com.nationwide.nationwide_server.email.dto;

import com.nationwide.nationwide_server.email.Email;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequestDTO {

    private String email;
    private String code;

}
