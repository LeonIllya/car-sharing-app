package car.sharing.service.impl;

import car.sharing.dto.user.UpdateUserRole;
import car.sharing.dto.user.UserRegistrationRequestDto;
import car.sharing.dto.user.UserRequestDto;
import car.sharing.dto.user.UserResponseDto;
import car.sharing.dto.user.UserResponseUpdateRole;
import car.sharing.exception.EntityNotFoundException;
import car.sharing.exception.RegistrationException;
import car.sharing.mapper.UserMapper;
import car.sharing.model.Role;
import car.sharing.model.User;
import car.sharing.repository.role.RoleRepository;
import car.sharing.repository.user.UserRepository;
import car.sharing.service.UserService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserResponseUpdateRole updateRoleForUser(UpdateUserRole userRole, Long userId) {
        User user = getUserById(userId);
        Set<Role> byRoleName = roleRepository.findByUserRole(userRole.userRole());
        user.setRoles(byRoleName);
        return userMapper.toUpdateRole(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException(
                    "This email is already registered: " + requestDto.getEmail());
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(roleRepository.getByUserRole(Role.UserRole.CUSTOMER)));

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo(User user) {
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserInfo(UserRequestDto requestDto, Long userId) {
        User user = getUserById(userId);
        user.setFirstName(requestDto.firstName());
        user.setLastName(requestDto.lastName());
        return userMapper.toDto(userRepository.save(user));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() ->
                new EntityNotFoundException("Can`t find user by id: " + userId));
    }
}
