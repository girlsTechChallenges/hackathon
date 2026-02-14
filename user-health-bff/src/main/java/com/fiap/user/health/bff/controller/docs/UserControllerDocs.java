package com.fiap.user.health.bff.controller.docs;

import com.fiap.user.health.bff.dto.request.UserRequestDto;
import com.fiap.user.health.bff.dto.request.UserUpdateRequestDto;
import com.fiap.user.health.bff.dto.response.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Users", description = "API for user management")
public interface UserControllerDocs {

    @Operation(summary = "Create new user", description = "Creates a new user in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto userRequestDto);

    @Operation(summary = "List all users", description = "Returns list of all users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List returned successfully")
    })
    ResponseEntity<List<UserResponseDto>> getAllUsers();

    @Operation(summary = "Get user by ID", description = "Returns specific user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<UserResponseDto> getUserById(@Parameter(description = "User ID") @PathVariable Long id);

    @Operation(summary = "Update user", description = "Updates data of an existing user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    ResponseEntity<UserResponseDto> updateUser(@Parameter(description = "User ID") @PathVariable Long id,
                                              @RequestBody UserUpdateRequestDto updateRequestDto);

    @Operation(summary = "Remove user", description = "Removes a user from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User removed successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<Void> deleteUser(@Parameter(description = "User ID") @PathVariable Long id);
}
