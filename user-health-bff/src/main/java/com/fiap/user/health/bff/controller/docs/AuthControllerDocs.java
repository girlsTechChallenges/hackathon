package com.fiap.user.health.bff.controller.docs;

import com.fiap.user.health.bff.dto.request.UserAuthRequestDto;
import com.fiap.user.health.bff.dto.request.UserCredentialsRequestDto;
import com.fiap.user.health.bff.exception.ApiErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "API endpoints for user authentication")
public interface AuthControllerDocs {

    @Operation(summary = "Login", description = "Allows a user to authenticate by logging in with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authenticated successfully", 
                    content = @Content(schema = @Schema(implementation = UserAuthRequestDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found", 
                    content = @Content(schema = @Schema(implementation = ApiErrorMessage.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password", 
                    content = @Content(schema = @Schema(implementation = ApiErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", 
                    content = @Content(schema = @Schema(implementation = ApiErrorMessage.class)))
    })
    ResponseEntity<UserAuthRequestDto> login(@Valid @RequestBody UserCredentialsRequestDto loginRequest);

    @Operation(summary = "Change password", description = "Allows a user to change an already registered password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", 
                    content = @Content(schema = @Schema(implementation = ApiErrorMessage.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", 
                    content = @Content(schema = @Schema(implementation = ApiErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", 
                    content = @Content(schema = @Schema(implementation = ApiErrorMessage.class)))
    })
    ResponseEntity<Void> updatePassword(@RequestBody UserCredentialsRequestDto request);
}

