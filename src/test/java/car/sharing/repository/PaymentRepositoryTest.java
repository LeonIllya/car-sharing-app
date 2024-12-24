package car.sharing.repository;

import car.sharing.model.Payment;
import car.sharing.repository.payment.PaymentRepository;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @Sql(scripts = "classpath:database/payments/add-payments.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/payments/remove-payments.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Find list payments by list rental ids")
    public void convertListRentalsId_intoListOfPayments() throws MalformedURLException {
        // Given
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.PAYMENT);
        payment.setRentalId(1L);
        payment.setSessionUrl(new URL("http://stripe1.url"));
        payment.setSessionId("sessionId1");
        payment.setTotalPrice(BigDecimal.valueOf(1000));

        Payment payment2 = new Payment();
        payment2.setId(2L);
        payment2.setStatus(Payment.Status.PENDING);
        payment2.setType(Payment.Type.PAYMENT);
        payment2.setRentalId(2L);
        payment2.setSessionUrl(new URL("http://stripe2.url"));
        payment2.setSessionId("sessionId2");
        payment2.setTotalPrice(BigDecimal.valueOf(2000));

        List<Payment> listOfPaymentsExpected = List.of(payment, payment2);

        //When
        List<Long> rentals = List.of(1L, 2L);
        List<Payment> listPaymentsActual = paymentRepository.findAllByRentalsId(rentals);

        //Then
        Assertions.assertEquals(2, listPaymentsActual.size());
        Assertions.assertEquals(listOfPaymentsExpected, listPaymentsActual);
    }
}
