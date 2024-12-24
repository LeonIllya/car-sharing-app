package car.sharing.dto.rental;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RentalRequestDto(
        @NotNull(message = "Rental date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate rentalDate,
        @NotNull(message = "Return date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate returnDate,
        @NotNull
        Long carId
) {
}
