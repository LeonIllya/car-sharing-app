package car.sharing.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import car.sharing.dto.user.UpdateUserRole;
import car.sharing.dto.user.UserRegistrationRequestDto;
import car.sharing.dto.user.UserRequestDto;
import car.sharing.dto.user.UserResponseDto;
import car.sharing.dto.user.UserResponseUpdateRole;
import car.sharing.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@Sql(scripts = "classpath:database/users/add-users.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/users/remove-users.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext webContext) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webContext)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("Register new user")
    void registerNewUser_ValidUserRegistrationRequestDto_ShouldReturnUserResponseDto()
            throws Exception {
        //Given
        UserRegistrationRequestDto requestDto = createUserRegistrationRequestDto();
        UserResponseDto userDtoExpected = new UserResponseDto(5L,
                requestDto.getEmail(), requestDto.getFirstName(), requestDto.getLastName());
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(post("/auth/registration")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        UserResponseDto userResponseDtoActual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        assertNotNull(userResponseDtoActual);
        EqualsBuilder.reflectionEquals(userDtoExpected, userResponseDtoActual, "id");
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Update role for user")
    void updateRoleForUser_ValidUpdateUserRole_ShouldReturnUserResponseUpdateRole()
            throws Exception {
        //Given
        UpdateUserRole userRole = createUserRole();

        Set<Role> setOfRolesExpected = getSetOfRoles(userRole.userRole());
        Long id = 3L;
        String jsonRequest = objectMapper.writeValueAsString(userRole);

        //When
        MvcResult result = mockMvc.perform(put("/users/{id}/role", id)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseUpdateRole userResponseUpdateRole = objectMapper
                .readValue(result.getResponse()
                        .getContentAsString(), UserResponseUpdateRole.class);
        assertNotNull(userResponseUpdateRole);
        EqualsBuilder.reflectionEquals(setOfRolesExpected, userResponseUpdateRole.role(), "id");
    }

    @Test
    @WithUserDetails("messi@gmail.com")
    @DisplayName("Get user's info")
    void getUserInfo_ValidEmail_ShouldReturnUserResponseDto() throws Exception {
        //Given
        UserResponseDto userDtoExpected = createUserDto();

        //When
        MvcResult result = mockMvc.perform(get("/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        UserResponseDto userResponseDtoActual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        assertNotNull(userResponseDtoActual);
        EqualsBuilder.reflectionEquals(userDtoExpected, userResponseDtoActual, "id");
    }

    @Test
    @WithUserDetails("messi@gmail.com")
    @DisplayName("Update user's info")
    void updateUserInfo_ValidEmail_ShouldReturnUserResponseDto() throws Exception {
        //Given
        UserRequestDto userRequestDto = createUserRequestDto();

        UserResponseDto userDtoExpected = new UserResponseDto(4L,
                "messi@gmail.com", "Tom", "Cruise");
        String jsonRequest = objectMapper.writeValueAsString(userRequestDto);
        //When
        MvcResult result = mockMvc.perform(put("/users/me")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        UserResponseDto userResponseDtoActual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        assertNotNull(userResponseDtoActual);
        EqualsBuilder.reflectionEquals(userDtoExpected, userResponseDtoActual, "id");
    }

    private UserRegistrationRequestDto createUserRegistrationRequestDto() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("neymar@gmail.com");
        requestDto.setPassword("1617181920");
        requestDto.setRepeatPassword("1617181920");
        requestDto.setFirstName("Sergio");
        requestDto.setLastName("Ramos");
        return requestDto;
    }

    private UpdateUserRole createUserRole() {
        return new UpdateUserRole(Role.UserRole.MANAGER);
    }

    private Set<Role> getSetOfRoles(Role.UserRole userRole) {
        Role role = new Role();
        role.setUserRole(userRole);
        return Set.of(role);
    }

    private UserResponseDto createUserDto() {
        return new UserResponseDto(4L, "messi@gmail.com",
                "Lionel", "Messi");
    }

    private UserRequestDto createUserRequestDto() {
        return new UserRequestDto("Tom", "Cruise");
    }
}
