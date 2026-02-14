package com.fiap.check.health.mapper;

import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.GoalRequestFrequency;
import com.fiap.check.health.api.model.GoalRequestReward;
import com.fiap.check.health.api.model.GoalResponse;
import com.fiap.check.health.model.GoalCategory;
import com.fiap.check.health.persistence.entity.Goal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoalMapper Tests")
class GoalMapperTest {

    private GoalMapper goalMapper;
    private GoalRequest goalRequest;
    private Goal goalEntity;

    @BeforeEach
    void setUp() {
        goalMapper = new GoalMapper();
        
        // Preparar o GoalRequest mock com todos os campos
        GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                .timesPerPeriod(1)
                .periodicity("daily")
                .build();
        
        GoalRequestReward reward = GoalRequestReward.builder()
                .points(10)
                .badge("Parabéns por completar!")
                .build();

        goalRequest = GoalRequest.builder()
                .userId("user123")
                .title("Exercitar-se diariamente")
                .description("Fazer 30 minutos de exercícios por dia")
                .startDate(LocalDate.of(2026, 2, 8))
                .endDate(LocalDate.of(2026, 3, 10))
                .frequency(frequency)
                .reward(reward)
                .notifications(true)
                .build();

        // Preparar a entidade Goal com todos os campos
        goalEntity = Goal.builder()
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
                .build();
    }

