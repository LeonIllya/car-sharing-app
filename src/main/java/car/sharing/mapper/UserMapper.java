package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.user.UserRegistrationRequestDto;
import car.sharing.dto.user.UserResponseDto;
import car.sharing.dto.user.UserResponseUpdateRole;
import car.sharing.model.Role;
import car.sharing.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toDto(User user);

    User toModel(UserRegistrationRequestDto requestDto);

    @Mapping(target = "role", source = "user.roles", qualifiedByName = "getRoleForUser")
    UserResponseUpdateRole toUpdateRole(User user);

    @Named("getRoleForUser")
    default String getRoleForUser(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getUserRole().toString())
                .collect(Collectors.joining(", "));
    }
}
