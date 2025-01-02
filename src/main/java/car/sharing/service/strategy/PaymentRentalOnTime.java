package car.sharing.service.strategy;

import car.sharing.model.Rental;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PaymentRentalOnTime implements PaymentAmountService {

    @Override
    public BigDecimal calculateTotalAmountByRentalDays(BigDecimal dailyFee, Rental rental) {
        LocalDate rentalDate = rental.getRentalDate();
        LocalDate returnDate = rental.getActualReturnDate();

        long rentalDays = ChronoUnit.DAYS.between(rentalDate, returnDate);

        return dailyFee.multiply(BigDecimal.valueOf(rentalDays));
    }
}
