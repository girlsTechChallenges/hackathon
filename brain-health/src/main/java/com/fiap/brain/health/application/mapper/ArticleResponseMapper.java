package com.fiap.brain.health.application.mapper;

import com.fiap.brain.health.api.dto.response.ArticleResponse;
import com.fiap.brain.health.api.dto.response.Quiz;
import com.fiap.brain.health.api.dto.response.Recommendation;
import com.fiap.brain.health.domain.model.MedicalArticle;
import com.fiap.brain.health.domain.port.AIProcessingPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ArticleResponseMapper {

    public ArticleResponse toArticleResponse(
            AIProcessingPort.AIProcessingResult aiResult,
            MedicalArticle article) {

        List<Recommendation> recommendations = aiResult.recommendations().stream()
                .map(this::toRecommendationDTO)
                .toList();

        List<Quiz> quizzes = aiResult.quizzes().stream()
                .map(this::toQuizDTO)
                .toList();

        return new ArticleResponse(
                aiResult.title(),
                aiResult.introduction(),
                recommendations,
                aiResult.conclusion(),
                quizzes,
                article.getArticleUrl(),
                aiResult.processedAt()
        );
    }

    private Recommendation toRecommendationDTO(AIProcessingPort.RecommendationItem item) {
        return new Recommendation(
                item.category(),
                item.description(),
                item.tips()
        );
    }

    private Quiz toQuizDTO(AIProcessingPort.QuizItem item) {
        return new Quiz(
                item.question(),
                item.options(),
                item.correctAnswer()
        );
    }

    public ArticleResponse toNotFoundResponse() {
        return new ArticleResponse(
                "Artigo Não Encontrado",
                "Não foi possível encontrar artigos sobre este tema.",
                List.of(),
                "Por favor, tente reformular sua pergunta.",
                List.of(),
                null,
                LocalDateTime.now()
        );
    }

    public ArticleResponse toErrorResponse(String errorMessage) {
        return new ArticleResponse(
                "Erro no Processamento",
                errorMessage,
                List.of(),
                "Por favor, tente novamente.",
                List.of(),
                null,
                LocalDateTime.now()
        );
    }
}
