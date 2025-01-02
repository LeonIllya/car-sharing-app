package car.sharing.service.strategy;

import car.sharing.model.Rental;
import org.springframework.stereotype.Service;

@Service
public class PaymentStrategy {
    public PaymentAmountService getPaymentAmount(Rental rental) {
        if (rental.getReturnDate().equals(rental.getActualReturnDate())
                || rental.getActualReturnDate().isBefore(rental.getReturnDate())) {
            return new PaymentRentalOnTime();
        } else {
            return new PaymentRentalLate();
        }
    }
}
