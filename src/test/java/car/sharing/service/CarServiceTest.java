package car.sharing.service;

import static car.sharing.model.Car.CarFrame.SEDAN;
import static car.sharing.model.Car.CarFrame.UNIVERSAL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import car.sharing.dto.car.CarDto;
import car.sharing.dto.car.CarSearchParametersDto;
import car.sharing.dto.car.CreateCarRequestDto;
import car.sharing.exception.EntityNotFoundException;
import car.sharing.mapper.CarMapper;
import car.sharing.mapper.impl.CarMapperImpl;
import car.sharing.model.Car;
import car.sharing.repository.car.CarRepository;
import car.sharing.repository.car.CarSpecificationBuilder;
import car.sharing.service.impl.CarServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    @Mock
    private CarRepository carRepository;
    @Spy
    private CarMapper carMapper = new CarMapperImpl();
    @Mock
    private CarSpecificationBuilder specificationBuilder;
    @InjectMocks
    private CarServiceImpl carService;

    private CreateCarRequestDto requestDto;
    private Car car;

    @BeforeEach
    void setUp() {
        requestDto = new CreateCarRequestDto("S-Class", "Mercedes",
                UNIVERSAL, 100, BigDecimal.valueOf(100));

        car = new Car();
        car.setId(1L);
        car.setModel(requestDto.model());
        car.setBrand(requestDto.brand());
        car.setCarFrame(requestDto.carFrame());
        car.setInventory(requestDto.inventory());
        car.setDailyFee(requestDto.dailyFee());
    }

    @Test
    @DisplayName("Save new car in database and return carDto")
    public void saveCar_ValidCreateCarRequestDto_ShouldReturnsCarDto() {
        // Given
        CarDto carDtoResponse = createCarDto(car);

        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(carDtoResponse);

        // When
        CarDto carDtoActual = carService.createCar(requestDto);

        // Then
        Assertions.assertEquals(carDtoResponse, carDtoActual);
        verify(carRepository, times(1)).save(car);
    }

    @Test
    @DisplayName("Update car in database by ID and return carDto")
    public void updateCarById_ValidCreateCarRequestDto_Success() {
        // Given
        int updateInventory = 50;
        car.setInventory(updateInventory);

        CreateCarRequestDto updateRequestDto = new CreateCarRequestDto(requestDto.model(),
                requestDto.brand(), requestDto.carFrame(), updateInventory, requestDto.dailyFee());

        CarDto carDtoResponse = createCarDto(car);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(carDtoResponse);

        // When
        CarDto carDtoActual = carService.updateCarById(1L, updateRequestDto);

        // Then
        Assertions.assertEquals(carDtoResponse, carDtoActual);
        verify(carRepository, times(1)).findById(1L);
        verify(carRepository, times(1)).save(car);
    }

    @Test
    @DisplayName("Can`t update a car by ID")
    public void updateCarById_InvalidBook_ShouldReturnEntityNotFoundException() {
        // Given
        int updateInventory = 50;
        CreateCarRequestDto updateRequestDto = new CreateCarRequestDto(requestDto.model(),
                requestDto.brand(), requestDto.carFrame(),
                updateInventory, requestDto.dailyFee());
        Long carId = 100L;

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> carService.updateCarById(carId, updateRequestDto)
        );

        // Then
        String expected = "Can't find a car by id: " + carId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Delete car by ID")
    public void deleteCarById_ValidId_Success() {
        // When
        carService.deleteCarById(1L);

        // Then
        verify(carRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Find car by ID and return carDto")
    public void findCarById_ValidCar_ShouldReturnCarDto() {
        // Given
        CarDto carDtoResponse = createCarDto(car);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(carDtoResponse);

        // When
        CarDto carDtoExpected = carService.findCarById(1L);

        // Then
        Assertions.assertEquals(carDtoResponse, carDtoExpected);
        verify(carRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Find car by ID and throw exception if not found")
    public void findCarById_InvalidCar_ShouldReturnEntityNotFoundException() {
        //Given
        Long carId = 100L;

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> carService.findCarById(carId)
        );

        //Then
        String expected = "Can`t find a car by id: " + carId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(carRepository, times(1)).findById(100L);
    }

    @Test
    @DisplayName("Find all cars and return list of cars DTO")
    public void findAllCars_ValidCars_ShouldReturnListOfCarsDto() {
        // Given
        CarDto carDtoResponse = createCarDto(car);

        Car car2 = createCar();
        CarDto carDtoResponse2 = createCarDto(car2);

        Page<Car> carPage = new PageImpl<>(List.of(car, car2));
        PageRequest defaultPageable = PageRequest.of(0, 5);

        when(carRepository.findAll(defaultPageable)).thenReturn(carPage);
        when(carMapper.toDto(any(Car.class))).thenReturn(carDtoResponse, carDtoResponse2);

        // When
        List<CarDto> expectedCars = carService.findAllCars(defaultPageable);

        // Then
        Assertions.assertEquals(2, expectedCars.size());
        Assertions.assertEquals(carDtoResponse, expectedCars.get(0));
        Assertions.assertEquals(carDtoResponse2, expectedCars.get(1));
    }

    @Test
    @DisplayName("Search cars with valid params")
    public void search_ValidCarsByParams_ShouldReturnListOfCarsDto() {
        // Given
        String[] brands = new String[] {"Toyota", "Mercedes"};
        String[] carFrames = new String[] {"SEDAN", "UNIVERSAL"};
        CarSearchParametersDto parametersDto = new CarSearchParametersDto(brands, carFrames);
        Specification<Car> carSpecification = specificationBuilder.build(parametersDto);

        Car car2 = createCar();
        List<Car> cars = List.of(car, car2);

        CarDto carDto = createCarDto(car);
        CarDto carDto2 = createCarDto(car2);

        when(specificationBuilder.build(parametersDto)).thenReturn(carSpecification);
        when(carRepository.findAll(carSpecification)).thenReturn(cars);
        when(carMapper.toDto(any(Car.class))).thenReturn(carDto, carDto2);

        List<CarDto> carDtoExpected = List.of(carDto, carDto2);

        // When
        List<CarDto> carDtoActual = carService.search(parametersDto);

        // Then
        Assertions.assertEquals(carDtoExpected, carDtoActual);
    }

    @Test
    @DisplayName("Search books with invalid params")
    public void search_InvalidCarsParams_ShouldReturnsEmptyList() {
        //Given
        String[] brands = new String[] {"InvalidBrand"};
        String[] carFrames = new String[] {"InvalidCarFrames"};
        CarSearchParametersDto parametersDto = new CarSearchParametersDto(brands, carFrames);
        Specification<Car> carSpecification = specificationBuilder.build(parametersDto);

        Car car2 = createCar();
        List<Car> cars = List.of(car, car2);

        CarDto carDto = createCarDto(car);
        CarDto carDto2 = createCarDto(car2);

        when(specificationBuilder.build(parametersDto)).thenReturn(carSpecification);
        when(carRepository.findAll(carSpecification)).thenReturn(cars);
        when(carMapper.toDto(any(Car.class))).thenReturn(carDto, carDto2);

        //When
        List<CarDto> carDtoActual = carService.search(parametersDto);

        //Then
        assertFalse(carDtoActual.isEmpty());
    }

    private Car createCar() {
        Car car = new Car();
        car.setId(2L);
        car.setModel("Corolla");
        car.setBrand("Toyota");
        car.setCarFrame(SEDAN);
        car.setInventory(10);
        car.setDailyFee(BigDecimal.valueOf(30));
        return car;
    }

    private CarDto createCarDto(Car car) {
        return new CarDto(car.getId(), car.getModel(),
            car.getBrand(), car.getCarFrame(), car.getInventory(), car.getDailyFee());
    }
}
