package com.fiap.check.health.mapper;

import com.fiap.check.health.api.model.*;
import com.fiap.check.health.model.*;
import com.fiap.check.health.persistence.entity.Goal;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class GoalMapper {

    public Goal toEntity(GoalRequest request) {
        if (request == null) {
            return null;
        }

        return Goal.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(mapCategoria(request.getCategory()))
                .type(request.getType() != null ? request.getType().getValue() : null)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .frequency(toFrequencyEntity(request.getFrequency()))
                .difficulty(request.getDifficulty() != null ? request.getDifficulty().getValue() : null)
                .reward(toRewardEntity(request.getReward()))
                .status(request.getStatus() != null ? request.getStatus().getValue() : null)
                .notifications(request.getNotifications())
                .build();
    }

    public GoalResponse toResponse(Goal goal) {
        if (goal == null) {
            return null;
        }

        GoalResponse.GoalResponseBuilder builder = GoalResponse.builder();
        
        builder.goalId(goal.getGoalId() != null ? goal.getGoalId().toString() : null);
        builder.userId(goal.getUserId());
        builder.title(goal.getTitle());
        builder.status(goal.getStatus());
        
        if (goal.getCreatedAt() != null) {
            builder.createdAt(OffsetDateTime.of(goal.getCreatedAt(), ZoneOffset.UTC));
        }
        
        builder.progress(toProgressResponse(goal.getProgress()));
        builder.gamification(toGamificationResponse(goal));
        
        // Mensagem de progresso
        if (goal.getProgress() != null) {
            int completed = goal.getProgress().getCompleted() != null ? goal.getProgress().getCompleted() : 0;
            int total = goal.getProgress().getTotal() != null ? goal.getProgress().getTotal() : 0;
            int points = goal.getReward() != null && goal.getReward().getPoints() != null ? 
                    goal.getReward().getPoints() : 0;
            
            String message = String.format(
                "Progress updated! You completed %d of %d %s and earned %d points.",
                completed, total, goal.getProgress().getUnit() != null ? goal.getProgress().getUnit() : "days", points
            );
            builder.message(message);
        }

        return builder.build();
    }

    private Frequency toFrequencyEntity(GoalRequestFrequency dto) {
        if (dto == null) {
            return null;
        }

        return Frequency.builder()
                .periodicity(dto.getPeriodicity())
                .timesPerPeriod(dto.getTimesPerPeriod())
                .build();
    }

    private Reward toRewardEntity(GoalRequestReward dto) {
        if (dto == null) {
            return null;
        }

        return Reward.builder()
                .points(dto.getPoints())
                .badge(dto.getBadge())
                .build();
    }

    private GoalResponseProgress toProgressResponse(Progress progress) {
        if (progress == null) {
            return null;
        }

        return GoalResponseProgress.builder()
                .completed(progress.getCompleted())
                .total(progress.getTotal())
                .unit(progress.getUnit())
                .build();
    }

    private GoalResponseGamification toGamificationResponse(Goal goal) {
        Integer pointsEarned = null;
        String badge = null;
        
        if (goal.getReward() != null) {
            pointsEarned = goal.getReward().getPoints();
            badge = goal.getReward().getBadge();
        }
        
        // Nível do usuário poderia ser calculado com base no total de pontos
        // Por enquanto, retornando um valor fixo
        return GoalResponseGamification.builder()
                .pointsEarned(pointsEarned)
                .badge(badge)
                .userLevel(1)
                .build();
    }

    private GoalCategory mapCategoria(GoalRequest.CategoryEnum category) {
        if (category == null) {
            return null;
        }
        
        return switch (category.getValue()) {
            case "SAUDE_FISICA" -> GoalCategory.SAUDE_FISICA;
            case "SAUDE_MENTAL" -> GoalCategory.SAUDE_MENTAL;
            case "NUTRICAO" -> GoalCategory.NUTRICAO;
            case "SONO" -> GoalCategory.SONO;
            case "BEM_ESTAR" -> GoalCategory.BEM_ESTAR;
            default -> throw new IllegalArgumentException("Categoria não reconhecida: " + category.getValue());
        };
    }
}