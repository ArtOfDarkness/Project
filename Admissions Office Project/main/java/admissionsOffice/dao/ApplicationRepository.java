import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import admissionsOffice.domain.Applicant;
import admissionsOffice.domain.Application;
import admissionsOffice.domain.Speciality;

public interface ApplicationRepository extends JpaRepository<Application, Integer>{

	List<Application> findByApplicant(Applicant applicant);

	Optional<Application> findByApplicantAndSpeciality(Applicant applicant, Speciality speciality);
}
