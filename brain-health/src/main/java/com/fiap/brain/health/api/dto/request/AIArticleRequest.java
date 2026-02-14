package com.fiap.brain.health.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Schema(description = "Request para buscar artigo médico e processar com IA")
public record AIArticleRequest(
        @JsonProperty("goalId")
        @Schema(description = "ID da meta", example = "1")
        Long goalId,

        @JsonProperty("userId")
        @Schema(description = "ID do usuário", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "userId é obrigatório")
        Long userId,

        @JsonProperty("category")
        @Schema(description = "Categoria", example = "SAUDE_FISICA")
        String category,

        @JsonProperty("title")
        @Schema(description = "Título do artigo para buscar", example = "Benefícios da Caminhada", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "title é obrigatório")
        String title,

        @JsonProperty("description")
        @Schema(description = "Descrição adicional", example = "Artigo sobre os benefícios da caminhada")
        String description
) {
    public AIArticleRequest {
        if (userId == null) {
            throw new IllegalArgumentException("userId é obrigatório");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title é obrigatório");
        }
    }
}
