package car.sharing.service.impl;

import car.sharing.dto.payment.external.PaymentResponseDto;
import car.sharing.dto.payment.internal.DescriptionForStripeDto;
import car.sharing.dto.payment.internal.RequestPaymentToStripeDto;
import car.sharing.exception.EntityNotFoundException;
import car.sharing.mapper.PaymentMapper;
import car.sharing.model.Car;
import car.sharing.model.Payment;
import car.sharing.model.Rental;
import car.sharing.model.User;
import car.sharing.repository.car.CarRepository;
import car.sharing.repository.payment.PaymentRepository;
import car.sharing.repository.rental.RentalRepository;
import car.sharing.repository.user.UserRepository;
import car.sharing.service.NotificationService;
import car.sharing.service.PaymentService;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Double FINE_MULTIPLIER = 1.5;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final StripeService stripeService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPayments(Long userId) {
        List<Rental> userRentals = rentalRepository.findAllByUserId(userId);
        List<Payment> paymentsByUser = paymentRepository.findAllByRentalsId(
                userRentals.stream().map(Rental::getId).toList());
        return paymentsByUser.stream().map(paymentMapper::toDto).toList();
    }

    @Override
    @Transactional
    public PaymentResponseDto createPaymentSession(RequestPaymentToStripeDto stripeDto) {
        Rental rental = getRentalById(stripeDto.rentalId());
        BigDecimal totalPrice = calculateTotalAmount(rental);

        DescriptionForStripeDto descriptionForSession = createDescriptionForSession(
                totalPrice, rental);
        Session stripeSession = stripeService.createStripeSession(descriptionForSession);
        Payment payment = savePayment(totalPrice, stripeSession, rental, stripeDto);
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional
    public void successPayment(String sessionId) {
        Payment payment = getSessionById(sessionId);
        Rental rental = getRentalById(payment.getRentalId());
        User user = getUserById(rental.getUserId());
        payment.setStatus(Payment.Status.PAID);
        paymentRepository.save(payment);
        if (user.getTelegramId() != null) {
            notificationService.sendNotification("Payment with session id {} "
                + " is successful." + sessionId, user.getTelegramId());
        }
        log.info("Payment with session id {} is successful.", sessionId);
    }

    @Override
    @Transactional
    public void cancelPayment(String sessionId) {
        Payment payment = getSessionById(sessionId);
        Rental rental = getRentalById(payment.getRentalId());
        User user = getUserById(rental.getUserId());
        payment.setStatus(Payment.Status.CANCELED);
        paymentRepository.save(payment);
        if (user.getTelegramId() != null) {
            notificationService.sendNotification("Payment with session id {} "
                + " is canceled." + sessionId, user.getTelegramId());
        }
        log.info("Payment with session id {} is canceled.", sessionId);
    }

    private BigDecimal calculateTotalAmount(Rental rental) {
        BigDecimal dailyFee = getCarById(rental.getCarId()).getDailyFee();
        if (rental.getReturnDate().equals(rental.getActualReturnDate())
                || rental.getActualReturnDate().isBefore(rental.getReturnDate())) {
            return getTotalAmountByRentalDays(dailyFee, rental);
        } else {
            return getTotalAmountByRentalDaysWithOverdue(dailyFee, rental);
        }
    }

    private BigDecimal getTotalAmountByRentalDays(BigDecimal dailyFee, Rental rental) {
        LocalDate rentalDate = rental.getRentalDate();
        LocalDate returnDate = rental.getActualReturnDate();

        long rentalDays = ChronoUnit.DAYS.between(rentalDate, returnDate);

        return dailyFee.multiply(BigDecimal.valueOf(rentalDays));
    }

    private BigDecimal getTotalAmountByRentalDaysWithOverdue(BigDecimal dailyFee, Rental rental) {
        LocalDate rentalDate = rental.getRentalDate();
        LocalDate returnDate = rental.getReturnDate();
        LocalDate actualReturnDate = rental.getActualReturnDate();

        long rentalDays = ChronoUnit.DAYS.between(rentalDate, returnDate);
        long overdueDays = ChronoUnit.DAYS.between(returnDate, actualReturnDate);
        BigDecimal fine = dailyFee.multiply(BigDecimal.valueOf(overdueDays * FINE_MULTIPLIER));

        return dailyFee.multiply(BigDecimal.valueOf(rentalDays)).add(fine);
    }

    private DescriptionForStripeDto createDescriptionForSession(
            BigDecimal totalPrice, Rental rental) {
        DescriptionForStripeDto description = new DescriptionForStripeDto();
        description.setTotalAmount(totalPrice);
        description.setName("Rental" + getCarById(rental.getCarId()).getModel()
                + getCarById(rental.getCarId()).getBrand());
        description.setDescription("This is a session for car rentals payment");
        return description;
    }

    private Payment savePayment(BigDecimal totalAmount, Session session, Rental rental,
                                    RequestPaymentToStripeDto stripeDto) {
        Payment payment = new Payment();
        payment.setType(stripeDto.type());
        payment.setRentalId(rental.getId());
        payment.setTotalPrice(totalAmount);

        payment.setSessionId(session.getId());
        try {
            payment.setSessionUrl(new URL(session.getUrl()));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid session URL: " + session.getUrl(), e);
        }
        paymentRepository.save(payment);
        return payment;
    }

    private Car getCarById(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can`t find a car by id: " + carId));
    }

    private Rental getRentalById(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can`t find a rental by id: " + rentalId));
    }

    private Payment getSessionById(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can`t find a session by id: " + sessionId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can`t find a user by id"));
    }
}
