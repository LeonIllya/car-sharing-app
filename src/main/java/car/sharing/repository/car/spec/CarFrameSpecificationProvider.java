package car.sharing.repository.car.spec;

import car.sharing.model.Car;
import car.sharing.repository.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CarFrameSpecificationProvider implements SpecificationProvider<Car> {
    public static final String CAR_FRAME_SPECIFICATION = "carFrame";

    @Override
    public String getKey() {
        return CAR_FRAME_SPECIFICATION;
    }

    public Specification<Car> getSpecification(Object[] params) {
        return (root, query, criteriaBuilder) -> root.get(CAR_FRAME_SPECIFICATION)
                .in(Arrays.stream(params).toArray());
    }
}
