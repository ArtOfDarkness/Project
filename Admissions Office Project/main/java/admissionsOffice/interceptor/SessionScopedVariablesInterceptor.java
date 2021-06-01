import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import admissionsOffice.domain.AccessLevel;
import admissionsOffice.domain.User;
import admissionsOffice.service.RatingListService;
import admissionsOffice.service.UserService;

@Component
public class SessionScopedVariablesInterceptor implements HandlerInterceptor {
	@Autowired
	private UserService userService;
	@Autowired
	private RatingListService ratingListService;

	Logger logger = LoggerFactory.getLogger(SessionScopedVariablesInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		logger.trace("Checking for 'user' attribute exists in session...");
		if (request.getSession().getAttribute("user") == null) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			if (auth != null && auth.getPrincipal() != "anonymousUser") {
				logger.trace("Getting User from database and setting it in session...");
				User user = (User) auth.getPrincipal();
				User userFromDb = userService.findById(user.getId());

				request.getSession().setAttribute("user", userFromDb);

				if (userFromDb.getAccessLevels().contains(AccessLevel.valueOf("USER"))) {
					logger.trace("Setting specialities applied by applicant in session...");
					request.getSession().setAttribute("specialities",
							ratingListService.findSpecialitiesAppliedByApplicant(userFromDb.getId()));
					logger.trace("Setting applicant's photo in session...");
					request.getSession().setAttribute("photo", userService.parseFileData(userFromDb));
				}
			}
		}
		return true;
	}
}
