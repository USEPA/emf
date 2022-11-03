package gov.epa.emissions.framework.services.spring;


import javax.servlet.ServletContext;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class EMFWebAppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext container) {
        AnnotationConfigWebApplicationContext context
          = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation("gov.epa.emissions.framework.services.spring");
        
        container.addListener(new EMFContextLoaderListener(context));

//        ServletRegistration.Dynamic dispatcher = container
//          .addServlet("dispatcher", new DispatcherServlet(context));
//        
//        dispatcher.setLoadOnStartup(1);
//        dispatcher.addMapping("/");
    }
}
