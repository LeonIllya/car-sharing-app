package car.sharing.service.strategy;

import car.sharing.model.Rental;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PaymentRentalLate implements PaymentAmountService {
    private static final Double FINE_MULTIPLIER = 1.5;

    @Override
    public BigDecimal calculateTotalAmountByRentalDays(BigDecimal dailyFee, Rental rental) {
        LocalDate rentalDate = rental.getRentalDate();
        LocalDate returnDate = rental.getReturnDate();
        LocalDate actualReturnDate = rental.getActualReturnDate();

        long rentalDays = ChronoUnit.DAYS.between(rentalDate, returnDate);
        long overdueDays = ChronoUnit.DAYS.between(returnDate, actualReturnDate);
        BigDecimal fine = dailyFee.multiply(BigDecimal.valueOf(overdueDays * FINE_MULTIPLIER));

        return dailyFee.multiply(BigDecimal.valueOf(rentalDays)).add(fine);
    }
}
