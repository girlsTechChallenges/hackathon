package com.fiap.brain.health.api.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetail(
        String type,
        String title,
        Integer status,
        String detail,
        String instance,
        LocalDateTime timestamp,
        String traceId,
        Map<String, Object> extensions
) {

    public static final String DEFAULT_TYPE = "about:blank";

    public static ProblemDetailBuilder builder() {
        return new ProblemDetailBuilder()
                .timestamp(LocalDateTime.now())
                .type(DEFAULT_TYPE);
    }
}
