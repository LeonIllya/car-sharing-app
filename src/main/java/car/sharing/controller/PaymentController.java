package car.sharing.controller;

import car.sharing.dto.payment.external.PaymentResponseDto;
import car.sharing.dto.payment.external.PaymentResponseForTelegram;
import car.sharing.dto.payment.internal.RequestPaymentToStripeDto;
import car.sharing.model.User;
import car.sharing.service.NotificationService;
import car.sharing.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment management", description = "Endpoints for managing payments")
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/payments")
public class PaymentController {
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "Get payments", description = "Get users payments")
    public List<PaymentResponseDto> getPayments(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return paymentService.getPayments(user.getId());
    }

    @PostMapping
    @Operation(summary = "Create a payment session",
            description = "Create a payment session to work with Stripe")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDto createSession(@Valid @RequestBody
                                                    RequestPaymentToStripeDto stripeDto) {
        return paymentService.createPaymentSession(stripeDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/success")
    @Operation(summary = "Stripe redirection for successful payments",
            description = "Check successful Stripe payments")
    public String checkSuccess(@NotBlank String sessionId) {
        PaymentResponseForTelegram messageForTelegram = paymentService.successPayment(sessionId);
        if (messageForTelegram.user().getTelegramId() != null) {
            notificationService.sendNotification("Payment with session id {} "
                    + " is successful." + sessionId, messageForTelegram.user().getTelegramId());
        }
        return "Payment is successful";
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/cancel")
    @Operation(summary = "Stripe redirection for canceled payments",
            description = "Return payment paused message")
    public String checkCancel(@NotBlank String sessionId) {
        PaymentResponseForTelegram messageForTelegram = paymentService.cancelPayment(sessionId);
        if (messageForTelegram.user().getTelegramId() != null) {
            notificationService.sendNotification("Payment with session id {} "
                    + " is canceled." + sessionId, messageForTelegram.user().getTelegramId());
        }
        return "Payment is cancel";
    }
}
