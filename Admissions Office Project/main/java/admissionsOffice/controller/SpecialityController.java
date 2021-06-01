import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import admissionsOffice.domain.Speciality;
import admissionsOffice.service.FacultyService;
import admissionsOffice.service.RatingListService;
import admissionsOffice.service.SpecialityService;

@Controller
@RequestMapping("/speciality")
@PreAuthorize("hasAuthority('ADMIN')")
public class SpecialityController {
	@Autowired
	private SpecialityService specialityService;
	@Autowired
	public FacultyService facultyService;
	@Autowired
	private RatingListService ratingListService;
	
	@GetMapping
	public String viewSpecialityList(Model model) {
		List<Speciality> specialitiesList = specialityService.findAll();
		Map<Speciality, Integer> submittedApps = ratingListService.parseNumberOfApplicationsBySpeciality();
		model.addAttribute("specialities", specialitiesList);
		model.addAttribute("submittedApps", submittedApps);

		return "specialityList";
	}
	
	@GetMapping("/create")
	public String viewCreationForm(Model model) {
		model.addAttribute("faculties", facultyService.findAll());
		
		return "specialityCreator";
	}

	@PostMapping("/create")
	public String createSpeciality(@RequestParam Map<String, String> form, @Valid Speciality speciality, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors() || form.get("faculty") == "") {
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errors);
            model.addAttribute(form.get("faculty") == "" ? "facultyError" : "", "Факультет спеціальності не може бути пустим!");
            model.addAttribute("faculties", facultyService.findAll());
            
            return "specialityCreator";
        }
		       		
		boolean specialityExists = !specialityService.createSpeciality(speciality, form);
		
		if (specialityExists) {
			model.addAttribute("specialityExistsMessage", "Така спеціальність вже існує!");
			model.addAttribute("faculties", facultyService.findAll());
			
			return "specialityCreator";
		}
		
		return "redirect:/speciality/";
	}

	@GetMapping("/edit")
	public String viewEditForm(@RequestParam("id") Speciality speciality, Model model) {
		if (speciality.isRecruitmentCompleted()) {
			return "redirect:/403";
		}
		
		model.addAttribute("speciality", speciality);
		model.addAttribute("faculties", facultyService.findAll());
		
		return "specialityEditor";
	}

	@PostMapping("/edit")
	public String updateSpeciality(@RequestParam("id") Speciality speciality, @RequestParam Map<String, String> form, @Valid Speciality updatedSpeciality, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors() || form.get("faculty") == "") {
			Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
			model.mergeAttributes(errors);
			model.addAttribute(form.get("faculty") == "" ? "facultyError" : "", "Факультет спеціальності не може бути пустим!");
			model.addAttribute("speciality", speciality);
			model.addAttribute("faculties", facultyService.findAll());
			
			return "specialityEditor";
		}

		boolean specialityExists = !specialityService.updateSpeciality(updatedSpeciality, form);
		
		if (specialityExists) {
			model.addAttribute("specialityExistsMessage", "Така спеціальність вже існує!");
			model.addAttribute("speciality", speciality);
			model.addAttribute("faculties", facultyService.findAll());
			
			return "specialityEditor";
		}
		
		return "redirect:/speciality";
	}
	
	@GetMapping("/delete")	
	public String deleteSpeciality(@RequestParam("id") Speciality speciality) {
		if (!speciality.getApplications().isEmpty()) {
			return "redirect:/403";
		}
		
		specialityService.deleteSpeciality(speciality);

		return "redirect:/speciality";
	}
	
	@GetMapping("/complete")
	public String completeRecruitment(@RequestParam("id") Speciality speciality) {
		Map<Speciality, Integer> submittedApps = ratingListService.parseNumberOfApplicationsBySpeciality();
		if (submittedApps.get(speciality) == 0 || speciality.isRecruitmentCompleted()) {
			return "redirect:/403";
		}
		
		specialityService.completeRecruitment(speciality);

		return "redirect:/speciality";
	}

}