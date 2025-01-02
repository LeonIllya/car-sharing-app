package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.rental.RentalRequestDto;
import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    Rental toModel(RentalRequestDto requestDto);

    @Mapping(target = "carId", source = "car.id")
    RentalResponseDto toDto(Rental rental);
}
