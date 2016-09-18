package org.grandviewtech.runner;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;

public class Run
	{
		public static void main(String[] args)
			{
				SpringApplication springApplication = new SpringApplication(Application.class);
				springApplication.setHeadless(false);
				springApplication.setBannerMode(Banner.Mode.OFF);
				springApplication.setWebEnvironment(false);
				springApplication.run(args);
			}
	}
