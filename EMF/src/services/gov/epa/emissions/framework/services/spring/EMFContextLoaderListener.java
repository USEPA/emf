package gov.epa.emissions.framework.services.spring;

import javax.servlet.ServletContextEvent;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

public class EMFContextLoaderListener extends ContextLoaderListener {

    private WebApplicationContext context;
    
    public EMFContextLoaderListener(WebApplicationContext context) {
        super(context);
        this.context = context;
    }
    
    /**
     * Initialize the root web application context.
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        initWebApplicationContext(event.getServletContext());
    }
}
