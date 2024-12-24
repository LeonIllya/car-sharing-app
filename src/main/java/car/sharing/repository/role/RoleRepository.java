package car.sharing.repository.role;

import car.sharing.model.Role;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role getByUserRole(Role.UserRole userRole);

    Set<Role> findByUserRole(Role.UserRole userRole);
}
