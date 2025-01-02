package car.sharing.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import car.sharing.dto.rental.RentalRequestDto;
import car.sharing.dto.rental.RentalResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
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
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@Sql(scripts = {
        "classpath:database/cars/add-cars.sql",
        "classpath:database/users/add-users.sql",
        "classpath:database/rentals/add-rentals.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/cars/remove-cars.sql",
        "classpath:database/users/remove-users.sql",
        "classpath:database/rentals/remove-rentals.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RentalControllerTest {
    protected static MockMvc mockMvc;
    private static final Long EXISTING_RENTAL_ID_FROM_DB = 1L;
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
    @WithUserDetails("messi@gmail.com")
    @DisplayName("Create a new rental")
    void createRental_ValidRentalRequestDto_ShouldReturnRentalResponseDto() throws Exception {
        //Given
        RentalRequestDto rentalRequestDto = createRentalRequestDto();
        RentalResponseDto rentalResponseDtoExpected = createRentalResponseDto(rentalRequestDto);
        String jsonRequest = objectMapper.writeValueAsString(rentalRequestDto);

        //When
        MvcResult result = mockMvc.perform(post("/rentals")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        RentalResponseDto rentalResponseDtoActual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), RentalResponseDto.class);
        assertNotNull(rentalResponseDtoActual);
        EqualsBuilder.reflectionEquals(rentalResponseDtoExpected, rentalResponseDtoActual, "id");
    }

    @Test
    @WithUserDetails("messi@gmail.com")
    @DisplayName("Get a rental by id")
    void getRentalById_ValidRentalId_ShouldReturnRentalResponseDto() throws Exception {
        //Given
        RentalRequestDto rentalRequestDto = new RentalRequestDto(
                LocalDate.of(2024,12,5),
                LocalDate.of(2024, 12, 10), 1L);
        RentalResponseDto rentalResponseDtoExpected = createRentalResponseDto(rentalRequestDto);
        String jsonRequest = objectMapper.writeValueAsString(rentalRequestDto);

        //When
        MvcResult result = mockMvc.perform(get("/rentals/{id}", EXISTING_RENTAL_ID_FROM_DB)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        RentalResponseDto rentalResponseDtoActual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), RentalResponseDto.class);
        assertNotNull(rentalResponseDtoActual);
        EqualsBuilder.reflectionEquals(rentalResponseDtoExpected, rentalResponseDtoActual, "id");
    }

    @Test
    @WithUserDetails("messi@gmail.com")
    @DisplayName("Add actual return date for rental by id")
    void returnRental_ValidRentalId_ShouldReturnRentalResponseDto() throws Exception {
        //Given
        RentalResponseDto responseDtoExpected = new RentalResponseDto(1L,
                LocalDate.of(2024, 12, 5),
                LocalDate.of(2024, 12, 10),
                LocalDate.of(2024, 12, 14), 1L);

        //When
        MvcResult result = mockMvc.perform(get("/rentals/{id}/return", EXISTING_RENTAL_ID_FROM_DB)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        RentalResponseDto rentalResponseDtoActual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), RentalResponseDto.class);
        assertNotNull(rentalResponseDtoActual);
        EqualsBuilder.reflectionEquals(responseDtoExpected, rentalResponseDtoActual, "id");
    }

    @Test
    @WithUserDetails("messi@gmail.com")
    @DisplayName("Search rental by params from database")
    void searchRentalByParams_ValidRentalParams_ShouldReturnListOfRentalResponseDto()
            throws Exception {
        //Given
        List<RentalResponseDto> searchRentalsExpected = createListOfRentalResponseDto();

        //When
        MvcResult result = mockMvc.perform(get("/rentals//search")
                .param("userIds", "1L", "2L")
                .param("isActive", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        RentalResponseDto[] searchRentalsActual = objectMapper.readValue(result.getResponse()
            .getContentAsString(), RentalResponseDto[].class);
        assertNotNull(searchRentalsActual);
        EqualsBuilder.reflectionEquals(searchRentalsExpected, searchRentalsActual, "id");
    }

    private RentalRequestDto createRentalRequestDto() {
        return new RentalRequestDto(LocalDate.of(2024, 12, 6),
                LocalDate.of(2024, 12, 12), 1L);
    }

    private RentalResponseDto createRentalResponseDto(RentalRequestDto requestDto) {
        return new RentalResponseDto(3L, requestDto.rentalDate(),
                requestDto.returnDate(), null, requestDto.carId());
    }

    private List<RentalResponseDto> createListOfRentalResponseDto() {
        RentalResponseDto responseDtoFromDB1 = new RentalResponseDto(1L,
                LocalDate.of(2024, 12, 5),
                LocalDate.of(2024, 12, 10),
                null, 1L);

        RentalResponseDto responseDtoFromDB2 = new RentalResponseDto(2L,
                LocalDate.of(2024, 12, 6),
                LocalDate.of(2024, 12, 10),
                null, 2L);

        return List.of(responseDtoFromDB1, responseDtoFromDB2);
    }
}
