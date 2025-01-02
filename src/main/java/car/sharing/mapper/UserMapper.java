package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.user.UserRegistrationRequestDto;
import car.sharing.dto.user.UserResponseDto;
import car.sharing.dto.user.UserResponseUpdateRole;
import car.sharing.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toDto(User user);

    User toModel(UserRegistrationRequestDto requestDto);

    @Mapping(target = "role", source = "user.roles")
    UserResponseUpdateRole toUpdateRole(User user);
}
