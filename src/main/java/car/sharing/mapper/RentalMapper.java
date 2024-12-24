package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.rental.RentalRequestDto;
import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.model.Rental;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    Rental toModel(RentalRequestDto requestDto);

    RentalResponseDto toDto(Rental rental);
}
