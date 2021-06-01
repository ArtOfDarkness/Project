import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import admissionsOffice.domain.Speciality;
import admissionsOffice.dto.SubjectDTO;
import admissionsOffice.service.SubjectService;

@RestController
public class SubjectRestController {
	@Autowired
	private SubjectService subjectService;

	@GetMapping("/subjectBySpeciality")
	public Set<SubjectDTO> viewSubjectsBySpeciality(@RequestParam("id") Speciality speciality) {
		return subjectService.findBySpeciality(speciality);
	}
}