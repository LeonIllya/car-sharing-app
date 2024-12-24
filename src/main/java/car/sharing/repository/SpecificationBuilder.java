package car.sharing.repository;

import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T, R> {
    Specification<T> build(R searchParametersDto);

}
