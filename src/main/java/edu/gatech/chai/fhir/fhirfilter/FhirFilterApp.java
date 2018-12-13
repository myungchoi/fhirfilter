package edu.gatech.chai.fhir.fhirfilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 
 */

@SpringBootApplication
public class FhirFilterApp extends SpringBootServletInitializer {
	
    public static void main( String[] args )
    {
        SpringApplication.run(FhirFilterApp.class, args);
    }
}
