package com.fiap.check.health.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Goal creation request")
public class GoalRequest {
    
    @JsonProperty("user_id")
    private String userId;
    
    @NotBlank
    @JsonProperty("title")
    private String title;
    
    @NotBlank
    @JsonProperty("description")
    private String description;
    
    @NotNull
    @JsonProperty("category")
    private CategoryEnum category;
    
    @NotNull
    @JsonProperty("type")
    private TypeEnum type;
    
    @NotNull
    @JsonProperty("start_date")
    private LocalDate startDate;
    
    @NotNull
    @JsonProperty("end_date")
    private LocalDate endDate;
    
    @Valid
    @JsonProperty("frequency")
    private GoalRequestFrequency frequency;
    
    @JsonProperty("difficulty")
    private DifficultyEnum difficulty;
    
    @Valid
    @JsonProperty("reward")
    private GoalRequestReward reward;
    
    @NotNull
    @JsonProperty("status")
    private StatusEnum status;
    
    @JsonProperty("notifications")
    private Boolean notifications;

    public enum CategoryEnum {
        SAUDE_FISICA("SAUDE_FISICA"),
        SAUDE_MENTAL("SAUDE_MENTAL"), 
        NUTRICAO("NUTRICAO"),
        SONO("SONO"),
        BEM_ESTAR("BEM_ESTAR");

        private final String value;

        CategoryEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum TypeEnum {
        DAILY("daily"),
        WEEKLY("weekly"),
        MONTHLY("monthly"),
        SINGLE("single");

        private final String value;

        TypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum DifficultyEnum {
        easy("easy"),
        medium("medium"),
        hard("hard");

        private final String value;

        DifficultyEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum StatusEnum {
        active("active"),
        completed("completed"),
        archived("archived");

        private final String value;

        StatusEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}