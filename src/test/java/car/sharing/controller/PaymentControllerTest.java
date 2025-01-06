package car.sharing.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import car.sharing.dto.payment.external.PaymentResponseDto;
import car.sharing.model.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Sql(scripts = {"classpath:database/cars/add-cars.sql",
        "classpath:database/users/add-users.sql",
        "classpath:database/rentals/add-rentals.sql",
        "classpath:database/payments/add-payments.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:database/cars/remove-cars.sql",
        "classpath:database/users/remove-users.sql",
        "classpath:database/rentals/remove-rentals.sql",
        "classpath:database/payments/remove-payments.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentControllerTest {

    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext webContext) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webContext)
            .apply(springSecurity())
            .build();
    }

    @Test
    @WithUserDetails("eto@gmail.com")
    @DisplayName("Get payment by user")
    void getPaymentByUser_ValidUser_ShouldReturnListOfPaymentResponseDto() throws Exception {
        //Given
        List<PaymentResponseDto> paymentResponseDtoExpected = createPaymentResponseDto();
        //When
        MvcResult result = mockMvc.perform(get("/payments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        PaymentResponseDto[] paymentResponseDtoActual = objectMapper.readValue(
            result.getResponse().getContentAsString(), PaymentResponseDto[].class);
        Assertions.assertNotNull(paymentResponseDtoActual);
        assertEquals(paymentResponseDtoExpected, Arrays.stream(paymentResponseDtoActual).toList());
    }

    private List<PaymentResponseDto> createPaymentResponseDto() throws MalformedURLException {
        PaymentResponseDto responseDto = new PaymentResponseDto(1L, Payment.Status.PENDING,
                Payment.Type.PAYMENT, new URL("http://stripe1.url"),
                "sessionId1", new BigDecimal("1000.0").setScale(2));
        return List.of(responseDto);
    }
}
