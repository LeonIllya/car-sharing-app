package car.sharing.dto.payment.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class DescriptionForStripeDto {
    @NotNull
    @Positive
    private BigDecimal totalAmount;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
}
