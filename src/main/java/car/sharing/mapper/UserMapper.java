package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.user.UserRegistrationRequestDto;
import car.sharing.dto.user.UserResponseDto;
import car.sharing.dto.user.UserResponseUpdateRole;
import car.sharing.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toDto(User user);

    User toModel(UserRegistrationRequestDto requestDto);

    UserResponseUpdateRole toUpdateRole(User user);
}
