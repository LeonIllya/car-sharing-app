package car.sharing.service;

import car.sharing.dto.user.UpdateUserRole;
import car.sharing.dto.user.UserRegistrationRequestDto;
import car.sharing.dto.user.UserRequestDto;
import car.sharing.dto.user.UserResponseDto;
import car.sharing.dto.user.UserResponseUpdateRole;
import car.sharing.exception.RegistrationException;
import car.sharing.model.User;

public interface UserService {
    UserResponseUpdateRole updateRoleForUser(UpdateUserRole userRole, Long userId);

    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserResponseDto getUserInfo(User user);

    UserResponseDto updateUserInfo(UserRequestDto requestDto, Long userId);
}
