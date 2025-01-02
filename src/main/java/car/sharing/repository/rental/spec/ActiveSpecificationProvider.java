package car.sharing.repository.rental.spec;

import car.sharing.model.Rental;
import car.sharing.repository.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ActiveSpecificationProvider implements SpecificationProvider<Rental> {
    public static final String RENTAL_IS_ACTIVE_SPECIFICATION = "isActive";

    @Override
    public String getKey() {
        return RENTAL_IS_ACTIVE_SPECIFICATION;
    }

    public Specification<Rental> getSpecification(Object[] params) {
        return (root, query, criteriaBuilder) -> root.get(RENTAL_IS_ACTIVE_SPECIFICATION)
                .in(Arrays.stream(params).toArray());
    }
}
