import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import admissionsOffice.domain.AccessLevel;
import admissionsOffice.domain.Application;
import admissionsOffice.domain.RatingList;
import admissionsOffice.domain.User;
import admissionsOffice.service.ApplicationService;
import admissionsOffice.service.RatingListService;
import admissionsOffice.service.SpecialityService;
import admissionsOffice.service.SupportingDocumentService;

@Controller
@RequestMapping("/application")
public class ApplicationController {
	@Autowired
	private ApplicationService applicationService;
	@Autowired
	private SpecialityService specialityService;
	@Autowired
	private SupportingDocumentService supportingDocumentService;
	@Autowired
	private RatingListService ratingListService;
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping
	public String viewApplicationList(HttpServletRequest request, HttpSession session, Model model) {
		User user = (User) request.getSession().getAttribute("user");
		List<Application> applicationsList = applicationService.findByApplicant(user.getApplicant());
		model.addAttribute("applications", applicationsList);
		model.addAttribute("applicationsStatus", applicationService.getApplicationsStatus(applicationsList));
		session.setAttribute("specialities", ratingListService.findSpecialitiesAppliedByApplicant(user.getId()));
		
		return "applicationList";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/create")
	public String viewCreationForm(Model model) {
		model.addAttribute("specialities", specialityService.findByRecruitmentCompletedFalse());
		
		return "applicationCreator";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@PostMapping("/create")
	public String createApplication(@RequestParam Map<String, String> form,	@RequestParam("supportingDocument") MultipartFile[] supportingDocuments,
			@Valid Application application, BindingResult bindingResult, Model model) throws IOException {
		Map<String, String> znoMarksErrors = applicationService.getZnoMarksErrors(form);
		Map<String, String> supportingDocumentErrors = supportingDocumentService.getSupportingDocumentErrors(supportingDocuments);
		
		if (bindingResult.hasErrors() || form.get("speciality") == "" || !znoMarksErrors.isEmpty() || !supportingDocumentErrors.isEmpty()) {
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errors);
            model.mergeAttributes(supportingDocumentErrors);
            model.addAttribute(!znoMarksErrors.isEmpty() ? "znoMarksErrorMessage" : "", "При заповненні балів по ЗНО були найдені помилки: " +
            		znoMarksErrors.values() + ". Попробуйте заповнити форму ще раз!");
            model.addAttribute(form.get("speciality").isEmpty() ? "specialityError" : "", "Поле Спеціальність не може буть пустим!");
    		model.addAttribute("specialities", specialityService.findByRecruitmentCompletedFalse());
    		
            return "applicationCreator";
        }
   		
		boolean applicationExists = !applicationService.createApplication(application, form, supportingDocuments);

		if (applicationExists) {
			model.addAttribute("applicationExistsMessage", "На обрану спеціальність заявка вже існує!");
			model.addAttribute("specialities", specialityService.findByRecruitmentCompletedFalse());

			return "applicationCreator";
		}

		return "redirect:/application";
	}
	
	@GetMapping("/edit")
	public String viewEditForm(@RequestParam("id") Application application, HttpSession session, Model model) {
		User currentUser = ((User) session.getAttribute("user"));
		if (currentUser.getAccessLevels().contains(AccessLevel.valueOf("USER"))
				&& !application.getApplicant().getId().equals(currentUser.getId())
				|| application.getRatingList().isAccepted()) {
			return "redirect:/403";
		}
		
		model.addAttribute("aplication", application);
		model.addAttribute("specialities", specialityService.findByRecruitmentCompletedFalse());
		model.addAttribute("downloadURI", ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/").toUriString());
		
		return "applicationEditor";
	}

	@PostMapping("/edit")
	public String updateApplication(@RequestParam("id") Application application, @RequestParam Map<String, String> form,
			@RequestParam("supportingDocument") MultipartFile[] supportingDocuments, HttpSession session, @Valid Application updatedApplication,
			BindingResult bindingResult, Model model) throws IOException {
		Map<String, String> znoMarksErrors = applicationService.getZnoMarksErrors(form);
		Map<String, String> supportingDocumentErrors = supportingDocumentService.getSupportingDocumentErrors(supportingDocuments);

		if (bindingResult.hasErrors() || !znoMarksErrors.isEmpty() || !supportingDocumentErrors.isEmpty()) {
			Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
			model.mergeAttributes(errors);
			model.mergeAttributes(znoMarksErrors);
			model.mergeAttributes(supportingDocumentErrors);
			model.addAttribute("aplication", application);
			model.addAttribute("specialities", specialityService.findByRecruitmentCompletedFalse());
			model.addAttribute("downloadURI", ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/").toUriString());
			
			return "applicationEditor";
		}
		
		boolean applicationExists = !applicationService.updateApplication(updatedApplication, form, supportingDocuments);
		
		if (applicationExists) {
			model.addAttribute("applicationExistsMessage", "На обрану спеціальність заявка вже існує!");
			model.addAttribute("aplication", application);
			model.addAttribute("specialities", specialityService.findByRecruitmentCompletedFalse());
			model.addAttribute("downloadURI", ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/").toUriString());

			return "applicationEditor";
		}
		
		if (((User) session.getAttribute("user")).getAccessLevels().contains(AccessLevel.valueOf("ADMIN"))) {
			return "redirect:/application/notAcceptedApps";
		}
		
		return "redirect:/application";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/delete")
	public String deleteApplication(@RequestParam("id") Application application, HttpSession session) {
		User currentUser = ((User) session.getAttribute("user"));
		if (currentUser.getAccessLevels().contains(AccessLevel.valueOf("USER"))
				&& !application.getApplicant().getId().equals(currentUser.getId())
				|| application.getSpeciality().isRecruitmentCompleted()) {
			return "redirect:/403";
		}
		
		applicationService.deleteApplication(application);

		return "redirect:/application";
	}
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@GetMapping("/notAcceptedApps")
	public String viewNotAcceptedApps(HttpSession session, @PageableDefault(size = 5, sort = "application_id", direction = Sort.Direction.ASC) Pageable pageable) {
		Page<RatingList> notAcceptedApps = ratingListService.findNotAcceptedApps(pageable);
		
		session.setAttribute("notAcceptedApps", notAcceptedApps);
		
		if (notAcceptedApps.isEmpty()) {
			return "redirect:/main";
		}
		
		return "notAcceptedApps";
	}	
}