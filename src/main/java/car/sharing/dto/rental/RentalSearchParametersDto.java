package car.sharing.dto.rental;

public record RentalSearchParametersDto(Long[] userId, Boolean[] isActive) {
}
