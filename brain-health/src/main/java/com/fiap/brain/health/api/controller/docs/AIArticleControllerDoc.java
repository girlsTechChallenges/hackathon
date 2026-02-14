package com.fiap.brain.health.api.controller.docs;

import com.fiap.brain.health.api.dto.error.ProblemDetail;
import com.fiap.brain.health.api.dto.request.AIArticleRequest;
import com.fiap.brain.health.api.dto.response.ArticleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "AI Articles", description = "Geração de artigos médicos com IA")
public interface AIArticleControllerDoc {

    @Operation(
        summary = "Buscar e gerar artigo médico com IA",
        description = "Busca artigos em fontes confiáveis e gera conteúdo estruturado usando OpenAI GPT-4"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Artigo gerado com sucesso",
            content = @Content(schema = @Schema(implementation = ArticleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Erro de validação",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Artigo não encontrado",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Conteúdo insuficiente",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Quota OpenAI excedida",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro no processamento",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Serviço indisponível",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    ResponseEntity<ArticleResponse> searchArticle(@Valid @RequestBody AIArticleRequest request);
}
