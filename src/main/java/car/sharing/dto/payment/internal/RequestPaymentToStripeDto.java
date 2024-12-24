package car.sharing.dto.payment.internal;

import car.sharing.model.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RequestPaymentToStripeDto(
        @NotNull
        Payment.Type type,
        @NotNull
        @Positive
        Long rentalId
) {
}
