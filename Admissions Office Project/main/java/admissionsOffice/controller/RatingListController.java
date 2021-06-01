import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import admissionsOffice.domain.AccessLevel;
import admissionsOffice.domain.Applicant;
import admissionsOffice.domain.Application;
import admissionsOffice.domain.RatingList;
import admissionsOffice.domain.Speciality;
import admissionsOffice.domain.User;
import admissionsOffice.service.ApplicationService;
import admissionsOffice.service.RatingListService;

@Controller
@RequestMapping("/ratingList")
public class RatingListController {
	@Autowired
	private ApplicationService applicationService;
	@Autowired
	private RatingListService ratingListService;
	
	@GetMapping("/speciality")
	public String viewApplicantsRankBySpeciality(@RequestParam("id") Speciality speciality, HttpServletRequest request, HttpSession session, Model model) throws URISyntaxException {
		User currentUser = ((User) session.getAttribute("user"));		
		List<Speciality> specialities = ((List<Speciality>) session.getAttribute("specialities"));
		if (currentUser.getAccessLevels().contains(AccessLevel.valueOf("USER")) && !specialities.contains(speciality)) {
			return "redirect:/403";
		}
		
		Map<Applicant, Double> applicantsRank = ratingListService.parseApplicantsRankBySpeciality(speciality.getId());
		Set<Applicant> enrolledApplicants = ratingListService.getEnrolledApplicantsBySpeciality(speciality);

		model.addAttribute("speciality", speciality);
		model.addAttribute("applicantsRank", applicantsRank);
		model.addAttribute("enrolledApplicants", enrolledApplicants);
		
		if (request.getHeader("referer") == null) {
			session.setAttribute("refererURI", new URI("/"));
		} else if (!(new URI(request.getHeader("referer")).getPath()).equals("/ratingList/totalMarkCalculation")) {
			session.setAttribute("refererURI", new URI(request.getHeader("referer")));
		}

		return "ratingList";
	}
	
	@GetMapping("/totalMarkCalculation")
	public String viewTotalMarkCalculation(@RequestParam("applicant_id") Applicant applicant, @RequestParam("speciality_id") Speciality speciality, HttpServletRequest request, Model model) throws URISyntaxException {
		Application application = applicationService.findByApplicantAndSpeciality(applicant, speciality);
		
		model.addAttribute("aplication", application);
		model.addAttribute("totalZnoMark", ratingListService.calculateTotalZnoMark(application.getSpeciality().getFaculty().getSubjectCoeffs(), application.getZnoMarks()));
		model.addAttribute("znoCoeff", RatingList.znoCoeff);
		model.addAttribute("attMarkCoeff", RatingList.attMarkCoeff);		
		model.addAttribute("refererURI", new URI(request.getHeader("referer")));

		return "totalMarkCalculation";
	}
}
