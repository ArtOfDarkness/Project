import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import admissionsOffice.domain.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Integer>{

	Optional<Subject> findByTitle(String title);

}
