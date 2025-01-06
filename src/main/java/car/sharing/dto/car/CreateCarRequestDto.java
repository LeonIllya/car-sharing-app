package car.sharing.dto.car;

import car.sharing.model.Car;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.hibernate.validator.constraints.Length;

public record CreateCarRequestDto(
        @NotBlank(message = "Please write the model of the car")
        @Length(min = 5, max = 30)
        String model,
        @NotBlank(message = "Please write the brand of the car")
        String brand,
        @NotNull
        Car.CarFrame carFrame,
        @Positive
        int inventory,
        @DecimalMin("0")
        BigDecimal dailyFee
) {
}
