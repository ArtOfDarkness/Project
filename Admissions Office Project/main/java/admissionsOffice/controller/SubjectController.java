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

import admissionsOffice.domain.Subject;
import admissionsOffice.service.SubjectService;

@Controller
@RequestMapping("/subject")
@PreAuthorize("hasAuthority('ADMIN')")
public class SubjectController {
	@Autowired
	private SubjectService subjectService;
	
	@GetMapping
	public String viewSubjectList(Model model) {
		List<Subject> subjectsList = subjectService.findAll();
		model.addAttribute("subjects", subjectsList);

		return "subjectList";
	}
	
	@GetMapping("/create")
	public String viewCreationForm(@RequestParam(name = "superRefererURI", required = false) String superRefererURI,
			HttpServletRequest request, Model model) throws URISyntaxException {
		model.addAttribute("refererURI", new URI(request.getHeader("referer")).getPath());
		
		if (superRefererURI != null) {
			model.addAttribute("superRefererURI", superRefererURI);
		}
		
		return "subjectCreator";
	}

	@PostMapping("/create")
	public String createSubject(String refererURI, String superRefererURI, @Valid Subject subject, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errors);
            model.addAttribute("refererURI", refererURI);
            
            if (superRefererURI != "") {
    			model.addAttribute("superRefererURI", superRefererURI);
    		}
            
            return "subjectCreator";
        }
        		
		boolean subjectExists = !subjectService.createSubject(subject);
		
		if (subjectExists) {
			model.addAttribute("subjectExistsMessage", "Такий предмет вже існує!");
			model.addAttribute("refererURI", refererURI);
			
			if (superRefererURI != "") {
				model.addAttribute("superRefererURI", superRefererURI);
			}
			
			return "subjectCreator";
		}
		
		if (superRefererURI != "") {
			return "redirect:" + refererURI + "?superRefererURI=" + superRefererURI;
		}
		
		return "redirect:" + refererURI;
	}
	
	@GetMapping("/edit")
	public String viewEditForm(@RequestParam("id") Subject subject, Model model) {
		model.addAttribute("subject", subject);
		
		return "subjectEditor";
	}

	@PostMapping("/edit")
	public String saveSubject(@RequestParam("id") Subject subject, @Valid Subject updatedSubject, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
			model.mergeAttributes(errors);
			model.addAttribute("subject", subject);
			
			return "subjectEditor";
		}
		
		boolean subjectExists = !subjectService.updateSubject(updatedSubject);
		
		if (subjectExists) {
			model.addAttribute("subjectExistsMessage", "Такий предмет вже існує!");
			model.addAttribute("subject", subject);

			return "subjectEditor";
		}

		return "redirect:/subject";
	}
	
	@GetMapping("/delete")
	public String deleteSubject(@RequestParam("id") Subject subject) {
		if (!subject.getFaculties().isEmpty()) {
			return "redirect:/403";
		}
		
		subjectService.deleteSubject(subject);

		return "redirect:/subject";
	}
}