    @Test
    @DisplayName("Deve converter GoalRequest para Goal entity com sucesso")
    void shouldConvertGoalRequestToEntitySuccessfully() {
        // When
        Goal result = goalMapper.toEntity(goalRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(result.getTitle()).isEqualTo("Exercitar-se diariamente");
        assertThat(result.getDescription()).isEqualTo("Fazer 30 minutos de exercícios por dia");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 8));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(result.getNotifications()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar null quando GoalRequest for null")
    void shouldReturnNullWhenGoalRequestIsNull() {
        // When
        Goal result = goalMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve converter Goal entity para GoalResponse com sucesso")
    void shouldConvertEntityToGoalResponseSuccessfully() {
        // When
        GoalResponse result = goalMapper.toResponse(goalEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGoalId()).isEqualTo("1");
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(result.getTitle()).isEqualTo("Exercitar-se diariamente");
        assertThat(result.getStatus()).isEqualTo("active");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar null quando Goal entity for null")
    void shouldReturnNullWhenGoalEntityIsNull() {
        // When
        GoalResponse result = goalMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve lidar com campos opcionais nulos corretamente")
    void shouldHandleOptionalNullFieldsCorrectly() {
        // Given
        GoalRequest requestWithNulls = GoalRequest.builder()
                .userId("user123")
                .title("Meta simples")
                .startDate(LocalDate.now())
                .notifications(false)
                // Campos opcionais deixados como null
                .description(null)
                .endDate(null)
                .frequency(null)
                .reward(null)
                .build();

        // When
        Goal result = goalMapper.toEntity(requestWithNulls);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(result.getTitle()).isEqualTo("Meta simples");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getEndDate()).isNull();
        assertThat(result.getFrequency()).isNull();
        assertThat(result.getReward()).isNull();
        assertThat(result.getNotifications()).isFalse();
    }

    @Test
    @DisplayName("Deve converter goalId null para string null em GoalResponse")
    void shouldConvertNullGoalIdToNullStringInResponse() {
        // Given
        Goal entityWithNullId = Goal.builder()
                .goalId(null)
                .userId(goalEntity.getUserId())
                .title(goalEntity.getTitle())
                .description(goalEntity.getDescription())
                .category(goalEntity.getCategory())
                .type(goalEntity.getType())
                .startDate(goalEntity.getStartDate())
                .endDate(goalEntity.getEndDate())
                .status(goalEntity.getStatus())
                .notifications(goalEntity.getNotifications())
                .createdAt(goalEntity.getCreatedAt())
                .build();

        // When
        GoalResponse result = goalMapper.toResponse(entityWithNullId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGoalId()).isNull();
    }

    @Test
    @DisplayName("Deve mapear todas as categorias corretamente")
    void shouldMapAllCategoriesCorrectly() {
        // Test SAUDE_FISICA
        GoalRequest requestSaudeFisica = GoalRequest.builder()
                .userId("user1")
                .title("Meta Física")
                .category(GoalRequest.CategoryEnum.SAUDE_FISICA)
                .type(GoalRequest.TypeEnum.DAILY)
                .startDate(LocalDate.now())
                .build();
        Goal resultFisica = goalMapper.toEntity(requestSaudeFisica);
        assertThat(resultFisica.getCategory()).isEqualTo(GoalCategory.SAUDE_FISICA);

        // Test SAUDE_MENTAL
        GoalRequest requestSaudeMental = GoalRequest.builder()
                .userId("user2")
                .title("Meta Mental")
                .category(GoalRequest.CategoryEnum.SAUDE_MENTAL)
                .type(GoalRequest.TypeEnum.DAILY)
                .startDate(LocalDate.now())
                .build();
        Goal resultMental = goalMapper.toEntity(requestSaudeMental);
        assertThat(resultMental.getCategory()).isEqualTo(GoalCategory.SAUDE_MENTAL);

        // Test NUTRICAO
        GoalRequest requestNutricao = GoalRequest.builder()
                .userId("user3")
                .title("Meta Nutrição")
                .category(GoalRequest.CategoryEnum.NUTRICAO)
                .type(GoalRequest.TypeEnum.WEEKLY)
                .startDate(LocalDate.now())
                .build();
        Goal resultNutricao = goalMapper.toEntity(requestNutricao);
        assertThat(resultNutricao.getCategory()).isEqualTo(GoalCategory.NUTRICAO);

        // Test SONO
        GoalRequest requestSono = GoalRequest.builder()
                .userId("user4")
                .title("Meta Sono")
                .category(GoalRequest.CategoryEnum.SONO)
                .type(GoalRequest.TypeEnum.MONTHLY)
                .startDate(LocalDate.now())
                .build();
        Goal resultSono = goalMapper.toEntity(requestSono);
        assertThat(resultSono.getCategory()).isEqualTo(GoalCategory.SONO);

        // Test BEM_ESTAR
        GoalRequest requestBemEstar = GoalRequest.builder()
                .userId("user5")
                .title("Meta Bem-estar")
                .category(GoalRequest.CategoryEnum.BEM_ESTAR)
                .type(GoalRequest.TypeEnum.SINGLE)
                .startDate(LocalDate.now())
                .build();
        Goal resultBemEstar = goalMapper.toEntity(requestBemEstar);
        assertThat(resultBemEstar.getCategory()).isEqualTo(GoalCategory.BEM_ESTAR);
    }

    @Test
    @DisplayName("Deve mapear diferentes tipos de goal corretamente")
    void shouldMapDifferentGoalTypesCorrectly() {
        // Test DAILY
        GoalRequest dailyRequest = GoalRequest.builder()
                .userId("user1")
                .title("Daily Goal")
                .category(GoalRequest.CategoryEnum.SAUDE_FISICA)
                .type(GoalRequest.TypeEnum.DAILY)
                .startDate(LocalDate.now())
                .build();
        Goal dailyResult = goalMapper.toEntity(dailyRequest);
        assertThat(dailyResult.getType()).isEqualTo("daily");

        // Test WEEKLY
        GoalRequest weeklyRequest = GoalRequest.builder()
                .userId("user2")
                .title("Weekly Goal")
                .category(GoalRequest.CategoryEnum.SAUDE_MENTAL)
                .type(GoalRequest.TypeEnum.WEEKLY)
                .startDate(LocalDate.now())
                .build();
        Goal weeklyResult = goalMapper.toEntity(weeklyRequest);
        assertThat(weeklyResult.getType()).isEqualTo("weekly");

        // Test MONTHLY
        GoalRequest monthlyRequest = GoalRequest.builder()
                .userId("user3")
                .title("Monthly Goal")
                .category(GoalRequest.CategoryEnum.NUTRICAO)
                .type(GoalRequest.TypeEnum.MONTHLY)
                .startDate(LocalDate.now())
                .build();
        Goal monthlyResult = goalMapper.toEntity(monthlyRequest);
        assertThat(monthlyResult.getType()).isEqualTo("monthly");

        // Test SINGLE
        GoalRequest singleRequest = GoalRequest.builder()
                .userId("user4")
                .title("Single Goal")
                .category(GoalRequest.CategoryEnum.SONO)
                .type(GoalRequest.TypeEnum.SINGLE)
                .startDate(LocalDate.now())
                .build();
        Goal singleResult = goalMapper.toEntity(singleRequest);
        assertThat(singleResult.getType()).isEqualTo("single");
    }

    @Test
    @DisplayName("Deve converter frequency corretamente")
    void shouldConvertFrequencyCorrectly() {
        // Given
        GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                .periodicity("weekly")
                .timesPerPeriod(3)
                .build();

        GoalRequest request = GoalRequest.builder()
                .userId("user123")
                .title("Goal with Frequency")
                .category(GoalRequest.CategoryEnum.SAUDE_FISICA)
                .type(GoalRequest.TypeEnum.DAILY)
                .startDate(LocalDate.now())
                .frequency(frequency)
                .build();

        // When
        Goal result = goalMapper.toEntity(request);

        // Then
        assertThat(result.getFrequency()).isNotNull();
        assertThat(result.getFrequency().getPeriodicity()).isEqualTo("weekly");
        assertThat(result.getFrequency().getTimesPerPeriod()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve converter reward corretamente")
    void shouldConvertRewardCorrectly() {
        // Given
        GoalRequestReward reward = GoalRequestReward.builder()
                .points(100)
                .badge("Champion Badge")
                .build();

        GoalRequest request = GoalRequest.builder()
                .userId("user123")
                .title("Goal with Reward")
                .category(GoalRequest.CategoryEnum.NUTRICAO)
                .type(GoalRequest.TypeEnum.DAILY)
                .startDate(LocalDate.now())
                .reward(reward)
                .build();

        // When
        Goal result = goalMapper.toEntity(request);

        // Then
        assertThat(result.getReward()).isNotNull();
        assertThat(result.getReward().getPoints()).isEqualTo(100);
        assertThat(result.getReward().getBadge()).isEqualTo("Champion Badge");
    }

    @Test
    @DisplayName("Deve converter goal com progress para response corretamente")
    void shouldConvertGoalWithProgressToResponseCorrectly() {
        // Given
        com.fiap.check.health.model.Progress progress = com.fiap.check.health.model.Progress.builder()
                .completed(15)
                .total(30)
                .unit("days")
                .build();

        Goal goalWithProgress = Goal.builder()
                .goalId(5L)
                .userId("user123")
                .title("Goal with Progress")
                .status("active")
                .createdAt(LocalDateTime.of(2026, 2, 9, 14, 30))
                .progress(progress)
                .build();

        // When
        GoalResponse result = goalMapper.toResponse(goalWithProgress);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProgress()).isNotNull();
        assertThat(result.getProgress().getCompleted()).isEqualTo(15);
        assertThat(result.getProgress().getTotal()).isEqualTo(30);
        assertThat(result.getProgress().getUnit()).isEqualTo("days");
        assertThat(result.getMessage()).contains("Progress updated!");
        assertThat(result.getMessage()).contains("15 of 30 days");
    }

    @Test
    @DisplayName("Deve converter goal com reward para response com gamification")
    void shouldConvertGoalWithRewardToResponseWithGamification() {
        // Given
        com.fiap.check.health.model.Reward reward = com.fiap.check.health.model.Reward.builder()
                .points(200)
                .badge("Elite Badge")
                .build();

        Goal goalWithReward = Goal.builder()
                .goalId(6L)
                .userId("user123")
                .title("Goal with Reward")
                .status("completed")
                .createdAt(LocalDateTime.now())
                .reward(reward)
                .build();

        // When
        GoalResponse result = goalMapper.toResponse(goalWithReward);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGamification()).isNotNull();
        assertThat(result.getGamification().getPointsEarned()).isEqualTo(200);
        assertThat(result.getGamification().getBadge()).isEqualTo("Elite Badge");
        assertThat(result.getGamification().getUserLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve gerar mensagem de progresso com valores null seguros")
    void shouldGenerateProgressMessageWithNullSafeValues() {
        // Given
        com.fiap.check.health.model.Progress progressWithNulls = com.fiap.check.health.model.Progress.builder()
                .completed(null)
                .total(null)
                .unit(null)
                .build();

        Goal goalWithNullProgress = Goal.builder()
                .goalId(7L)
                .userId("user123")
                .title("Goal with Null Progress")
                .status("active")
                .progress(progressWithNulls)
                .build();

        // When
        GoalResponse result = goalMapper.toResponse(goalWithNullProgress);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("0 of 0 days");
        assertThat(result.getMessage()).contains("earned 0 points");
    }

    @Test
    @DisplayName("Deve converter goal sem progress para response sem message")
    void shouldConvertGoalWithoutProgressToResponseWithoutMessage() {
        // Given
        Goal goalWithoutProgress = Goal.builder()
                .goalId(8L)
                .userId("user123")
                .title("Goal without Progress")
                .status("pending")
                .createdAt(LocalDateTime.now())
                .progress(null)
                .build();

        // When
        GoalResponse result = goalMapper.toResponse(goalWithoutProgress);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProgress()).isNull();
        assertThat(result.getMessage()).isNull();
        assertThat(result.getGamification()).isNotNull();
        assertThat(result.getGamification().getPointsEarned()).isNull();
        assertThat(result.getGamification().getBadge()).isNull();
        assertThat(result.getGamification().getUserLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve converter createdAt para OffsetDateTime corretamente")
    void shouldConvertCreatedAtToOffsetDateTimeCorrectly() {
        // Given
        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 9, 15, 45, 30);
        Goal goalWithCreatedAt = Goal.builder()
                .goalId(9L)
                .userId("user123")
                .title("Goal with CreatedAt")
                .status("active")
                .createdAt(createdAt)
                .build();

        // When
        GoalResponse result = goalMapper.toResponse(goalWithCreatedAt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getCreatedAt().toLocalDateTime()).isEqualTo(createdAt);
        assertThat(result.getCreatedAt().getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("Deve lidar com goal sem createdAt")
    void shouldHandleGoalWithoutCreatedAt() {
        // Given
        Goal goalWithoutCreatedAt = Goal.builder()
                .goalId(10L)
                .userId("user123")
                .title("Goal without CreatedAt")
                .status("active")
                .createdAt(null)
                .build();

        // When
        GoalResponse result = goalMapper.toResponse(goalWithoutCreatedAt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreatedAt()).isNull();
    }
}