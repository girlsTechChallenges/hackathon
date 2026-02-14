package com.fiap.user.health.bff.model;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    private Long id;
    private String nome;
    private String email;
    private String login;
    private String senha;
}
