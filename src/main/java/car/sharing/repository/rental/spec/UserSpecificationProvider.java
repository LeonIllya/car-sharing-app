package car.sharing.repository.rental.spec;

import car.sharing.model.Rental;
import car.sharing.repository.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserSpecificationProvider implements SpecificationProvider<Rental> {
    private static final String USER_SPECIFICATION = "userId";

    @Override
    public String getKey() {
        return USER_SPECIFICATION;
    }

    public Specification<Rental> getSpecification(Object[] params) {
        return (root, query, criteriaBuilder) -> root.get(USER_SPECIFICATION)
                .in(Arrays.stream(params).toArray());
    }
}
