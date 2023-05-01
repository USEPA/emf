package gov.epa.emissions.framework.services.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public final class WebApplicationContextUtils {
    private static WebApplicationContextUtils instance = new WebApplicationContextUtils();

    public static WebApplicationContextUtils getInstance() {
      return instance;
    }

    @Autowired
    private WebApplicationContextAware webApplicationContextAware;

    public ApplicationContext getApplicationContext() {
        return webApplicationContextAware.getApplicationContext();
    }
  }