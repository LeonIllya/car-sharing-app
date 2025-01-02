package car.sharing.service;

import static car.sharing.model.Car.CarFrame.UNIVERSAL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import car.sharing.dto.payment.external.PaymentResponseDto;
import car.sharing.dto.payment.internal.DescriptionForStripeDto;
import car.sharing.dto.payment.internal.RequestPaymentToStripeDto;
import car.sharing.exception.EntityNotFoundException;
import car.sharing.mapper.PaymentMapper;
import car.sharing.mapper.impl.PaymentMapperImpl;
import car.sharing.model.Car;
import car.sharing.model.Payment;
import car.sharing.model.Rental;
import car.sharing.model.User;
import car.sharing.repository.car.CarRepository;
import car.sharing.repository.payment.PaymentRepository;
import car.sharing.repository.rental.RentalRepository;
import car.sharing.service.impl.PaymentServiceImpl;
import car.sharing.service.impl.StripeService;
import car.sharing.service.strategy.PaymentAmountService;
import car.sharing.service.strategy.PaymentStrategy;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
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

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    private static final int DEFAULT_CAR_INVENTORY = 100;
    private static final String DEFAULT_SESSION_ID = "sessionId";

    @Mock
    private PaymentRepository paymentRepository;
    @Spy
    private PaymentMapper paymentMapper = new PaymentMapperImpl();
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private PaymentStrategy paymentStrategy;
    @Mock
    private PaymentAmountService paymentAmountService;
    @Mock
    private StripeService stripeService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private Payment payment2;
    private Rental rental;
    private Rental rental2;
    private Car car;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setTelegramId(1L);

        car = new Car();
        car.setId(1L);
        car.setModel("S-Class");
        car.setBrand("Mercedes");
        car.setCarFrame(UNIVERSAL);
        car.setInventory(DEFAULT_CAR_INVENTORY);
        car.setDailyFee(BigDecimal.valueOf(100));

        rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(10));
        rental.setActualReturnDate(LocalDate.now().plusDays(10));
        rental.setCar(car);
        rental.setUser(user);

        rental2 = new Rental();
        rental2.setId(2L);
        rental2.setRentalDate(LocalDate.now());
        rental2.setReturnDate(LocalDate.now().plusDays(10));
        rental2.setActualReturnDate(LocalDate.now().plusDays(12));
        rental.setCar(car);
        rental.setUser(user);

        payment = new Payment();
        payment.setId(1L);
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRental(rental);
        payment.setSessionId("sessionId");
        payment.setTotalPrice(BigDecimal.valueOf(100));

        payment2 = new Payment();
        payment2.setId(2L);
        payment2.setStatus(Payment.Status.PENDING);
        payment2.setType(Payment.Type.PAYMENT);
        payment2.setRental(rental2);
        payment2.setSessionId("sessionId2");
        payment2.setTotalPrice(BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("Get a list of user's payments")
    public void getPayments_WhenPaymentsExist_ShouldReturnPaymentDtos() {
        // Given
        Long userId = 1L;
        List<Rental> rentals = List.of(rental, rental2);
        List<Payment> payments = List.of(payment, payment2);

        when(rentalRepository.findAllByUserId(userId)).thenReturn(rentals);
        when(paymentRepository.findAllByRentalsId(rentals.stream().map(Rental::getId).toList()))
                .thenReturn(payments);
        when(paymentMapper.toDto(any(Payment.class)))
                .thenReturn(createPaymentDto(payment), createPaymentDto(payment2));

        // When
        List<PaymentResponseDto> paymentsDtoExpected = List.of(
                createPaymentDto(payment), createPaymentDto(payment2));
        List<PaymentResponseDto> paymentsDtoActual = paymentService.getPayments(userId);

        // Then
        Assertions.assertEquals(paymentsDtoExpected, paymentsDtoActual);
        verify(rentalRepository, times(1)).findAllByUserId(userId);
        verify(paymentRepository, times(1))
                .findAllByRentalsId(rentals.stream().map(Rental::getId).toList());
    }

    @Test
    @DisplayName("Create a new payment")
    public void createPaymentSession_ValidRequestPaymentToStripeDto_ShouldReturnPaymentResponseDto()
            throws MalformedURLException {
        //Given
        Session stripeSession = new Session();
        stripeSession.setId("sessionId");
        stripeSession.setUrl("http://stripe.url");

        DescriptionForStripeDto descriptionForSession = new DescriptionForStripeDto();
        descriptionForSession.setName("Rental" + car.getModel() + car.getBrand());
        descriptionForSession.setDescription("This is a session for car rentals payment");
        descriptionForSession.setTotalAmount(BigDecimal.valueOf(1000));

        payment.setId(null);
        payment.setSessionUrl(new URL("http://stripe.url"));
        payment.setTotalPrice(BigDecimal.valueOf(1000));
        Long carId = 1L;

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(paymentStrategy.getPaymentAmount(rental)).thenReturn(paymentAmountService);
        when(paymentAmountService.calculateTotalAmountByRentalDays(
            rental.getCar().getDailyFee(), rental))
                .thenReturn(BigDecimal.valueOf(1000));
        when(stripeService.createStripeSession(descriptionForSession))
                .thenReturn(stripeSession);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        //When
        RequestPaymentToStripeDto stripeDto = new RequestPaymentToStripeDto(
                Payment.Type.PAYMENT, rental.getId());
        PaymentResponseDto paymentDto = createPaymentDto(payment);
        PaymentResponseDto paymentSessionActual = paymentService.createPaymentSession(stripeDto);

        //Then
        Assertions.assertEquals(paymentDto, paymentSessionActual);
        verify(rentalRepository, times(1)).findById(1L);
        verify(stripeService, times(1)).createStripeSession(descriptionForSession);
        verify(paymentRepository, times(1)).save(payment);
        verify(carRepository, times(2)).findById(carId);
    }

    @Test
    @DisplayName("Confirm a payment")
    public void successPayment_ValidSessionId_Success() {
        //Given
        when(paymentRepository.findBySessionId(DEFAULT_SESSION_ID))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        doNothing().when(notificationService).sendNotification(any(), any());

        //When
        paymentService.successPayment(DEFAULT_SESSION_ID);

        //Then
        Assertions.assertEquals(payment.getStatus(), Payment.Status.PAID);
        verify(paymentRepository, times(1)).findBySessionId(DEFAULT_SESSION_ID);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("Return exception")
    public void successPayment_InvalidSessionId_ShouldReturnEntityNotFoundException() {
        //Given
        when(paymentRepository.findBySessionId(DEFAULT_SESSION_ID)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.successPayment(DEFAULT_SESSION_ID)
        );

        //Then
        String expected = "Can`t find a session by id: " + DEFAULT_SESSION_ID;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(paymentRepository, times(1)).findBySessionId(DEFAULT_SESSION_ID);
    }

    @Test
    @DisplayName("Cancel a payment")
    public void canselPayment_ValidSessionIdAndUserId_Success() {
        //Given
        when(paymentRepository.findBySessionId(DEFAULT_SESSION_ID))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        doNothing().when(notificationService).sendNotification(any(), any());

        //When
        paymentService.cancelPayment(DEFAULT_SESSION_ID);

        //Then
        Assertions.assertEquals(payment.getStatus(), Payment.Status.CANCELED);
        verify(paymentRepository, times(1)).findBySessionId(DEFAULT_SESSION_ID);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("Return exception")
    public void canselPayment_InvalidSessionId_ShouldReturnEntityNotFoundException() {
        //Given
        when(paymentRepository.findBySessionId(DEFAULT_SESSION_ID)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.cancelPayment(DEFAULT_SESSION_ID)
        );

        //Then
        String expected = "Can`t find a session by id: " + DEFAULT_SESSION_ID;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verify(paymentRepository, times(1)).findBySessionId(DEFAULT_SESSION_ID);
    }

    private PaymentResponseDto createPaymentDto(Payment payment) {
        return new PaymentResponseDto(payment.getId(), payment.getStatus(), payment.getType(),
                payment.getSessionUrl(), payment.getSessionId(), payment.getTotalPrice());
    }
}
