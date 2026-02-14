package com.fiap.check.health.controller;

import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.GoalResponse;
import com.fiap.check.health.api.model.ProgressRequest;
import com.fiap.check.health.exception.GoalNotFoundException;
import com.fiap.check.health.service.GoalService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalController Unit Tests")
class GoalControllerTest {

    @Mock
    private GoalService goalService;

    @InjectMocks
    private GoalController goalController;

    private GoalRequest goalRequest;
    private GoalResponse goalResponse;
    private ProgressRequest progressRequest;

    @BeforeEach
    void setUp() {
        goalRequest = GoalRequest.builder()
                .userId("user123")
                .title("Exercitar-se diariamente")
                .description("Fazer 30 minutos de exercícios por dia")
                .startDate(LocalDate.of(2026, 2, 8))
                .endDate(LocalDate.of(2026, 3, 10))
                .notifications(true)
                .build();

        goalResponse = GoalResponse.builder()
                .goalId("1")
                .userId("user123")
                .title("Exercitar-se diariamente")
                .status("active")
                .createdAt(OffsetDateTime.of(2026, 2, 8, 10, 30, 0, 0, ZoneOffset.UTC))
                .build();

        progressRequest = ProgressRequest.builder()
                .increment(1)
                .build();
    }

    @Nested
    @DisplayName("POST /goals Tests")
    class CreateGoalTests {

        @Test
        @DisplayName("Deve criar meta com sucesso e retornar status 201")
        void shouldCreateGoalSuccessfullyAndReturn201() {
            // Given
            when(goalService.createGoal(any(GoalRequest.class))).thenReturn(goalResponse);

            // When
            ResponseEntity<GoalResponse> response = goalController.goalsPost(goalRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getGoalId()).isEqualTo("1");
            assertThat(response.getBody().getUserId()).isEqualTo("user123");
            assertThat(response.getBody().getTitle()).isEqualTo("Exercitar-se diariamente");
        }
    }

    @Nested
    @DisplayName("GET /goals Tests")
    class ListGoalsTests {

        @Test
        @DisplayName("Deve retornar lista de metas com sucesso")
        void shouldReturnListOfGoalsSuccessfully() {
            // Given
            when(goalService.listGoals()).thenReturn(Arrays.asList(goalResponse));

            // When
            ResponseEntity<List<GoalResponse>> response = goalController.goalsGet();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getUserId()).isEqualTo("user123");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há metas")
        void shouldReturnEmptyListWhenNoGoalsExist() {
            // Given
            when(goalService.listGoals()).thenReturn(Collections.emptyList());

            // When
            ResponseEntity<List<GoalResponse>> response = goalController.goalsGet();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GET /goals/{goalId} Tests")
    class FindGoalByIdTests {

        @Test
        @DisplayName("Deve encontrar meta por ID com sucesso")
        void shouldFindGoalByIdSuccessfully() {
            // Given
            when(goalService.findById(1L)).thenReturn(Optional.of(goalResponse));

            // When
            ResponseEntity<GoalResponse> response = goalController.goalsGoalIdGet("1");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getGoalId()).isEqualTo("1");
            assertThat(response.getBody().getUserId()).isEqualTo("user123");
        }

        @Test
        @DisplayName("Deve retornar 404 quando meta não for encontrada")
        void shouldReturn404WhenGoalNotFound() {
            // Given
            when(goalService.findById(999L)).thenReturn(Optional.empty());

            // When
            ResponseEntity<GoalResponse> response = goalController.goalsGoalIdGet("999");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
        }
    }

    @Nested
    @DisplayName("PUT /goals/{goalId} Tests")  
    class UpdateGoalTests {

        @Test
        @DisplayName("Deve atualizar meta com sucesso")
        void shouldUpdateGoalSuccessfully() {
            // Given
            when(goalService.updateGoal(any(Long.class), any(GoalRequest.class)))
                    .thenReturn(goalResponse);

            // When
            ResponseEntity<GoalResponse> response = goalController.goalsGoalIdPut("1", goalRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getGoalId()).isEqualTo("1");
            assertThat(response.getBody().getUserId()).isEqualTo("user123");
        }

        @Test
        @DisplayName("Deve propagar exceção ao tentar atualizar meta inexistente")
        void shouldPropagateExceptionWhenUpdatingNonExistentGoal() {
            // Given
            when(goalService.updateGoal(anyLong(), any(GoalRequest.class)))
                    .thenThrow(new GoalNotFoundException(999L));

            // When & Then
            assertThatThrownBy(() -> goalController.goalsGoalIdPut("999", goalRequest))
                    .isInstanceOf(GoalNotFoundException.class)
                    .hasMessageContaining("Goal não encontrado com ID: 999");
        }
    }

    @Nested
    @DisplayName("DELETE /goals/{goalId} Tests")
    class DeleteGoalTests {

        @Test
        @DisplayName("Deve deletar meta com sucesso e retornar 204")
        void shouldDeleteGoalSuccessfullyAndReturn204() {
            // When
            ResponseEntity<Void> response = goalController.goalsGoalIdDelete("1");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("Deve propagar exceção ao tentar deletar meta inexistente")
        void shouldPropagateExceptionWhenDeletingNonExistentGoal() {
            // Given
            doThrow(new GoalNotFoundException(999L))
                    .when(goalService).deleteGoal(999L);

            // When & Then
            assertThatThrownBy(() -> goalController.goalsGoalIdDelete("999"))
                    .isInstanceOf(GoalNotFoundException.class)
                    .hasMessageContaining("Goal não encontrado com ID: 999");
        }
    }

    @Nested
    @DisplayName("PATCH /goals/{goalId}/progress Tests")
    class UpdateProgressTests {

        @Test
        @DisplayName("Deve atualizar progresso com sucesso")
        void shouldUpdateProgressSuccessfully() {
            // Given
            when(goalService.updateProgress(any(Long.class), any(ProgressRequest.class)))
                    .thenReturn(goalResponse);

            // When
            ResponseEntity<GoalResponse> response = goalController.goalsGoalIdProgressPatch("1", progressRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getGoalId()).isEqualTo("1");
        }

        @Test
        @DisplayName("Deve propagar exceção ao atualizar progresso de meta inexistente")
        void shouldPropagateExceptionWhenUpdatingProgressOfNonExistentGoal() {
            // Given
            when(goalService.updateProgress(anyLong(), any(ProgressRequest.class)))
                    .thenThrow(new GoalNotFoundException(999L));

            // When & Then
            assertThatThrownBy(() -> goalController.goalsGoalIdProgressPatch("999", progressRequest))
                    .isInstanceOf(GoalNotFoundException.class)
                    .hasMessageContaining("Goal não encontrado com ID: 999");
        }
    }
}