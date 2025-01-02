package car.sharing.controller;

import car.sharing.dto.rental.RentalRequestDto;
import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.dto.rental.RentalSearchParametersDto;
import car.sharing.model.User;
import car.sharing.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rental management", description = "Endpoints for managing rentals")
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Create new rental by user",
            description = "Create new rental by user")
    public RentalResponseDto createRental(@RequestBody @Valid RentalRequestDto requestDto,
                                                                 Authentication authentication) {
        return rentalService.createRental(requestDto, getUser(authentication).getId());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get rental by id",
            description = "Get rental by id")
    public RentalResponseDto getRentalById(@PathVariable @Positive Long id) {
        return rentalService.getRental(id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}/return")
    @Operation(summary = "Return rental by actual time",
            description = "Return rental by actual time")
    public RentalResponseDto returnRental(@PathVariable @Positive Long id) {
        return rentalService.actualReturnDate(id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/search")
    @Operation(summary = "Get user rentals by id",
            description = "get rentals by user ID and whether "
                    + "the rental is still active or not")
    public List<RentalResponseDto> search(RentalSearchParametersDto searchParameters) {
        return rentalService.search(searchParameters);
    }

    private User getUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
