package car.sharing.dto.payment.external;

import car.sharing.model.Payment;
import java.math.BigDecimal;
import java.net.URL;

public record PaymentResponseDto(
        Long id,
        Payment.Status status,
        Payment.Type type,
        URL sessionUrl,
        String sessionId,
        BigDecimal totalPrice
) {
}
