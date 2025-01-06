package car.sharing.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import car.sharing.dto.car.CarDto;
import car.sharing.dto.car.CreateCarRequestDto;
import car.sharing.model.Car;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@Sql(scripts = "classpath:database/cars/add-cars.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/cars/remove-cars.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTest {
    protected static MockMvc mockMvc;
    private static final Long EXISTING_CAR_ID_FROM_DB = 3L;
    private static final Long NO_EXISTENT_ID_FROM_DB = 100L;
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
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Create a new car")
    void createCar_validCarRequestDto_Success() throws Exception {
        //Given
        CreateCarRequestDto carRequestDto = createCarRequestDto();
        CarDto carDtoExpected = createCarDto(carRequestDto);
        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);

        //When
        MvcResult result = mockMvc.perform(post("/cars")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        CarDto carDtoActual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CarDto.class);
        assertNotNull(carDtoActual);
        assertTrue(EqualsBuilder.reflectionEquals(carDtoExpected, carDtoActual, "id"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Update a car by id")
    void updateCarById_ValidCarRequestDto_returnCarDto() throws Exception {
        //Given
        CreateCarRequestDto carRequestDto = updateCarRequestDto();
        CarDto carDtoExpected = createCarDto(carRequestDto);
        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);

        //When
        MvcResult mvcResult = mockMvc.perform(put("/cars/{id}", EXISTING_CAR_ID_FROM_DB)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CarDto carDtoActual = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(), CarDto.class);
        assertNotNull(carDtoActual);
        assertTrue(EqualsBuilder.reflectionEquals(carDtoExpected, carDtoActual, "id"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Can`t update a car by id")
    void updateCarById_InvalidRequestId_ShouldReturnEntityNotFoundException() throws Exception {
        //Given
        CreateCarRequestDto carRequestDto = updateCarRequestDto();
        String expected = "Can't find a car by id: " + NO_EXISTENT_ID_FROM_DB;
        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);

        //When
        MvcResult mvcResult = mockMvc.perform(put("/cars/{id}", NO_EXISTENT_ID_FROM_DB)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        //Then
        String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();
        assertEquals(expected, message);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Delete a car by id")
    void deleteCarById_ValidCarId_Success() throws Exception {
        //When
        mockMvc.perform(delete("/cars/{id}", EXISTING_CAR_ID_FROM_DB)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER", "MANAGER"})
    @DisplayName("Get a car by id from database")
    void getCarById_ValidCarId_returnCarDto() throws Exception {
        //Given
        CreateCarRequestDto carRequestDto = new CreateCarRequestDto("BMW",
                "e39", Car.CarFrame.UNIVERSAL, 50,
                new BigDecimal("60.0").setScale(2));
        CarDto carDtoExpected = createCarDto(carRequestDto);
        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);

        //When
        MvcResult result = mockMvc.perform(get("/cars/{id}", EXISTING_CAR_ID_FROM_DB)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CarDto carDtoActual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CarDto.class);
        assertNotNull(carDtoActual);
        assertTrue(EqualsBuilder.reflectionEquals(carDtoExpected, carDtoActual, "id"));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER", "MANAGER"})
    @DisplayName("Can`t get a car by id from database")
    void getCarById_InvalidCarId_ShouldReturnEntityNotFoundException() throws Exception {
        CreateCarRequestDto carRequestDto = createCarRequestDto();
        String expected = "Can`t find a car by id: " + NO_EXISTENT_ID_FROM_DB;

        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);
        MvcResult result = mockMvc.perform(get("/cars/{id}", NO_EXISTENT_ID_FROM_DB)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String message = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertEquals(expected, message);
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER", "MANAGER"})
    @DisplayName("Get all cars from database")
    void getAllCars_GivenAllCarsFromDatabase_ShouldReturnListOfCarDto() throws Exception {
        List<CarDto> listOfCarsExpected = createListOfCars();

        MvcResult mvcResult = mockMvc.perform(get("/cars")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarDto[] listOfCarDtoActual = objectMapper.readValue(mvcResult.getResponse()
            .getContentAsString(), CarDto[].class);
        assertEquals(3L, listOfCarDtoActual.length);
        assertEquals(listOfCarsExpected, Arrays.stream(listOfCarDtoActual).toList());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER", "MANAGER"})
    @DisplayName("Search car by params from database")
    void searchCars_ValidParameters_returnListOfCars() throws Exception {
        List<CarDto> listOfCars = createListOfCars();
        List<CarDto> searchCarsExpected = List.of(listOfCars.get(1), listOfCars.get(2));

        MvcResult mvcResult = mockMvc.perform(get("/cars/search")
                .param("brands", "3", "e39")
                .param("carFrames", "SEDAN", "UNIVERSAL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarDto[] listOfCarDtoActual = objectMapper.readValue(mvcResult.getResponse()
            .getContentAsString(), CarDto[].class);
        assertNotNull(listOfCarDtoActual);
        assertEquals(searchCarsExpected.size(), listOfCarDtoActual.length);
        assertEquals(searchCarsExpected, Arrays.stream(listOfCarDtoActual).toList());
    }

    private CreateCarRequestDto createCarRequestDto() {
        return new CreateCarRequestDto("Corolla",
                "Toyota", Car.CarFrame.SEDAN, 50, BigDecimal.valueOf(30));
    }

    private CreateCarRequestDto updateCarRequestDto() {
        return new CreateCarRequestDto("Corolla",
            "Toyota", Car.CarFrame.SEDAN, 100, BigDecimal.valueOf(60));
    }

    private CarDto createCarDto(CreateCarRequestDto requestDto) {
        return new CarDto(4L, requestDto.model(), requestDto.brand(),
                requestDto.carFrame(), requestDto.inventory(), requestDto.dailyFee());
    }

    private List<CarDto> createListOfCars() {
        CarDto mercedesCar = new CarDto(1L, "Mercedes", "Q7",
                Car.CarFrame.SEDAN, 100, new BigDecimal("50.0").setScale(2));
        CarDto teslaCar = new CarDto(2L, "Tesla", "3",
                Car.CarFrame.SEDAN, 150, new BigDecimal("40.0").setScale(2));
        CarDto bmwCar = new CarDto(3L, "BMW", "e39",
                Car.CarFrame.UNIVERSAL, 50, new BigDecimal("60.0").setScale(2));
        return List.of(mercedesCar, teslaCar, bmwCar);
    }
}
