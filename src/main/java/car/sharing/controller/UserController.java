package car.sharing.controller;

import car.sharing.dto.user.UpdateUserRole;
import car.sharing.dto.user.UserRequestDto;
import car.sharing.dto.user.UserResponseDto;
import car.sharing.dto.user.UserResponseUpdateRole;
import car.sharing.model.User;
import car.sharing.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for managing users "
        + "authentication and profiles")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update role", description = "Update user role by user id")
    public UserResponseUpdateRole updateRoleForUser(@RequestBody @Valid UpdateUserRole userRole,
                                                    @PathVariable @Positive Long id) {
        return userService.updateRoleForUser(userRole, id);
    }

    @GetMapping("/me")
    @Operation(summary = "Get profile", description = "Get user profile info")
    public UserResponseDto getUserInfo(Authentication authentication) {
        return userService.getUserInfo(getUser(authentication));
    }

    @PutMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update user info", description = "Update user profile info")
    public UserResponseDto updateUserInfo(@RequestBody @Valid UserRequestDto requestDto,
                                Authentication authentication) {
        return userService.updateUserInfo(requestDto, getUser(authentication).getId());
    }

    private User getUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
