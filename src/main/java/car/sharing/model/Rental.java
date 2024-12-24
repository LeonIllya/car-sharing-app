package car.sharing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "rentals")
@EqualsAndHashCode(of = {"id"})
@SQLDelete(sql = "UPDATE rentals SET is_deleted = TRUE WHERE id=?")
@SQLRestriction(value = "is_deleted = FALSE")
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "rental_date", nullable = false)
    private LocalDate rentalDate;
    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    @Column(name = "car_id", nullable = false)
    private Long carId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private boolean isActive = true;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private boolean isDeleted = false;
}
