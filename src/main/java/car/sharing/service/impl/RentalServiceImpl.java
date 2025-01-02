package car.sharing.service.impl;

import car.sharing.dto.rental.RentalRequestDto;
import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.dto.rental.RentalSearchParametersDto;
import car.sharing.exception.EntityNotFoundException;
import car.sharing.mapper.RentalMapper;
import car.sharing.model.Car;
import car.sharing.model.Rental;
import car.sharing.model.User;
import car.sharing.repository.car.CarRepository;
import car.sharing.repository.rental.RentalRepository;
import car.sharing.repository.rental.RentalSpecificationBuilder;
import car.sharing.repository.user.UserRepository;
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
    private final UserRepository userRepository;
    private final RentalSpecificationBuilder specificationBuilder;

    @Override
    @Transactional
    public RentalResponseDto createRental(RentalRequestDto requestDto, Long userId) {
        Rental rental = rentalMapper.toModel(requestDto);
        rental.setUser(getUserById(userId));
        Car car = getCarById(requestDto.carId());
        car.setInventory(car.getInventory() - 1);
        rental.setCar(car);
        carRepository.save(car);
        rentalRepository.save(rental);
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
    public RentalResponseDto getRental(Long rentalId) {
        return rentalMapper.toDto(getRentalById(rentalId));
    }

    @Override
    public RentalResponseDto actualReturnDate(Long rentalId) {
        Rental rental = getRentalById(rentalId);
        rental.setActualReturnDate(LocalDate.now());
        rental.setActive(false);

        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
        return rentalMapper.toDto(rentalRepository.save(rental));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Can`t find a user by id: " + userId));
    }

    private Rental getRentalById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Can`t find a rental by id: " + id));
    }

    private Car getCarById(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can`t find a car by id: " + carId));
    }
}
