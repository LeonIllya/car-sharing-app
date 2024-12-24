package car.sharing.service.impl;

import car.sharing.dto.rental.RentalRequestDto;
import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.dto.rental.RentalSearchParametersDto;
import car.sharing.exception.EntityNotFoundException;
import car.sharing.mapper.RentalMapper;
import car.sharing.model.Car;
import car.sharing.model.Rental;
import car.sharing.repository.car.CarRepository;
import car.sharing.repository.rental.RentalRepository;
import car.sharing.repository.rental.RentalSpecificationBuilder;
import car.sharing.service.RentalService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;
    private final RentalSpecificationBuilder specificationBuilder;

    @Override
    @Transactional
    public RentalResponseDto createRental(RentalRequestDto requestDto, Long userId) {
        Rental rental = rentalMapper.toModel(requestDto);
        rental.setUserId(userId);
        rentalRepository.save(rental);

        Car car = getCar(rental.getCarId());
        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);
        return rentalMapper.toDto(rental);
    }

    @Override
    public List<RentalResponseDto> search(RentalSearchParametersDto params) {
        Specification<Rental> rentalSpecification = specificationBuilder.build(params);
        return rentalRepository.findAll(rentalSpecification)
                .stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public RentalResponseDto getRentalById(Long rentalId) {
        return rentalMapper.toDto(getRental(rentalId));
    }

    @Override
    public RentalResponseDto actualReturnDate(Long rentalId) {
        Rental rental = getRental(rentalId);
        rental.setActualReturnDate(LocalDate.now());
        rental.setActive(false);

        Car car = getCar(rental.getCarId());
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
        return rentalMapper.toDto(rentalRepository.save(rental));
    }

    private Rental getRental(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Can`t find a rental by id: " + id));
    }

    private Car getCar(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can`t find a car by id: " + carId));
    }
}
