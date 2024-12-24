package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.car.CarDto;
import car.sharing.dto.car.CreateCarRequestDto;
import car.sharing.model.Car;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto toDto(Car car);

    Car toModel(CreateCarRequestDto requestDto);
}
