package car.sharing.repository.car;

import car.sharing.dto.car.CarSearchParametersDto;
import car.sharing.model.Car;
import car.sharing.repository.SpecificationBuilder;
import car.sharing.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CarSpecificationBuilder implements SpecificationBuilder<Car, CarSearchParametersDto> {
    private static final String BRAND_SPECIFICATION = "brand";
    private static final String CAR_FRAME_SPECIFICATION = "carFrame";
    private final SpecificationProviderManager<Car> carSpecificationProviderManager;

    @Override
    public Specification<Car> build(CarSearchParametersDto searchParameters) {
        Specification<Car> spec = Specification.where(null);
        if (searchParameters.brands() != null && searchParameters.brands().length > 0) {
            spec = spec.and(carSpecificationProviderManager
                .getSpecificationProvider(BRAND_SPECIFICATION)
                .getSpecification(searchParameters.brands()));
        }

        if (searchParameters.carFrames() != null && searchParameters.carFrames().length > 0) {
            spec = spec.and(carSpecificationProviderManager
                .getSpecificationProvider(CAR_FRAME_SPECIFICATION)
                .getSpecification(searchParameters.carFrames()));
        }
        return spec;
    }
}
