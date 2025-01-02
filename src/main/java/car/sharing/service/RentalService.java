package car.sharing.service;

import car.sharing.dto.rental.RentalRequestDto;
import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.dto.rental.RentalSearchParametersDto;
import java.util.List;

public interface RentalService {
    RentalResponseDto createRental(RentalRequestDto requestDto, Long userId);

    List<RentalResponseDto> search(RentalSearchParametersDto search);

    RentalResponseDto getRental(Long rentalId);

    RentalResponseDto actualReturnDate(Long rentalId);
}
