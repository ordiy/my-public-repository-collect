package io.ordiy.github.data.ip_geoinfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IpToGeoInfoApplication {
	private static final Logger logger = LoggerFactory.getLogger(IpToGeoInfoApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(IpToGeoInfoApplication.class, args);
	}

}
