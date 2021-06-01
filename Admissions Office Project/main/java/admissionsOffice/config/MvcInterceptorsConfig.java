import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import admissionsOffice.interceptor.InternalServerErrorInterceptor;
import admissionsOffice.interceptor.SessionScopedVariablesInterceptor;

@Configuration
@EnableWebMvc
public class MvcInterceptorsConfig implements WebMvcConfigurer {
	@Autowired
	private SessionScopedVariablesInterceptor sessionScopedVariablesInterceptor;
	@Autowired
	private InternalServerErrorInterceptor internalServerErrorInterceptor;
		
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
	    registry.addInterceptor(sessionScopedVariablesInterceptor).addPathPatterns("/**");
	    registry.addInterceptor(internalServerErrorInterceptor).addPathPatterns("/**");
	}
}
