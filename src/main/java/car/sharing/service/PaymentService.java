package car.sharing.service;

import car.sharing.dto.payment.external.PaymentResponseDto;
import car.sharing.dto.payment.internal.RequestPaymentToStripeDto;
import java.util.List;

public interface PaymentService {
    List<PaymentResponseDto> getPayments(Long userId);

    PaymentResponseDto createPaymentSession(RequestPaymentToStripeDto stripeDto);

    void successPayment(String sessionId);

    void cancelPayment(String sessionId);
}
