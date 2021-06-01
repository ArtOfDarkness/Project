import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import admissionsOffice.dao.ApplicantRepository;
import admissionsOffice.dao.RatingListRepository;
import admissionsOffice.dao.SpecialityRepository;
import admissionsOffice.domain.Applicant;
import admissionsOffice.domain.Application;
import admissionsOffice.domain.RatingList;
import admissionsOffice.domain.Speciality;
import admissionsOffice.domain.Subject;
import admissionsOffice.dto.SpecialityDTO;

@Service
public class RatingListService {
	Logger logger = LoggerFactory.getLogger(RatingListService.class);
	
	@Autowired
	private RatingListRepository ratingListRepository;
	@Autowired
	private SpecialityRepository specialityRepository;
	@Autowired
	private ApplicantRepository applicantRepository;
	@Autowired
	private MailSender mailSender;
	
	public Optional<RatingList> findById(Integer id) {
		logger.trace("Getting rating list by id=" + id + " from database...");
		
		return ratingListRepository.findById(id);
	}

	public RatingList initializeRatingList(Application application, Map<String, String> form) {
		logger.trace("Initializing rating list for specified application...");
		
		Optional<RatingList> ratingListFromDb = findById(application.getId());
		RatingList ratingList = ratingListFromDb.orElse(new RatingList());
		
		ratingList.setId(application.getId());
		
		Double totalMark = calculateTotalMark(application.getSpeciality().getFaculty().getSubjectCoeffs(), application.getZnoMarks(), application.getAttMark());
		ratingList.setTotalMark(totalMark);
				
		checkApplicationForRejectionMessage(application, form, ratingList);
		
		checkApplicationForBeingAccepted(application, form, ratingList);

		ratingList.setApplication(application);
		
		return ratingList;
	}

	public void checkApplicationForRejectionMessage(Application application, Map<String, String> form, RatingList ratingList) {
		logger.trace("Checking application for rejection message present...");
		
		for (String key : form.keySet()) {
			if (key.equals("rejectionMessage") && !form.get(key).isEmpty()) {
				ratingList.setRejectionMessage(form.get(key));
				sendApplicationRejectionEmail(application, form.get(key));
			} else {
				ratingList.setRejectionMessage(null);
			}
		}
	}
	
	public void checkApplicationForBeingAccepted(Application application, Map<String, String> form,	RatingList ratingList) {
		logger.trace("Checking application for being accepted...");
		
		for (String key : form.keySet()) {
			if (key.equals("accept")) {
				ratingList.setAccepted(true);
				ratingList.setRejectionMessage(null);
				sendApplicationAcceptanceEmail(application);
			}
		}
	}
	
	public void sendApplicationAcceptanceEmail(Application application) {
		logger.trace("Sending application acceptance message to user's email...");
		
		String message = String.format(
				"Доброго дня, %s %s! \n\n" +
						"Ваша вступна заявка на спеціальність \"%s\" прийнята адміністратором.\n" +
						"Результати конкурсного відбору по обраній спеціальності ви можете відслідкувати в своєму особистому кабінеті.",
					application.getApplicant().getUser().getFirstName(),
					application.getApplicant().getUser().getLastName(),
					application.getSpeciality().getTitle()					
				);

		mailSender.send(application.getApplicant().getUser().getEmail(), "Вступна заявка на спеціальність \"" + application.getSpeciality().getTitle() + "\" прийнята", message);
	}
	
	public void sendApplicationRejectionEmail(Application application, String rejectionMessage) {
		logger.trace("Sending application rejection message to user's email...");
		
		String message = String.format(
				"Доброго дня, %s %s! \n\n" +
						"Ваша вступна заявка на спеціальність \"%s\" відхилена адміністратором по причині: \"%s\".\n" +
						"Для участі в конкурсному відборі по обраній спеціальності виправте, будьласка, виявленні неточності в заявці у свому особистому кабінеті.",
					application.getApplicant().getUser().getFirstName(),
					application.getApplicant().getUser().getLastName(),
					application.getSpeciality().getTitle(),
					rejectionMessage					
				);

		mailSender.send(application.getApplicant().getUser().getEmail(), "Вступна заявка на спеціальність \"" + application.getSpeciality().getTitle() + "\" відхилена", message);
	}

	public Double calculateTotalMark(Map<Subject, Double> subjectCoeffs, Map<Subject, Integer> znoMarks, Integer attMark) {
		logger.trace("Calculating application total mark...");
		
		Double totalZnoMark = calculateTotalZnoMark(subjectCoeffs, znoMarks);
		return RatingList.znoCoeff * totalZnoMark + RatingList.attMarkCoeff * Double.valueOf(attMark);
	}

	public Double calculateTotalZnoMark(Map<Subject, Double> subjectCoeffs, Map<Subject, Integer> znoMarks) {
		logger.trace("Calculating total ZNO mark...");
		
		Double totalZnoMark = 0.0;
		
		for (Entry<Subject, Integer> entry : znoMarks.entrySet()) {
			Double subjectCoeff = subjectCoeffs.get(entry.getKey());
			Integer znoSubjectMark = entry.getValue();
			Double znoMark = subjectCoeff * Double.valueOf(znoSubjectMark);
			
			totalZnoMark += znoMark;
		}
		return totalZnoMark;
	}

