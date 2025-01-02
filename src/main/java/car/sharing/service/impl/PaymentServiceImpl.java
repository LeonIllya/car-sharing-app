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
import car.sharing.service.NotificationService;
import car.sharing.service.PaymentService;
import car.sharing.service.strategy.PaymentAmountService;
import car.sharing.service.strategy.PaymentStrategy;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final StripeService stripeService;
    private final NotificationService notificationService;
    private final PaymentStrategy paymentStrategy;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPayments(Long userId) {
        List<Rental> userRentals = rentalRepository.findAllByUserId(userId);
        List<Payment> paymentsByUser = paymentRepository.findAllByRentalsId(
                userRentals.stream().map(Rental::getId).toList());
        return paymentsByUser.stream().map(paymentMapper::toDto).toList();
    }

    @Override
    public PaymentResponseDto createPaymentSession(RequestPaymentToStripeDto stripeDto) {
        Rental rental = getRentalById(stripeDto.rentalId());
        PaymentAmountService paymentAmount = paymentStrategy.getPaymentAmount(rental);
        BigDecimal totalPrice = paymentAmount.calculateTotalAmountByRentalDays(
                rental.getCar().getDailyFee(), rental);

        DescriptionForStripeDto descriptionForSession = createDescriptionForSession(
                totalPrice, rental);
        Session stripeSession = stripeService.createStripeSession(descriptionForSession);
        Payment payment = savePayment(totalPrice, stripeSession, rental, stripeDto);
        return paymentMapper.toDto(payment);
    }

    @Override
    public void successPayment(String sessionId) {
        Payment payment = getSessionById(sessionId);
        Rental rental = payment.getRental();
        User user = rental.getUser();
        payment.setStatus(Payment.Status.PAID);
        paymentRepository.save(payment);
        if (user.getTelegramId() != null) {
            notificationService.sendNotification("Payment with session id {} "
                    + " is successful." + sessionId, user.getTelegramId());
        }
        log.info("Payment with session id {} is successful.", sessionId);
    }

    @Override
    public void cancelPayment(String sessionId) {
        Payment payment = getSessionById(sessionId);
        Rental rental = payment.getRental();
        User user = rental.getUser();
        payment.setStatus(Payment.Status.CANCELED);
        paymentRepository.save(payment);
        if (user.getTelegramId() != null) {
            notificationService.sendNotification("Payment with session id {} "
                    + " is canceled." + sessionId, user.getTelegramId());
        }
        log.info("Payment with session id {} is canceled.", sessionId);
    }

    private DescriptionForStripeDto createDescriptionForSession(
            BigDecimal totalPrice, Rental rental) {
        DescriptionForStripeDto description = new DescriptionForStripeDto();
        description.setTotalAmount(totalPrice);
        description.setName("Rental" + getCarById(rental.getCar().getId()).getModel()
                + getCarById(rental.getCar().getId()).getBrand());
        description.setDescription("This is a session for car rentals payment");
        return description;
    }

    private Payment savePayment(BigDecimal totalAmount, Session session, Rental rental,
                                    RequestPaymentToStripeDto stripeDto) {
        Payment payment = new Payment();
        payment.setType(stripeDto.type());
        payment.setRental(rental);
        payment.setSessionId(session.getId());
        payment.setTotalPrice(totalAmount);
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
}
