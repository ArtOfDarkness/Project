import org.springframework.data.jpa.repository.JpaRepository;

import admissionsOffice.domain.User;

public interface UserRepository extends JpaRepository<User, Integer>{
	User findByEmail(String email);
	
	User findByActivationCode(String code);
	
}
