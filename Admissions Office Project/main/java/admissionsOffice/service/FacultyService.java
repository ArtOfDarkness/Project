import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import admissionsOffice.dao.FacultyRepository;
import admissionsOffice.dao.SubjectRepository;
import admissionsOffice.domain.Application;
import admissionsOffice.domain.Faculty;
import admissionsOffice.domain.Subject;

@Service
public class FacultyService {
	Logger logger = LoggerFactory.getLogger(FacultyService.class);
	
	@Autowired
	private FacultyRepository facultyRepository;
	@Autowired
	private SubjectRepository subjectRepository;
	@Autowired
	private ApplicationService applicationService;

	public List<Faculty> findAll() {
		logger.trace("Getting all faculties from database...");
		
		return facultyRepository.findAll();
	}

	public boolean checkIfExists(Faculty faculty) {
    	logger.trace("Checking if stored faculty already exists in database...");
    	
		Optional<Faculty> facultyFromDb = facultyRepository.findByTitle(faculty.getTitle());
		
		if (facultyFromDb.isPresent() && faculty.getId() != facultyFromDb.get().getId()) {
			logger.warn("Faculty with title \"" + facultyFromDb.get().getTitle() + "\" already exists in database...");
			return true;
		}
		return false;
	}
	
	public boolean createFaculty(Faculty faculty, Map<String, String> form) {
		logger.trace("Adding new faculty to database...");
		
		if (checkIfExists(faculty)) 
			return false;
		
		logger.trace("Saving new faculty in database...");
		facultyRepository.save(faculty);
		updateFaculty(faculty, form);
		return true;
	}

	public boolean updateFaculty(Faculty faculty, Map<String, String> form) {
		logger.trace("Updating faculty in database...");

		if (checkIfExists(faculty)) 
			return false;

		Set<Subject> examSubjects = parseExamSubjects(form);
		faculty.setExamSubjects(examSubjects);

		Map<Subject, Double> subjectCoeffs = parseSubjectCoeffs(form);
		faculty.setSubjectCoeffs(subjectCoeffs);

		logger.trace("Saving updated faculty in database...");
		facultyRepository.save(faculty);
		return true;
	}

	public void deleteFaculty(Faculty faculty) {
		logger.trace("Deleting faculty from database...");
		
		facultyRepository.delete(faculty);
	}

	public Set<Subject> parseExamSubjects(Map<String, String> form) {
		logger.trace("Parsing exam subjects from Form Strings and mapping to Java Collection of objects...");
		
		Set<String> subjectTitles = subjectRepository.findAll().stream().map(Subject::getTitle).collect(Collectors.toSet());
		Set<Subject> examSubjects = new HashSet<>();

		for (String key : form.keySet()) {
			if (subjectTitles.contains(form.get(key))) {
				examSubjects.add(new Subject(Integer.valueOf(key), form.get(key)));
			}
		}
		return examSubjects;
	}
	
	public Map<Subject, Double> parseSubjectCoeffs(Map<String, String> form) {
		logger.trace("Parsing subjects coefficients from Form Strings and mapping to Java Collection of objects...");
		
		Set<String> subjectTitles = subjectRepository.findAll().stream().map(Subject::getTitle).collect(Collectors.toSet());
		Map<Subject, Double> subjectCoeffs = new HashMap<>();

		for (String key : form.keySet()) {
			if (subjectTitles.contains(form.get(key))) {
				for (String key2 : form.keySet()) {
					if (key2.equals("coeff" + key)) {
						subjectCoeffs.put(new Subject(Integer.valueOf(key), form.get(key)), Double.valueOf(form.get(key2)));
					}
				}
			}
		}
		return subjectCoeffs;
	}
	
	public Map<Faculty, Integer> countApplicationsByFaculty() {
		logger.trace("Counting number of applications by Faculty...");
		
		List<Faculty> facultyList = findAll();
		List<Application> applicationList = applicationService.findAll();
		Map<Faculty, Integer> applicationsByFaculty = new HashMap<>();
		
		for (Faculty faculty : facultyList) {
			Integer appCounter = 0;			
			for (Application application : applicationList) {
				if (application.getSpeciality().getFaculty().equals(faculty)) {
					appCounter += 1;
				}				
			}
			applicationsByFaculty.put(faculty, appCounter);			
		}
		return applicationsByFaculty;
	}
}