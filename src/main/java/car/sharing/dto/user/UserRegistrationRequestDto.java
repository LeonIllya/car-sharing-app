package car.sharing.dto.user;

import car.sharing.validation.Email;
import car.sharing.validation.FieldMatch;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@FieldMatch(first = "password", second = "repeatPassword")
@Data
public class UserRegistrationRequestDto {
    @Email
    @NotBlank(message = "Please write your email")
    @Length(min = 10, max = 50)
    private String email;
    @NotBlank(message = "Please write your password")
    @Length(min = 10, max = 50)
    private String password;
    @Length(min = 10, max = 50)
    private String repeatPassword;
    @NotBlank(message = "Please write your firstName")
    @Length(min = 1, max = 50)
    private String firstName;
    @NotBlank(message = "Please write your lastName")
    @Length(min = 1, max = 50)
    private String lastName;
}
