import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class InternalServerErrorInterceptor implements HandlerInterceptor {
	Logger logger = LoggerFactory.getLogger(InternalServerErrorInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		logger.trace("Checking for servlet response HTTP status code...");
		if (response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
			logger.trace("HTTP status 500 returned. Redirecting to index page...");
			if (request.getRequestURI() != null) {
				response.sendRedirect(request.getRequestURI());
			} else {
				response.sendRedirect("/");
			}
			return false;
		}
		return true;
	}
}
