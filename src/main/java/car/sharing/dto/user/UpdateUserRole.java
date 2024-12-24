package car.sharing.dto.user;

import car.sharing.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRole(@NotNull(message = "Role is required") Role.UserRole userRole) {
}
