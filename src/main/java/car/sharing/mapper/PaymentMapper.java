package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.payment.external.PaymentResponseDto;
import car.sharing.dto.payment.external.PaymentResponseForTelegram;
import car.sharing.model.Payment;
import car.sharing.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    PaymentResponseDto toDto(Payment payment);

    PaymentResponseForTelegram toTelegramDto(User user, String sessionId);
}