	public Map<Speciality, Integer> parseNumberOfApplicationsBySpeciality() {
		logger.trace("Parsing number of applications by specialty from DB array and mapping to Java Collection of objects...");
		
		List<Object[]> submittedAppsFromDb = ratingListRepository.countApplicationsBySpeciality();
		List<Speciality> specialitiesList = specialityRepository.findAll();
		Map<Speciality, Integer> submittedApps = new HashMap<>();
		
		for (Speciality speciality : specialitiesList) {
			for (Object[] object : submittedAppsFromDb) {
				
				if (((Integer) object[0]).equals(speciality.getId())) {
					submittedApps.put(speciality, ((BigInteger) object[1]).intValue());
					break;
				} else {
					submittedApps.put(speciality, 0);
				}
			}
		}
		return submittedApps;
	}
	
	public Map<Applicant, Double> parseApplicantsRankBySpeciality(Integer specialityId) {
		logger.trace("Parsing applicants rank by specialty from DB array and mapping to Java Collection of objects...");
		
		List<Object[]> applicantsRankFromDb = ratingListRepository.getApplicantsRankBySpeciality(specialityId);
		List<Applicant> applicantsList = applicantRepository.findAll();
		Map<Applicant, Double> applicantsRank = new HashMap<>();
		Comparator<Map.Entry<Applicant, Double>> mapValuesComparator = Comparator.comparing(Map.Entry::getValue);
		
		for (Applicant applicant : applicantsList) {
			for (Object[] object : applicantsRankFromDb) {
				if (((Integer) object[0]).equals(applicant.getId())) {
					applicantsRank.put(applicant, (Double) object[1]);
				}
			}
		}
		return applicantsRank.entrySet().stream().sorted(mapValuesComparator.reversed())
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(),
						(oldValue, newValue) -> oldValue, LinkedHashMap::new));
	}
	
	public List<Speciality> findSpecialitiesAppliedByApplicant(Integer applicantId) {
		logger.trace("Getting all specialities applied by applicant from database...");
		
		List<Integer> specialitiesByApplicantFromDb = ratingListRepository.findSpecialitiesByApplicant(applicantId);
		List<Speciality> specialitiesList = specialityRepository.findAll();
		
		return specialitiesList.stream()
				.filter(speciality -> specialitiesByApplicantFromDb.stream()
						.anyMatch(specialityId -> specialityId.equals(speciality.getId())))
				.collect(Collectors.toList());
	}
	
	public Set<SpecialityDTO> parseSpecialitiesAppliedByApplicant(Integer applicantId) {
		logger.trace("Parsing specialities applied by applicant and mapping to Set of Speciality DTO objects...");
		
		List<Speciality> specialities = findSpecialitiesAppliedByApplicant(applicantId);

		return specialities.stream().map(speciality -> new SpecialityDTO(speciality.getId(), speciality.getTitle()))
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public Page<RatingList> findNotAcceptedApps(Pageable pageable) {
		logger.trace("Getting all not accepted applications from database...");
		
		return ratingListRepository.findByAcceptedFalseAndRejectionMessageIsNull(pageable);
	}

	public void announceRecruitmentResultsBySpeciality(Speciality speciality) {
		logger.trace("Preparing to announce recruitment results by specified speciality...");
		
		Set<Applicant> enrolledApplicants = getEnrolledApplicantsBySpeciality(speciality);
		enrolledApplicants.stream().forEach(applicant -> sendApplicantEnrollmentEmail(applicant, speciality));
//		enrolledApplicants.stream().forEach(applicant -> System.out.println(applicant.getUser().getFirstName() + " " + applicant.getUser().getLastName() + ", Вы приняты!"));
	}

	public Set<Applicant> getEnrolledApplicantsBySpeciality(Speciality speciality) {
		logger.trace("Getting all enrolled applicants by speciality...");
		
		Map<Applicant, Double> applicantsRank = parseApplicantsRankBySpeciality(speciality.getId());
		Set<Applicant> enrolledApplicants = new LinkedHashSet<>();
		Integer i = 1;
		
		if (speciality.isRecruitmentCompleted()) {
			for (Entry<Applicant, Double> entry : applicantsRank.entrySet()) {
				if (i <= speciality.getEnrollmentPlan()) {
					enrolledApplicants.add(entry.getKey());
					i++;
				}
			}
		}
		return enrolledApplicants;
	}

	public void sendApplicantEnrollmentEmail(Applicant applicant, Speciality speciality) {
		logger.trace("Sending applicant enrollment message to user's email...");
		
		String message = String.format(
				"Доброго дня, %s %s! \n\n" +
						"Вітаємо! По результатам конкурсного відбору на спеціальність \"%s\" Ви виявились в числі абітурієнтів, рекомедованих до зарахування.\n" +
						"Будь ласка, протягом 10 днів подайте оригінали документів до приймальної комісії.",
					applicant.getUser().getFirstName(),
					applicant.getUser().getLastName(),
					speciality.getTitle()									
				);

		mailSender.send(applicant.getUser().getEmail(), "Набір на спеціальність \"" + speciality.getTitle() + "\" завершений", message);
	}
}
