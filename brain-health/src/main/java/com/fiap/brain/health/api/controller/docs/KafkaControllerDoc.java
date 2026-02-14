package com.fiap.brain.health.api.controller.docs;

import com.fiap.brain.health.api.dto.error.ProblemDetail;
import com.fiap.brain.health.api.dto.request.KafkaTestMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Kafka Testing", description = "Endpoints para teste de integração Kafka")
public interface KafkaControllerDoc {

    @Operation(
        summary = "Enviar mensagem de teste para Kafka",
        description = "Envia mensagem para o tópico brain-health-request"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Mensagem enviada com sucesso",
            content = @Content(schema = @Schema(implementation = Map.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Erro de validação",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    ResponseEntity<Map<String, Object>> sendTestMessage(@Valid @RequestBody KafkaTestMessageRequest request);

    @Operation(
        summary = "Obter informações de configuração Kafka",
        description = "Retorna nomes dos tópicos e status da integração"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Configuração obtida com sucesso",
            content = @Content(schema = @Schema(implementation = Map.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    ResponseEntity<Map<String, String>> getKafkaInfo();
}
