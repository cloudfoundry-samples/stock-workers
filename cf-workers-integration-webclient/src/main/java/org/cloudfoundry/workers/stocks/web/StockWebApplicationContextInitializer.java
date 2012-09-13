package org.cloudfoundry.workers.stocks.web;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * this class is called before the DispatcherServlet machinery is created,
 * and gives us a chance to influence the {@link org.springframework.context.ApplicationContext}
 * profiles.
 *
 * @author Josh Long
 */
public class StockWebApplicationContextInitializer implements ApplicationContextInitializer<AnnotationConfigWebApplicationContext> {

    private CloudEnvironment cloudEnvironment = new CloudEnvironment();

    private boolean isCloudFoundry() {
        return cloudEnvironment.isCloudFoundry();
    }

    @Override
    public void initialize(AnnotationConfigWebApplicationContext applicationContext) {


        String profile;
        if (isCloudFoundry()) {
            profile = "cloud";
        } else {
            profile = "local";
        }

        applicationContext.getEnvironment().setActiveProfiles(profile);


        Class<?>[] configs = {WebMvcConfiguration.class};
        String[] basePkgs = new String[configs.length];
        int i = 0;
        for (Class<?> pkg : configs)
            basePkgs[i++] = pkg.getPackage().getName();

        applicationContext.scan(basePkgs);
        applicationContext.refresh();
    }
}
