package car.sharing.dto.payment.external;

import car.sharing.model.Payment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.net.URL;

public record PaymentRequestDto(
        @NotBlank
        Payment.Type type,
        @NotBlank
        Long rentalId,
        URL sessionUrl,
        @NotBlank
        String sessionId,
        @NotBlank
        @Positive
        Long totalPrice
) {
}
