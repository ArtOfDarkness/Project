import org.springframework.data.jpa.repository.JpaRepository;

import admissionsOffice.domain.Applicant;

public interface ApplicantRepository extends JpaRepository<Applicant, Integer>{

}
