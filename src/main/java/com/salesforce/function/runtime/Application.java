package com.salesforce.function.runtime;

import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.salesforce.function.Function;

/**
 * Application initiator.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
        app.run(args);
    }

    @Bean
    public Function function() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(com.salesforce.function.Function.class));
        // FIXME: scope function impl lookup for faster start-up; figure out a better way
        Set<BeanDefinition> beanDefs = provider.findCandidateComponents("com");
        if (beanDefs == null || beanDefs.isEmpty()) {
            return null;
        }

        BeanDefinition beanDef = beanDefs.iterator().next();
        // REVIEWME: obviously, this assumes we can cleaning instantiate each function class
        Class<? extends Function> fxClass = (Class<? extends Function>) Class.forName(beanDef.getBeanClassName());
        return fxClass.newInstance();
    }

    @Bean
    public CommandLineRunner commandLineRunner(final ApplicationContext ctx) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                // FIXME: for debugging; remove eventually
                /*System.out.println("Beans:");
                String[] beanNames = ctx.getBeanDefinitionNames();
                Arrays.sort(beanNames);
                for (String beanName : beanNames) {
                    System.out.println(beanName);
                }*/
            }
        };
    }

}
