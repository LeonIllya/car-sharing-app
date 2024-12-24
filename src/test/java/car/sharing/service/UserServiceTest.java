package car.sharing.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import car.sharing.dto.user.UpdateUserRole;
import car.sharing.dto.user.UserRegistrationRequestDto;
import car.sharing.dto.user.UserRequestDto;
import car.sharing.dto.user.UserResponseDto;
import car.sharing.exception.EntityNotFoundException;
import car.sharing.exception.RegistrationException;
import car.sharing.mapper.UserMapper;
import car.sharing.mapper.impl.UserMapperImpl;
import car.sharing.model.Role;
import car.sharing.model.User;
import car.sharing.repository.role.RoleRepository;
import car.sharing.repository.user.UserRepository;
import car.sharing.service.impl.UserServiceImpl;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Spy
    private UserMapper userMapper = new UserMapperImpl();
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationRequestDto requestDto;

    private User user;

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setUserRole(Role.UserRole.CUSTOMER);

        requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("admin1@gmail.com");
        requestDto.setPassword("12345678910");
        requestDto.setRepeatPassword("12345678910");
        requestDto.setFirstName("Danil");
        requestDto.setLastName("Zinchenko");

        user = new User();
        user.setId(1L);
        user.setEmail(requestDto.getEmail());
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setPassword(requestDto.getPassword());
        user.setRoles(Set.of(role));

    }

    @Test
    @DisplayName("Register a new user")
    public void registerUser_ValidUser_ShouldReturnUserResponseDto() throws RegistrationException {
        // Given
        UserResponseDto responseDto = new UserResponseDto(user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName());

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("admin1@gmail.com");
        when(roleRepository.getByUserRole(Role.UserRole.CUSTOMER)).thenReturn(role);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        // When
        UserResponseDto result = userService.register(requestDto);

        // Then
        Assertions.assertEquals(responseDto, result);
        verify(userRepository, times(1)).existsByEmail(requestDto.getEmail());
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @DisplayName("Register a new user with invalid parameters")
    public void registerUser_InvalidUserWithExistsEmailInDB_ShouldReturnRegistrationException() {
        // Given
        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        //When
        RegistrationException exception = Assertions.assertThrows(
                RegistrationException.class,
                () -> userService.register(requestDto)
        );

        //Then
        String expected = "This email is already registered: " + requestDto.getEmail();
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(userRepository, times(1)).existsByEmail(requestDto.getEmail());
    }

    @Test
    @DisplayName("Update role for user")
    public void updateUserRole_ValidUpdateUser_Success() {
        // Given
        UpdateUserRole userRole = new UpdateUserRole(Role.UserRole.CUSTOMER);
        Role role1 = new Role();
        role1.setUserRole(Role.UserRole.MANAGER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserRole(userRole.userRole()))
                .thenReturn(Set.of(role1));
        when(userRepository.save(user)).thenReturn(user);

        // When
        userService.updateRoleForUser(userRole, 1L);

        // Then
        Assertions.assertEquals(Set.of(role1), user.getRoles());
        verify(userRepository, times(1)).findById(user.getId());
        verify(roleRepository, times(1)).findByUserRole(userRole.userRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Get an user`s profile")
    public void getUserProfile_ValidUser_ShouldReturnUserResponseDto() {
        // Given
        UserResponseDto responseDto = new UserResponseDto(user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName());

        when(userMapper.toDto(user)).thenReturn(responseDto);

        // When
        UserResponseDto userInfoExpected = userService.getUserInfo(user);

        // Then
        Assertions.assertEquals(responseDto, userInfoExpected);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @DisplayName("Update user`s profile by user")
    public void updateUserInfo_ValidUser_Success() {
        // Given
        UserRequestDto userRequestDto = new UserRequestDto("Andrey", "Zub");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        //When
        userService.updateUserInfo(userRequestDto, user.getId());

        //Then
        Assertions.assertEquals(userRequestDto.firstName(), user.getFirstName());
        Assertions.assertEquals(userRequestDto.lastName(), user.getLastName());
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Update user`s profile with invalid data")
    public void updateUserInfo_InvalidUser_ShouldReturnEntityNotFoundException() {
        //Given
        Long userId = 100L;
        UserRequestDto updateUserInfo = new UserRequestDto("Andrey", "Zub");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUserInfo(updateUserInfo, userId)
        );

        //Then
        String expected = "Can`t find user by id: " + userId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(userRepository, times(1)).findById(100L);
    }
}
