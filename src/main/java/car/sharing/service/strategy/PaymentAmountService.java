package car.sharing.service.strategy;

import car.sharing.model.Rental;
import java.math.BigDecimal;

public interface PaymentAmountService {
    BigDecimal calculateTotalAmountByRentalDays(BigDecimal dailyFee, Rental rental);
}
