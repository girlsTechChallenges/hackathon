package com.fiap.user.health.bff.model;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Token {

    private String accessToken;
    private Long expiresIn;
    
}