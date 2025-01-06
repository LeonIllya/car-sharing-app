package car.sharing.repository.car.spec;

import car.sharing.model.Car;
import car.sharing.repository.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class BrandSpecificationProvider implements SpecificationProvider<Car> {
    public static final String BRAND_SPECIFICATION = "brand";

    @Override
    public String getKey() {
        return BRAND_SPECIFICATION;
    }

    public Specification<Car> getSpecification(Object[] params) {
        return (root, query, criteriaBuilder) -> root.get(BRAND_SPECIFICATION)
                .in(Arrays.stream(params).toArray());
    }
}
