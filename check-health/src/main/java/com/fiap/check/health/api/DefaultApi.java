package com.fiap.check.health.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.GoalResponse;
import com.fiap.check.health.api.model.ProgressRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Validated
@Tag(name = "Default", description = "the Default API")
public interface DefaultApi {

    @Operation(
            operationId = "goalsGet",
            summary = "List all goals",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of goals",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = GoalResponse.class))))
            }
    )
    @RequestMapping(method = RequestMethod.GET, value = "/goals", produces = { "application/json" })
    ResponseEntity<List<GoalResponse>> goalsGet() throws JsonProcessingException;

    @Operation(
            operationId = "goalsPost",
            summary = "Create a new goal",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Goal created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GoalResponse.class)))
            }
    )
    @RequestMapping(method = RequestMethod.POST, value = "/goals",
            produces = { "application/json" }, consumes = { "application/json" })
    ResponseEntity<GoalResponse> goalsPost(
            @Parameter(name = "GoalRequest", required = true)
            @Valid @RequestBody GoalRequest goalRequest
    ) throws JsonProcessingException;

    @Operation(
            operationId = "goalsGoalIdGet",
            summary = "Get goal details",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Goal details",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GoalResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Goal not found")
            }
    )
    @RequestMapping(method = RequestMethod.GET, value = "/goals/{goal_id}", produces = { "application/json" })
    ResponseEntity<GoalResponse> goalsGoalIdGet(
            @Parameter(name = "goal_id", required = true, in = ParameterIn.PATH)
            @PathVariable("goal_id") String goalId
    );

    @Operation(
            operationId = "goalsGoalIdPut",
            summary = "Update an existing goal",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Goal updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GoalResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Goal not found")
            }
    )
    @RequestMapping(method = RequestMethod.PUT, value = "/goals/{goal_id}",
            produces = { "application/json" }, consumes = { "application/json" })
    ResponseEntity<GoalResponse> goalsGoalIdPut(
            @Parameter(name = "goal_id", required = true, in = ParameterIn.PATH)
            @PathVariable("goal_id") String goalId,
            @Parameter(name = "GoalRequest", required = true)
            @Valid @RequestBody GoalRequest goalRequest
    );

    @Operation(
            operationId = "goalsGoalIdDelete",
            summary = "Delete a goal",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Goal deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Goal not found")
            }
    )
    @RequestMapping(method = RequestMethod.DELETE, value = "/goals/{goal_id}")
    ResponseEntity<Void> goalsGoalIdDelete(
            @Parameter(name = "goal_id", required = true, in = ParameterIn.PATH)
            @PathVariable("goal_id") String goalId
    );

    @Operation(
            operationId = "goalsGoalIdProgressPatch",
            summary = "Update goal progress",
            description = "Record partial achievements and update rewards.",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Progress updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GoalResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Goal not found")
            }
    )
    @RequestMapping(method = RequestMethod.PATCH, value = "/goals/{goal_id}/progress",
            produces = { "application/json" }, consumes = { "application/json" })
    ResponseEntity<GoalResponse> goalsGoalIdProgressPatch(
            @Parameter(name = "goal_id", required = true, in = ParameterIn.PATH)
            @PathVariable("goal_id") String goalId,
            @Parameter(name = "ProgressRequest", required = true)
            @Valid @RequestBody ProgressRequest progressRequest
    );
}