import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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

import admissionsOffice.domain.Faculty;
import admissionsOffice.service.FacultyService;
import admissionsOffice.service.SubjectService;

@Controller
@RequestMapping("/faculty")
@PreAuthorize("hasAuthority('ADMIN')")
public class FacultyController {
	@Autowired
	private FacultyService facultyService;
	@Autowired
	private SubjectService subjectService;
	
	@GetMapping
	public String viewFacultyList(Model model) {
		List<Faculty> facultiesList = facultyService.findAll();
		Map<Faculty, Integer> countApplicationsByFaculty = facultyService.countApplicationsByFaculty();
		model.addAttribute("faculties", facultiesList);
		model.addAttribute("aplicationsByFaculty", countApplicationsByFaculty);

		return "facultyList";
	}
	
	@GetMapping("/create")
	public String viewCreationForm(@RequestParam(name = "superRefererURI", required = false) String superRefererURI,
			HttpServletRequest request, Model model) throws URISyntaxException {
		model.addAttribute("subjects", subjectService.findAll());
		model.addAttribute("refererURI", new URI(request.getHeader("referer")).getPath());

		if (superRefererURI != null) {
			model.addAttribute("superRefererURI", superRefererURI);
		}
		
		return "facultyCreator";
	}

	@PostMapping("/create")
	public String createFaculty(@RequestParam Map<String, String> form, @Valid Faculty faculty,	BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errors);
            model.addAttribute("subjects", subjectService.findAll());
            model.addAttribute("refererURI", form.get("refererURI"));

    		if (form.get("superRefererURI") != "") {
    			model.addAttribute("superRefererURI", form.get("superRefererURI"));
    		}
            
            return "facultyCreator";
        }
        		
		boolean facultyExists = !facultyService.createFaculty(faculty, form);
		
		if (facultyExists) {
			model.addAttribute("facultyExistsMessage", "Такий факультет вже існує!");
			model.addAttribute("subjects", subjectService.findAll());
			model.addAttribute("refererURI", form.get("refererURI"));

			if (form.get("superRefererURI") != "") {
				model.addAttribute("superRefererURI", form.get("superRefererURI"));
			}
			
			return "facultyCreator";
		}
		
		if (form.get("superRefererURI") != "") {			
			return "redirect:" + form.get("superRefererURI");
		}
		
		return "redirect:" + form.get("refererURI");
	}
	
	@GetMapping("/edit")
	public String viewEditForm(@RequestParam("id") Faculty faculty, Model model) {
		Map<Faculty, Integer> countApplicationsByFaculty = facultyService.countApplicationsByFaculty();
		if (countApplicationsByFaculty.get(faculty) != 0) {
			return "redirect:/403";	
		}
		
		model.addAttribute("faculty", faculty);
		model.addAttribute("subjects", subjectService.findAll());
		
		return "facultyEditor";
	}

	@PostMapping("/edit")
	public String updateFaculty(@RequestParam("id") Faculty faculty, @RequestParam Map<String, String> form,
			@Valid Faculty updatedFaculty, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
			model.mergeAttributes(errors);
			model.addAttribute("faculty", faculty);
			model.addAttribute("subjects", subjectService.findAll());
			
			return "facultyEditor";
		}

		boolean facultyExists = !facultyService.updateFaculty(updatedFaculty, form);
		
		if (facultyExists) {
			model.addAttribute("facultyExistsMessage", "Такий факультет вже існує!");
			model.addAttribute("faculty", faculty);
			model.addAttribute("subjects", subjectService.findAll());
						
			return "facultyEditor";
		}
		
		return "redirect:/faculty";
	}
	
	@GetMapping("/delete")
	public String deleteFaculty(@RequestParam("id") Faculty faculty) {
		if (!faculty.getExamSubjects().isEmpty() && !faculty.getSpecialities().isEmpty()) {
			return "redirect:/403";	
		}
		
		facultyService.deleteFaculty(faculty);

		return "redirect:/faculty";
	}
}