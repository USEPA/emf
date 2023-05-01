package gov.epa.emissions.framework.services.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class WebApplicationContextAware implements ApplicationContextAware {
    private ApplicationContext context;

    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }

}
