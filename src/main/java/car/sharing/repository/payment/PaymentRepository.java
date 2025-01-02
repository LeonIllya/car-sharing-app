package car.sharing.repository.payment;

import car.sharing.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p JOIN p.rental r WHERE r.id IN :rentalIds")
    List<Payment> findAllByRentalsId(@Param("rentalIds") List<Long> rentalIds);

    Optional<Payment> findBySessionId(String sessionId);
}
