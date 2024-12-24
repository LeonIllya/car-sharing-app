package car.sharing.controller;

import car.sharing.dto.car.CarDto;
import car.sharing.dto.car.CarSearchParametersDto;
import car.sharing.dto.car.CreateCarRequestDto;
import car.sharing.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Car management", description = "Endpoints for managing cars")
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/cars")
public class CarController {
    private final CarService carService;

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new car", description = "Create new car")
    public CarDto createCar(@RequestBody @Valid CreateCarRequestDto requestDto) {
        return carService.createCar(requestDto);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    @Operation(summary = "Update car by id", description = "Update car by id")
    public CarDto updateCar(@PathVariable @Positive Long id,
                            @RequestBody @Valid CreateCarRequestDto requestDto) {
        return carService.updateCarById(id, requestDto);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete car by id", description = "Delete car by id")
    public void deleteCarById(@PathVariable @Positive Long id) {
        carService.deleteCarById(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car by id",
            description = "Get car by id")
    public CarDto findCarById(@PathVariable @Positive Long id) {
        return carService.findCarById(id);
    }

    @GetMapping
    @Operation(summary = "Get all cars with all information",
            description = "Get a list of cars with all information")
    public List<CarDto> findAllCars(Pageable pageable) {
        return carService.findAllCars(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search cars by parameters", description = "Search cars by parameters")
    public List<CarDto> getAllByCarFrame(CarSearchParametersDto parametersDto) {
        return carService.search(parametersDto);
    }
}
