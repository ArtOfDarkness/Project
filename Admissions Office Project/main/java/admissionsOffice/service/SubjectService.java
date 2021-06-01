import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import admissionsOffice.dao.SubjectRepository;
import admissionsOffice.domain.Speciality;
import admissionsOffice.domain.Subject;
import admissionsOffice.dto.SubjectDTO;

@Service
public class SubjectService {
	Logger logger = LoggerFactory.getLogger(SubjectService.class);
	
	@Autowired
	private SubjectRepository subjectRepository;

	public List<Subject> findAll() {
		logger.trace("Getting all subjects from database...");
		
		return subjectRepository.findAll();
	}
	
	public Set<SubjectDTO> findBySpeciality(Speciality speciality) {
		logger.trace("Getting all subjects by specified speciality from database...");
		
		Set<Subject> subjects = speciality.getFaculty().getExamSubjects();

		return subjects.stream().map(subject -> new SubjectDTO(subject.getId(), subject.getTitle()))
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public boolean checkIfExists(Subject subject) {
    	logger.trace("Checking if stored subject already exists in database...");
		
		Optional<Subject> subjectFromDb = subjectRepository.findByTitle(subject.getTitle());
	
		if (subjectFromDb.isPresent() && subject.getId() != subjectFromDb.get().getId()) {
			logger.warn("Subject with title \"" + subjectFromDb.get().getTitle() + "\" already exists in database...");
			return true;
		}
		return false;
	}
	
	public boolean createSubject(Subject subject) {
		logger.trace("Adding new subject to database...");
		
		if (checkIfExists(subject)) 
			return false;

		logger.trace("Saving new subject in database...");
		subjectRepository.save(subject);
		return true;
	}

	public boolean updateSubject(Subject subject) {
		logger.trace("Updating subject in database...");
		
		if (checkIfExists(subject)) 
			return false;
		
		logger.trace("Saving updated subject in database...");
		subjectRepository.save(subject);
		return true;
	}

	public void deleteSubject(Subject subject) {
		logger.trace("Deleting subject from database...");
		
		subjectRepository.delete(subject);		
	}
}
