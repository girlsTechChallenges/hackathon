package com.fiap.check.health.service.impl;

import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.GoalResponse;
import com.fiap.check.health.api.model.ProgressRequest;
import com.fiap.check.health.event.publisher.GoalEventPublisher;
import com.fiap.check.health.exception.GoalNotFoundException;
import com.fiap.check.health.mapper.GoalMapper;
import com.fiap.check.health.persistence.entity.Goal;
import com.fiap.check.health.persistence.repository.GoalRepository;
import com.fiap.check.health.service.GoalService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final GoalEventPublisher goalEventPublisher;

    public GoalServiceImpl(GoalRepository goalRepository, GoalMapper goalMapper, GoalEventPublisher goalEventPublisher) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
        this.goalEventPublisher = goalEventPublisher;
    }

    @Override
    @Transactional
    public GoalResponse createGoal(GoalRequest goalRequest) {
        Goal goal = goalMapper.toEntity(goalRequest);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setStatus("active");
        
        // Inicializar progresso padrão se não estiver definido
        if (goal.getProgress() == null) {
            // Calcular total baseado no tipo e datas
            int total = calculateDefaultTotal(goal);
            goal.setProgress(com.fiap.check.health.model.Progress.builder()
                    .completed(0)
                    .total(total)
                    .unit(getDefaultUnit(goal.getType()))
                    .build());
        }
        
        Goal savedGoal = goalRepository.save(goal);
        
        // Publica evento de goal criado no Kafka
        try {
            goalEventPublisher.publishGoalCreated(savedGoal);
            log.info("Goal created event published successfully for goalId: {}", savedGoal.getGoalId());
        } catch (Exception e) {
            log.error("Error publishing goal created event for goalId: {}", savedGoal.getGoalId(), e);
            // Não propaga a exceção para não afetar a criação do goal
        }
        
        return goalMapper.toResponse(savedGoal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> listGoals() {
        return goalRepository.findAll()
                .stream()
                .map(goalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GoalResponse> findById(Long goalId) {
        return goalRepository.findById(goalId)
                .map(goalMapper::toResponse);
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(Long goalId, GoalRequest goalRequest) {
        return goalRepository.findById(goalId)
                .map(goal -> {
                    Goal updatedGoal = goalMapper.toEntity(goalRequest);
                    goal.setTitle(updatedGoal.getTitle());
                    goal.setDescription(updatedGoal.getDescription());
                    goal.setCategory(updatedGoal.getCategory());
                    goal.setType(updatedGoal.getType());
                    goal.setStartDate(updatedGoal.getStartDate());
                    goal.setEndDate(updatedGoal.getEndDate());
                    goal.setFrequency(updatedGoal.getFrequency());
                    goal.setDifficulty(updatedGoal.getDifficulty());
                    goal.setReward(updatedGoal.getReward());
                    goal.setStatus(updatedGoal.getStatus());
                    goal.setNotifications(updatedGoal.getNotifications());
                    Goal savedGoal = goalRepository.save(goal);
                    return goalMapper.toResponse(savedGoal);
                })
                .orElseThrow(() -> new GoalNotFoundException(goalId));
    }

    @Override
    @Transactional
    public void deleteGoal(Long goalId) {
        if (!goalRepository.existsById(goalId)) {
            throw new GoalNotFoundException(goalId);
        }
        goalRepository.deleteById(goalId);
    }

    @Override
    @Transactional
    public GoalResponse updateProgress(Long goalId, ProgressRequest progressRequest) {
        return goalRepository.findById(goalId)
                .map(goal -> {
                    if (goal.getProgress() != null) {
                        int completed = goal.getProgress().getCompleted() + progressRequest.getIncrement();
                        goal.getProgress().setCompleted(completed);
                        // lógica de gamificação: pontos extras, badges, etc.
                        if (completed >= goal.getProgress().getTotal()) {
                            goal.setStatus("completed");
                        }
                    }
                    Goal savedGoal = goalRepository.save(goal);
                    return goalMapper.toResponse(savedGoal);
                })
                .orElseThrow(() -> new GoalNotFoundException(goalId));
    }
    
    private int calculateDefaultTotal(Goal goal) {
        if (goal.getStartDate() != null && goal.getEndDate() != null) {
            long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(goal.getStartDate(), goal.getEndDate());
            
            return switch (goal.getType() != null ? goal.getType() : "daily") {
                case "daily" -> (int) daysDifference + 1; // +1 para incluir o dia final
                case "weekly" -> (int) ((daysDifference / 7) + 1);
                case "monthly" -> {
                    long monthsDifference = java.time.temporal.ChronoUnit.MONTHS.between(goal.getStartDate(), goal.getEndDate());
                    yield (int) monthsDifference + 1;
                }
                case "single" -> 1;
                default -> 30; // valor padrão de 30 dias
            };
        }
        // Valores padrão se as datas não estiverem definidas
        return switch (goal.getType() != null ? goal.getType() : "daily") {
            case "daily" -> 30;
            case "weekly" -> 4;
            case "monthly" -> 1;
            case "single" -> 1;
            default -> 30;
        };
    }
    
    private String getDefaultUnit(String type) {
        return switch (type != null ? type : "daily") {
            case "daily" -> "days";
            case "weekly" -> "weeks";
            case "monthly" -> "months";
            case "single" -> "goal";
            default -> "days";
        };
    }
}