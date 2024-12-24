package car.sharing.dto.user;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserRequestDto(
        @NotBlank(message = "Please write your firstName")
        @Length(min = 1, max = 50)
        String firstName,
        @NotBlank(message = "Please write your lastName")
        @Length(min = 1, max = 50)
        String lastName
) {
}
