package org.grandviewtech.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.grandviewtech.service.system.PropertyReader;
import org.grandviewtech.service.system.SystemFileLocation;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class Run
	{
		private static Logger		logger	= Logger.getLogger(Run.class);
		private static Properties	properties;
		
		static
			{
				initializeLogsProperties();
				initializeProperties();
			}
			
		private static void initializeLogsProperties()
			{
				try
					{
						Resource resource = new ClassPathResource("properties" + File.separator + "log4j.properties");
						Properties properties = new Properties();
						properties.load(resource.getInputStream());
						PropertyConfigurator.configure(properties);
						logger.info("Congifuring logger");
					}
				catch (Exception exception)
					{
						logger.error("Exception while loading Log4j.properties", exception);
					}
			}
			
		private static void initializeProperties()
			{
				try
					{
						
						properties = PropertyReader.getProperties();
						String path = "properties" + File.separator + "application.properties";
						Resource resource = new ClassPathResource(path);
						properties.load(resource.getInputStream());
						PropertyReader.setProperties(properties);
						//
						Properties systemProperties = new Properties();
						String pid_HostName = ManagementFactory.getRuntimeMXBean().getName();
						if ( pid_HostName != null )
							{
								if ( pid_HostName.contains("@") )
									{
										String[] data = org.apache.commons.lang3.StringUtils.split(pid_HostName, "@");
										properties.put("pId".toLowerCase(), data[0]);
										properties.put("hostName".toLowerCase(), data[1]);
										properties.put("serialNumber".toLowerCase(), "" + (new BigInteger(data[1].getBytes())));
										systemProperties.put("pId".toLowerCase(), data[0]);
										systemProperties.put("hostName".toLowerCase(), data[1]);
										systemProperties.put("serialNumber".toLowerCase(), "" + (new BigInteger(data[1].getBytes())));
									}
							}
						String FOLDER = SystemFileLocation.SYSTEM_FILE_LOCATION;
						File folder = new File(FOLDER);
						if ( !folder.exists() )
							{
								FileUtils.forceMkdir(folder);
							}
						String FILENAME = FOLDER + File.separator + PropertyReader.getProperties("systemFileName");
						File file = new File(FILENAME);
						OutputStream output = new FileOutputStream(file);
						DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
						systemProperties.store(output, "updating system properties on " + dateFormat.format(new Date()));
					}
				catch (Exception exception)
					{
						logger.error("Exception while loading application.properties", exception);
					}
			}
			
		public static void main(String[] args)
			{
				SpringApplication springApplication = new SpringApplication(Application.class);
				springApplication.setHeadless(false);
				springApplication.setBannerMode(Banner.Mode.OFF);
				springApplication.setWebEnvironment(false);
				springApplication.run(args);
			}
	}
