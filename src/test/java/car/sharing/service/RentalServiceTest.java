package car.sharing.service;

import static car.sharing.model.Car.CarFrame.UNIVERSAL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import car.sharing.dto.rental.RentalRequestDto;
import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.dto.rental.RentalSearchParametersDto;
import car.sharing.exception.EntityNotFoundException;
import car.sharing.mapper.RentalMapper;
import car.sharing.mapper.impl.RentalMapperImpl;
import car.sharing.model.Car;
import car.sharing.model.Rental;
import car.sharing.model.User;
import car.sharing.repository.car.CarRepository;
import car.sharing.repository.rental.RentalRepository;
import car.sharing.repository.rental.RentalSpecificationBuilder;
import car.sharing.repository.user.UserRepository;
import car.sharing.service.impl.RentalServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {
    private static final int DEFAULT_CAR_INVENTORY = 100;
    private static final int CAR_INVENTORY_EXPECT = 99;
    private static final boolean RENTAL_IS_FINISHED = false;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private RentalMapper rentalMapper = new RentalMapperImpl();
    @Mock
    private CarRepository carRepository;
    @Mock
    private RentalSpecificationBuilder specificationBuilder;
    @InjectMocks
    private RentalServiceImpl rentalService;

    private User user;
    private Car car;
    private RentalRequestDto requestDto;
    private Rental rental;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("admin1@gmail.com");
        user.setFirstName("Danil");
        user.setLastName("Zinchenko");

        car = new Car();
        car.setId(1L);
        car.setModel("S-Class");
        car.setBrand("Mercedes");
        car.setCarFrame(UNIVERSAL);
        car.setInventory(DEFAULT_CAR_INVENTORY);
        car.setDailyFee(BigDecimal.valueOf(100));

        requestDto = new RentalRequestDto(LocalDate.now(),
                LocalDate.now().plusDays(5), 1L);

        rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(requestDto.rentalDate());
        rental.setReturnDate(requestDto.returnDate());
        rental.setActualReturnDate(null);
        rental.setCar(car);
        rental.setUser(user);
    }

    @Test
    @DisplayName("Save new rentals and return its DTO")
    public void addRental_ValidRental_ShouldReturnRentalResponseDto() {
        // Given
        when(rentalMapper.toModel(requestDto)).thenReturn(rental);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(carRepository.save(car)).thenReturn(car);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        RentalResponseDto responseDtoExpected = createRentalDto(rental);
        RentalResponseDto rentalActual = rentalService.createRental(requestDto, 1L);

        // Then
        Assertions.assertEquals(responseDtoExpected, rentalActual);
        Assertions.assertEquals(car.getInventory(), CAR_INVENTORY_EXPECT);
        verify(rentalRepository, times(1)).save(rental);
        verify(carRepository, times(1)).save(car);
    }

    @Test
    @DisplayName("Search rentals with valid params")
    public void search_ValidRentalsByParams_ShouldReturnListOfRentalsDto() {
        //Given
        Long[] usersId = new Long[] {1L, 2L};
        Boolean[] isActive = new Boolean[] {true, false};
        RentalSearchParametersDto parametersDto = new RentalSearchParametersDto(usersId, isActive);
        Specification<Rental> rentalSpecification = specificationBuilder.build(parametersDto);

        Rental rental1 = createRental();
        List<Rental> rentals = List.of(rental, rental1);

        RentalResponseDto rentalDto = createRentalDto(rental);
        RentalResponseDto rentalDto1 = createRentalDto(rental1);

        when(specificationBuilder.build(parametersDto)).thenReturn(rentalSpecification);
        when(rentalRepository.findAll(rentalSpecification)).thenReturn(rentals);
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(rentalDto, rentalDto1);

        List<RentalResponseDto> rentalResponseDtosExpected = List.of(rentalDto, rentalDto1);

        //When
        List<RentalResponseDto> rentalResponseDtosActual = rentalService.search(parametersDto);

        //Then
        Assertions.assertEquals(rentalResponseDtosActual, rentalResponseDtosExpected);
    }

    @Test
    @DisplayName("Search rentals with invalid params")
    public void search_InvalidRentalsParams_ShouldReturnsEmptyList() {
        //Given
        Long[] usersId = new Long[] {100L, 200L};
        Boolean[] isActive = new Boolean[] {true, false};
        RentalSearchParametersDto parametersDto = new RentalSearchParametersDto(usersId, isActive);
        Specification<Rental> rentalSpecification = specificationBuilder.build(parametersDto);

        Rental rental1 = createRental();
        List<Rental> rentals = List.of(rental, rental1);

        RentalResponseDto rentalDto = createRentalDto(rental);
        RentalResponseDto rentalDto1 = createRentalDto(rental1);

        when(specificationBuilder.build(parametersDto)).thenReturn(rentalSpecification);
        when(rentalRepository.findAll(rentalSpecification)).thenReturn(rentals);
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(rentalDto, rentalDto1);

        //When
        List<RentalResponseDto> rentalResponseDtosActual = rentalService.search(parametersDto);

        //Then
        assertFalse(rentalResponseDtosActual.isEmpty());
    }

    @Test
    @DisplayName("Find rental by ID and return rental DTO")
    public void findRentalById_ValidRental_ShouldReturnRentalResponseDto() {
        // Given
        RentalResponseDto rentalDto = createRentalDto(rental);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        // When
        RentalResponseDto rentalByIdExpected = rentalService.getRental(1L);

        // Then
        Assertions.assertEquals(rentalDto, rentalByIdExpected);
        verify(rentalRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Find rental by ID and throw exception if not found")
    public void findRentalById_InvalidRental_ShouldReturnEntityNotFoundException() {
        //Given
        Long rentalId = 100L;

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.getRental(rentalId)
        );

        //Then
        String expected = "Can`t find a rental by id: " + rentalId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(rentalRepository, times(1)).findById(100L);
    }

    @Test
    @DisplayName("Add actual return date to rental")
    public void addActualReturnDate_ValidRental_ShouldReturnRentalResponseDto() {
        //Given
        rental.setActualReturnDate(LocalDate.now().plusDays(10));
        car.setInventory(CAR_INVENTORY_EXPECT);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(carRepository.save(car)).thenReturn(car);
        when(rentalRepository.save(rental)).thenReturn(rental);

        //When
        rentalService.actualReturnDate(1L);

        //Then
        Assertions.assertEquals(car.getInventory(), DEFAULT_CAR_INVENTORY);
        Assertions.assertEquals(rental.isActive(), RENTAL_IS_FINISHED);
        verify(rentalRepository,times(1)).findById(1L);
        verify(rentalRepository, times(1)).save(rental);
    }

    @Test
    @DisplayName("Should throw exception when rentals is not active")
    public void addActualReturnDate_InvalidRental_ShouldReturnEntityNotFoundException() {
        // Given
        Long rentalId = 100L;

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.actualReturnDate(rentalId)
        );

        //Then
        String expected = "Can`t find a rental by id: " + rentalId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(rentalRepository, times(1)).findById(100L);
    }

    private Rental createRental() {
        Rental rental = new Rental();
        rental.setId(2L);
        rental.setRentalDate(LocalDate.now().plusDays(2));
        rental.setReturnDate(LocalDate.now().plusDays(6));
        rental.setActualReturnDate(LocalDate.now().plusDays(8));
        rental.setCar(car);
        rental.setUser(user);
        return rental;
    }

    private RentalResponseDto createRentalDto(Rental rental) {
        return new RentalResponseDto(rental.getId(), rental.getRentalDate(),
            rental.getReturnDate(), rental.getActualReturnDate(), car.getId());
    }
}
