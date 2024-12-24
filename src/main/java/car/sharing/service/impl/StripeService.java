package car.sharing.service.impl;

import car.sharing.dto.payment.internal.DescriptionForStripeDto;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {
    private static final String SUCCESS_URL = "http://localhost:8080/payments/success?sessionId={CHECKOUT_SESSION_ID}";
    private static final String CANCEL_URL = "http://localhost:8080/payments/cancel?sessionId={CHECKOUT_SESSION_ID}";
    private static final Long DEFAULT_QUANTITY = 1L;
    private static final String DEFAULT_CURRENCY = "USD";
    private static final BigDecimal CENTS_AMOUNT = BigDecimal.valueOf(100);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Session createStripeSession(DescriptionForStripeDto stripeDto) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(SUCCESS_URL)
                    .setCancelUrl(CANCEL_URL)
                    .addLineItem(
                        SessionCreateParams.LineItem.builder()
                            .setQuantity(DEFAULT_QUANTITY)
                            .setPriceData(SessionCreateParams.LineItem.PriceData
                                .builder()
                                .setCurrency(DEFAULT_CURRENCY)
                                .setUnitAmountDecimal(
                                    stripeDto.getTotalAmount().multiply(CENTS_AMOUNT)
                                )
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(stripeDto.getName())
                                        .setDescription(stripeDto.getDescription())
                                        .build()
                                )
                                .build()
                            )
                            .build()
                    )
                    .build();
            return Session.create(params);
        } catch (RuntimeException | StripeException e) {
            throw new RuntimeException("Can`t create a session: " + e);
        }
    }
}
