package car.sharing.dto.car;

import car.sharing.model.Car;
import java.math.BigDecimal;

public record CarDto(
        Long id,
        String model,
        String brand,
        Car.CarFrame carFrame,
        int inventory,
        BigDecimal dailyFee
) {
}
