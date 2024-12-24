package car.sharing.repository.rental;

import car.sharing.dto.rental.RentalSearchParametersDto;
import car.sharing.model.Rental;
import car.sharing.repository.SpecificationBuilder;
import car.sharing.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalSpecificationBuilder implements SpecificationBuilder<Rental,
        RentalSearchParametersDto> {
    private static final String USER_SPECIFICATION = "userId";
    private static final String RENTAL_IS_ACTIVE_SPECIFICATION = "isActive";
    private final SpecificationProviderManager<Rental> rentalSpecificationProviderManager;

    @Override
    public Specification<Rental> build(RentalSearchParametersDto searchParameters) {
        Specification<Rental> spec = Specification.where(null);
        if (searchParameters.userId() != null && searchParameters.userId().length > 0) {
            spec = spec.and(rentalSpecificationProviderManager
                    .getSpecificationProvider(USER_SPECIFICATION)
                    .getSpecification(searchParameters.userId()));
        }

        if (searchParameters.isActive() != null && searchParameters.isActive().length > 0) {
            spec = spec.and(rentalSpecificationProviderManager
                    .getSpecificationProvider(RENTAL_IS_ACTIVE_SPECIFICATION)
                    .getSpecification(searchParameters.isActive()));
        }
        return spec;
    }
}
