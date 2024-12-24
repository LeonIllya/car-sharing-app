package car.sharing.repository;

import car.sharing.exception.EntityNotFoundException;
import car.sharing.model.Role;
import car.sharing.model.User;
import car.sharing.repository.user.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql(scripts = "classpath:database/users/add-users.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/remove-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Find user by email")
    public void findUserByEmail_ShouldReturnUser() {
        //Given
        User user = new User();
        user.setId(3L);
        user.setEmail("eto@gmail.com");
        user.setFirstName("Samuel");
        user.setLastName("Eto");
        user.setPassword("12345678910");

        Role role = new Role();
        role.setUserRole(Role.UserRole.MANAGER);
        user.setRoles(Set.of(role));

        //When
        String email = "eto@gmail.com";
        User userActual = userRepository.findByEmail(email).orElseThrow(() ->
                new EntityNotFoundException("Can`t find a user by email: " + email));

        //Then
        Assertions.assertEquals(user, userActual);
    }
}
