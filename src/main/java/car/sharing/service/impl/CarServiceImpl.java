package car.sharing.service.impl;

import car.sharing.dto.car.CarDto;
import car.sharing.dto.car.CarSearchParametersDto;
import car.sharing.dto.car.CreateCarRequestDto;
import car.sharing.exception.EntityNotFoundException;
import car.sharing.mapper.CarMapper;
import car.sharing.model.Car;
import car.sharing.repository.car.CarRepository;
import car.sharing.repository.car.CarSpecificationBuilder;
import car.sharing.service.CarService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarSpecificationBuilder specificationBuilder;

    @Override
    @Transactional
    public CarDto createCar(CreateCarRequestDto requestDto) {
        Car car = carMapper.toModel(requestDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    @Transactional
    public CarDto updateCarById(Long id, CreateCarRequestDto requestDto) {
        Car car = carRepository.findById(id).orElseThrow(()
                -> new EntityNotFoundException("Can't find a car by id: " + id));
        car.setId(id);
        car.setModel(requestDto.model());
        car.setBrand(requestDto.brand());
        car.setCarFrame(requestDto.carFrame());
        car.setInventory(requestDto.inventory());
        car.setDailyFee(requestDto.dailyFee());
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    @Transactional
    public void deleteCarById(Long id) {
        carRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CarDto findCarById(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can`t find a car by id: " + carId));
        return carMapper.toDto(car);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarDto> findAllCars(Pageable pageable) {
        return carRepository.findAll(pageable)
                .stream()
                .map(carMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarDto> search(CarSearchParametersDto params) {
        Specification<Car> carSpecification = specificationBuilder.build(params);
        return carRepository.findAll(carSpecification)
                .stream()
                .map(carMapper::toDto)
                .toList();
    }
}
