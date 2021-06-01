import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import admissionsOffice.dao.FacultyRepository;
import admissionsOffice.dao.SpecialityRepository;
import admissionsOffice.domain.Faculty;
import admissionsOffice.domain.Speciality;

@Service
public class SpecialityService {
	Logger logger = LoggerFactory.getLogger(SpecialityService.class);
	
	@Autowired
	private SpecialityRepository specialityRepository;
	@Autowired
	private FacultyRepository facultyRepository;
	@Autowired
	private RatingListService ratingListService;

	public List<Speciality> findAll() {
		logger.trace("Getting all specialities from database...");
		
		return specialityRepository.findAll();
	}

	public List<Speciality> findByRecruitmentCompletedFalse() {
		logger.trace("Getting all specialities where recruitment is completed from database...");
		
		return specialityRepository.findByRecruitmentCompletedFalse();
	}
	
	public boolean checkIfExists(Speciality speciality) {
    	logger.trace("Checking if stored speciality already exists in database...");
		
		Optional<Speciality> specialityFromDb = specialityRepository.findByTitle(speciality.getTitle());
		
		if (specialityFromDb.isPresent() && speciality.getId() != specialityFromDb.get().getId()) {
			logger.warn("Speciality with title \"" + specialityFromDb.get().getTitle() + "\" already exists in database...");
			return true;
		}
		return false;
	}
	
	public boolean createSpeciality(Speciality speciality, Map<String, String> form) {
		logger.trace("Adding new speciality to database...");
		
		if (checkIfExists(speciality)) 
			return false;
		
		Faculty faculty = parseFaculty(form);
		speciality.setFaculty(faculty);
		speciality.setRecruitmentCompleted(false);

		logger.trace("Saving new speciality in database...");
		specialityRepository.save(speciality);
		return true;
	}

	public boolean updateSpeciality(Speciality speciality, Map<String, String> form) {
		logger.trace("Updating speciality in database...");
		
		if (checkIfExists(speciality)) 
			return false;
		
		Faculty faculty = parseFaculty(form);
		speciality.setFaculty(faculty);
		speciality.setRecruitmentCompleted(false);
		
		logger.trace("Saving updated speciality in database...");
		specialityRepository.save(speciality);
		return true;
	}

	public void deleteSpeciality(Speciality speciality) {
		logger.trace("Deleting speciality from database...");
		
		specialityRepository.delete(speciality);
	}

	public void completeRecruitment(Speciality speciality) {
		logger.trace("Completing recruitment by specified speciality...");
		speciality.setRecruitmentCompleted(true);
		specialityRepository.save(speciality);

		ratingListService.announceRecruitmentResultsBySpeciality(speciality);
	}
	
	public Faculty parseFaculty(Map<String, String> form) {
		logger.trace("Parsing faculty from Form Strings and mapping to Java Object...");
		
		Integer facultyId = Integer.valueOf(form.get("faculty"));
		Faculty faculty = facultyRepository.findById(facultyId).get();
		
		return faculty;
	}
}