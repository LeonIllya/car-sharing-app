package car.sharing.dto.payment.external;

import car.sharing.model.User;

public record PaymentResponseForTelegram(
        User user,
        String sessionId
) {
}
