package car.sharing.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import car.sharing.dto.payment.external.PaymentResponseDto;
import car.sharing.dto.payment.internal.RequestPaymentToStripeDto;
import car.sharing.model.Payment;
import car.sharing.service.NotificationService;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

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
//    @MockBean
//    private NotificationService notificationService;

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
        PaymentResponseDto paymentResponseDtoExpected = createPaymentResponseDto();
        //When
        MvcResult result = mockMvc.perform(get("/payments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        PaymentResponseDto[] paymentResponseDtoActual = objectMapper.readValue(
            result.getResponse().getContentAsString(), PaymentResponseDto[].class);
        Assertions.assertNotNull(paymentResponseDtoActual);
        EqualsBuilder.reflectionEquals(List.of(paymentResponseDtoExpected),
                Arrays.stream(paymentResponseDtoActual).toList(), "id");
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER", "MANAGER"})
    @DisplayName("Create session")
    void createSession_ValidRequestPaymentToStripeDto_ShouldReturnPaymentResponseDto()
            throws Exception {
        //Given
        RequestPaymentToStripeDto requestPaymentToStripeDto = createRequestPaymentToStripeDto();
        PaymentResponseDto paymentResponseDtoExpected = createPaymentResponseDto();
        String jsonRequest = objectMapper.writeValueAsString(requestPaymentToStripeDto);
        //When
        MvcResult result = mockMvc.perform(post("/payments")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        PaymentResponseDto responseDtoActual = objectMapper.readValue(
                result.getResponse().getContentAsString(), PaymentResponseDto.class);

        Assertions.assertNotNull(responseDtoActual);
        EqualsBuilder.reflectionEquals(paymentResponseDtoExpected,
                responseDtoActual, "id");
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Get success message by session id")
    void getSuccessMessageBySessionId_ValidSessionId_ShouldReturnString() throws Exception {
        //Given
        String sessionId = "sessionId1";
        String requestUrl = UriComponentsBuilder.fromUriString("/payments/success")
                .queryParam("sessionId", sessionId)
                .toUriString();

        //When
//        doNothing().when(notificationService).sendNotification(anyString(), anyLong());
        MvcResult result = mockMvc.perform(get(requestUrl))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        String messageExpected = "Payment is successful";
        EqualsBuilder.reflectionEquals(messageExpected, result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Get cancel message by session id")
    void getCancelMessageBySessionId_ValidSessionId_ShouldReturnString() throws Exception {
        //Given
        String sessionId = "sessionId1";
        String requestUrl = UriComponentsBuilder.fromUriString("/payments/cancel")
                .queryParam("sessionId", sessionId)
                .toUriString();

        //When
//        doNothing().when(notificationService).sendNotification(anyString(), anyLong());
        MvcResult result = mockMvc.perform(get(requestUrl))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        String messageExpected = "Payment is cancel";
        EqualsBuilder.reflectionEquals(messageExpected, result.getResponse().getContentAsString());
    }

    private PaymentResponseDto createPaymentResponseDto() throws MalformedURLException {
        return new PaymentResponseDto(1L, Payment.Status.PENDING,
                Payment.Type.PAYMENT, new URL("http://stripe1.url"),
                "sessionId1", new BigDecimal("1000.0").setScale(2));
    }

    private RequestPaymentToStripeDto createRequestPaymentToStripeDto() {
        return new RequestPaymentToStripeDto(Payment.Type.PAYMENT, 3L);
    }
}
