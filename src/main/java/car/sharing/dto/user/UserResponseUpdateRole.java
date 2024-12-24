package car.sharing.dto.user;

import car.sharing.model.Role;
import java.util.Set;

public record UserResponseUpdateRole(
        Long id,
        String email,
        Set<Role> role,
        String firstName,
        String lastName
) {
}
