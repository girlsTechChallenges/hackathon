package com.fiap.check.health.util;

import com.fiap.check.health.api.model.*;
import com.fiap.check.health.model.GoalCategory;
import com.fiap.check.health.model.Progress;
import com.fiap.check.health.persistence.entity.Goal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Classe utilitária para criar dados de teste padronizados
 * Facilita a manutenção e evita duplicação de código nos testes
 */
public class TestDataFactory {

    public static class GoalEntityBuilder {
        public static Goal createValidGoalEntity() {
            return Goal.builder()
                    .goalId(1L)
                    .userId("user123")
                    .title("Exercitar-se diariamente")
                    .description("Fazer 30 minutos de exercícios por dia")
                    .category(GoalCategory.SAUDE_FISICA)
                    .type("daily")
                    .startDate(LocalDate.of(2026, 2, 8))
                    .endDate(LocalDate.of(2026, 3, 10))
                    .status("active")
                    .notifications(true)
                    .createdAt(LocalDateTime.of(2026, 2, 8, 10, 30))
                    .progress(Progress.builder().completed(0).total(30).build())
                    .build();
        }

        public static Goal createGoalWithProgress(int completed, int total) {
            Goal baseGoal = createValidGoalEntity();
            return Goal.builder()
                    .goalId(baseGoal.getGoalId())
                    .userId(baseGoal.getUserId())
                    .title(baseGoal.getTitle())
                    .description(baseGoal.getDescription())
                    .category(baseGoal.getCategory())
                    .type(baseGoal.getType())
                    .startDate(baseGoal.getStartDate())
                    .endDate(baseGoal.getEndDate())
                    .status(baseGoal.getStatus())
                    .notifications(baseGoal.getNotifications())
                    .createdAt(baseGoal.getCreatedAt())
                    .progress(Progress.builder().completed(completed).total(total).build())
                    .build();
        }

        public static Goal createCompletedGoal() {
            Goal baseGoal = createValidGoalEntity();
            return Goal.builder()
                    .goalId(baseGoal.getGoalId())
                    .userId(baseGoal.getUserId())
                    .title(baseGoal.getTitle())
                    .description(baseGoal.getDescription())
                    .category(baseGoal.getCategory())
                    .type(baseGoal.getType())
                    .startDate(baseGoal.getStartDate())
                    .endDate(baseGoal.getEndDate())
                    .notifications(baseGoal.getNotifications())
                    .createdAt(baseGoal.getCreatedAt())
                    .status("completed")
                    .progress(Progress.builder().completed(30).total(30).build())
                    .build();
        }
    }

    public static class GoalRequestBuilder {
        public static GoalRequest createValidGoalRequest() {
            return GoalRequest.builder()
                    .userId("user123")
                    .title("Exercitar-se diariamente")
                    .description("Fazer 30 minutos de exercícios por dia")
                    .category(GoalRequest.CategoryEnum.SAUDE_FISICA)
                    .type(GoalRequest.TypeEnum.DAILY)
                    .startDate(LocalDate.of(2026, 2, 8))
                    .endDate(LocalDate.of(2026, 3, 10))
                    .notifications(true)
                    .build();
        }

        public static GoalRequest createGoalRequestWithFrequency() {
            GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                    .timesPerPeriod(1)
                    .periodicity("daily")
                    .build();

            GoalRequest baseRequest = createValidGoalRequest();
            return GoalRequest.builder()
                    .userId(baseRequest.getUserId())
                    .title(baseRequest.getTitle())
                    .description(baseRequest.getDescription())
                    .category(baseRequest.getCategory())
                    .type(baseRequest.getType())
                    .startDate(baseRequest.getStartDate())
                    .endDate(baseRequest.getEndDate())
                    .notifications(baseRequest.getNotifications())
                    .frequency(frequency)
                    .build();
        }

        public static GoalRequest createGoalRequestWithReward() {
            GoalRequestReward reward = GoalRequestReward.builder()
                    .points(10)
                    .badge("Parabéns por completar!")
                    .build();

            GoalRequest baseRequest = createValidGoalRequest();
            return GoalRequest.builder()
                    .userId(baseRequest.getUserId())
                    .title(baseRequest.getTitle())
                    .description(baseRequest.getDescription())
                    .category(baseRequest.getCategory())
                    .type(baseRequest.getType())
                    .startDate(baseRequest.getStartDate())
                    .endDate(baseRequest.getEndDate())
                    .notifications(baseRequest.getNotifications())
                    .reward(reward)
                    .build();
        }

        public static GoalRequest createInvalidGoalRequest() {
            return GoalRequest.builder()
                    .userId(null) // Campo obrigatório nulo
                    .title("") // Título vazio
                    .build();
        }
    }

    public static class GoalResponseBuilder {
        public static GoalResponse createValidGoalResponse() {
            GoalResponseProgress progress = GoalResponseProgress.builder()
                    .completed(5)
                    .total(30)
                    .unit("days")
                    .build();

            GoalResponseGamification gamification = GoalResponseGamification.builder()
                    .pointsEarned(150)
                    .badge("beginner")
                    .userLevel(2)
                    .build();

            return GoalResponse.builder()
                    .goalId("1")
                    .userId("user123")
                    .title("Exercitar-se diariamente")
                    .status("active")
                    .createdAt(OffsetDateTime.of(2026, 2, 8, 10, 30, 0, 0, ZoneOffset.UTC))
                    .progress(progress)
                    .gamification(gamification)
                    .message("Meta criada com sucesso!")
                    .build();
        }

        public static GoalResponse createCompletedGoalResponse() {
            GoalResponseProgress progress = GoalResponseProgress.builder()
                    .completed(30)
                    .total(30)
                    .unit("days")
                    .build();

            GoalResponseGamification gamification = GoalResponseGamification.builder()
                    .pointsEarned(500)
                    .badge("achiever")
                    .userLevel(5)
                    .build();

            return GoalResponse.builder()
                    .goalId("1")
                    .userId("user123")
                    .title("Exercitar-se diariamente")
                    .status("completed")
                    .createdAt(OffsetDateTime.of(2026, 2, 8, 10, 30, 0, 0, ZoneOffset.UTC))
                    .progress(progress)
                    .gamification(gamification)
                    .message("Parabéns! Meta concluída com sucesso!")
                    .build();
        }
    }

    public static class ProgressRequestBuilder {
        public static ProgressRequest createValidProgressRequest() {
            return ProgressRequest.builder()
                    .increment(1)
                    .unit("days")
                    .build();
        }

        public static ProgressRequest createLargeProgressRequest() {
            return ProgressRequest.builder()
                    .increment(10)
                    .unit("days")  
                    .build();
        }

        public static ProgressRequest createFinalProgressRequest() {
            return ProgressRequest.builder()
                    .increment(5)
                    .unit("days")
                    .build();
        }

        public static ProgressRequest createProgressRequestWithIncrement(int increment) {
            return ProgressRequest.builder()
                    .increment(increment)
                    .unit("days")
                    .build();
        }

        public static ProgressRequest createInvalidProgressRequest() {
            return ProgressRequest.builder()
                    .unit("days")
                    .build(); // Sem increment obrigatório
        }
    }

    // Constantes úteis para testes
    public static final Long VALID_GOAL_ID = 1L;
    public static final Long INVALID_GOAL_ID = 999L;
    public static final String VALID_USER_ID = "user123";
    public static final String GOAL_NOT_FOUND_MESSAGE = "Goal não encontrado com ID: ";
}