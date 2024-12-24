package car.sharing.service;

import car.sharing.dto.car.CarDto;
import car.sharing.dto.car.CarSearchParametersDto;
import car.sharing.dto.car.CreateCarRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarDto createCar(CreateCarRequestDto requestDto);

    CarDto updateCarById(Long id, CreateCarRequestDto requestDto);

    void deleteCarById(Long id);

    CarDto findCarById(Long carId);

    List<CarDto> findAllCars(Pageable pageable);

    List<CarDto> search(CarSearchParametersDto parametersDto);
}
