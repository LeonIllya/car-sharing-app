package car.sharing.repository.rental;

import car.sharing.model.Rental;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long>,
        JpaSpecificationExecutor<Rental> {
    List<Rental> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"car", "user"})
    Optional<Rental> findById(Long id);
}
