package car.sharing.service;

import car.sharing.dto.payment.external.PaymentResponseDto;
import car.sharing.dto.payment.external.PaymentResponseForTelegram;
import car.sharing.dto.payment.internal.RequestPaymentToStripeDto;
import java.util.List;

public interface PaymentService {
    List<PaymentResponseDto> getPayments(Long userId);

    PaymentResponseDto createPaymentSession(RequestPaymentToStripeDto stripeDto);

    PaymentResponseForTelegram successPayment(String sessionId);

    PaymentResponseForTelegram cancelPayment(String sessionId);
}
