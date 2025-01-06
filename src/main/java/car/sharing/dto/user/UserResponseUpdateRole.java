package car.sharing.dto.user;

public record UserResponseUpdateRole(
        Long id,
        String email,
        String role,
        String firstName,
        String lastName
) {
}
