package com.fiap.brain.health.infrastructure.adapter.ai;

import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAIProcessingAdapter implements AIProcessingPort {

    private final ChatModel chatModel;

    @Override
    public AIProcessingResult processArticle(String question, MedicalArticle article) {
        log.info("Processing article with OpenAI - URL: {}, Content length: {}",
                article.getArticleUrl(), article.getContentLength());

        try {
            var converter = new BeanOutputConverter<>(OpenAIResponseDTO.class);
            var prompt = buildPrompt(question, article.getContent(), converter.getFormat());

            var aiResponse = chatModel.call(new Prompt(prompt))
                    .getResult()
                    .getOutput()
                    .getContent();

            log.debug("OpenAI response received: {} characters", aiResponse.length());

            var openAIResponse = converter.convert(aiResponse);

            if (openAIResponse == null) {
                throw new AIProcessingException("OpenAI response conversion returned null");
            }

            return toDomainResult(openAIResponse);

        } catch (Exception e) {
            log.error("Error processing with OpenAI: {}", e.getMessage(), e);
            throw new AIProcessingException("Failed to process article with OpenAI", e);
        }
    }

    private String buildPrompt(String question, String content, String format) {
        return String.format("""
                Você é especialista em saúde.
                
                Pergunta: %s
                Conteúdo: %s
                
                Crie:
                1. Título
                2. Introdução
                3. Até 3 recomendações (cada com categoria, descrição e dicas)
                4. Conclusão
                5. 3 quiz (perguntas de múltipla escolha)
                
                IMPORTANTE:
                - NÃO inclua o campo "timestamp" na resposta
                - NÃO inclua o campo "sourceLink" na resposta
                - NÃO inclua o campo "processedAt" na resposta
                - NÃO inclua o campo "context" na resposta
                
                %s
                """, question, content, format);
    }

    private AIProcessingResult toDomainResult(OpenAIResponseDTO dto) {
        List<RecommendationItem> recommendations = dto.recommendations != null
                ? dto.recommendations.stream()
                        .map(r -> new RecommendationItem(r.category, r.description, r.tips))
                        .toList()
                : List.of();

        List<QuizItem> quizzes = dto.quizzes != null
                ? dto.quizzes.stream()
                        .map(q -> new QuizItem(q.question, q.options, q.correctAnswer))
                        .toList()
                : List.of();

        return new AIProcessingResult(
                dto.title,
                dto.introduction,
                recommendations,
                dto.conclusion,
                quizzes,
                LocalDateTime.now()
        );
    }

    private record OpenAIResponseDTO(
            String title,
            String introduction,
            List<RecommendationDTO> recommendations,
            String conclusion,
            List<QuizDTO> quizzes
    ) {}

    private record RecommendationDTO(
            String category,
            String description,
            List<String> tips
    ) {}

    private record QuizDTO(
            String question,
            List<String> options,
            String correctAnswer
    ) {}
}
