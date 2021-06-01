import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import admissionsOffice.domain.AccessLevel;
import admissionsOffice.domain.User;
import admissionsOffice.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@GetMapping
	public String userList(Model model) {
		model.addAttribute("users", userService.findAll());

		return "userList";
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@GetMapping("{user}")
	public String userEditForm(@PathVariable User user, Model model) {
		model.addAttribute("user", user);
		model.addAttribute("accessLevels", AccessLevel.values());

		return "userEditor";
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@PostMapping
	public String userSave(@RequestParam Map<String, String> form, @RequestParam("userId") User user, Model model) {
		Map<String, String> errors = userService.getUserErrors(form);
		
		if (!errors.isEmpty()) {
			model.mergeAttributes(errors);
			model.addAttribute("user", user);
			model.addAttribute("accessLevels", AccessLevel.values());

			return "userEditor";						
		}
		
		userService.saveUser(user, form);

		return "redirect:/user";
	}

	@GetMapping("profile")
	public String getProfile(@AuthenticationPrincipal User user, Model model) {
		model.addAttribute("user", userService.findById(user.getId()));
		
		return "profile";
	}

	@PostMapping("profile")	
	public String updateProfile(
			@AuthenticationPrincipal User user,
			@RequestParam String firstName,
			@RequestParam String lastName,
			@RequestParam String email,
			@RequestParam String password,
			@RequestParam String confirmPassword,
			@RequestParam(required = false) String birthDate,
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String school,
			@RequestParam(required = false) MultipartFile photo,
			@RequestParam(required = false) String removePhotoFlag,
			Model model) throws IOException {
		Map<String, String> errors = userService.getProfileErrors(user, firstName, lastName, email, password, confirmPassword, photo);
				
		if (!errors.isEmpty()) {
			model.mergeAttributes(errors);
			model.addAttribute("firstName", firstName);
			model.addAttribute("lastName", lastName);
			model.addAttribute("email", email);
			model.addAttribute("birthDate", birthDate);
			model.addAttribute("city", city);
			model.addAttribute("school", school);
			
			return "profile";			
		}
		
		boolean userExists = !userService.updateProfile(user, firstName, lastName, email, password, birthDate, city, school, photo, removePhotoFlag);
		
		if (userExists) {
			model.addAttribute("userExistsMessage", "Такий користувач вже існує!");
			model.addAttribute("user", userService.findById(user.getId()));

			return "profile";
		}
		
		return "redirect:/main";
	}
}
