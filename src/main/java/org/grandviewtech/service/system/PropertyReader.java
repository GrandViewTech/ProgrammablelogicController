package org.grandviewtech.service.system;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.grandviewtech.service.runtime.user.useractivity.Activities;
import org.grandviewtech.service.runtime.user.useractivity.Activity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class PropertyReader
	{
		private static Properties properties = new Properties();
		
		public static void loadProperties(String path) throws IOException
			{
				Properties temp = new Properties();
				Resource resource = new ClassPathResource(path);
				temp.load(resource.getInputStream());
				for (Object object : temp.keySet())
					{
						setProperty((String) object, temp.getProperty((String) object));
					}
			}
			
		public static void setProperty(String key, String value)
			{
				if (key != null && key.trim().length() > 0 && value != null && value.trim().length() > 0)
					{
						key = key.trim().toLowerCase();
						if (properties.contains(key))
							{
								properties.remove(key);
							}
						properties.setProperty(key, value);
					}
			}
			
		private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PropertyConfigurator.class);
		
		public static String getProperties(String key)
			{
				Activities activities = Activities.getInstance();
				if (properties != null && key != null && key.trim().length() > 0)
					{
						key = key.trim().toLowerCase();
						Object object = properties.get(key);
						String returnValue = (object == null) ? null : (String) object;
						if (returnValue == null)
							{
								String error = "Key-Value Pair for Key : " + key + " not Found";
								logger.error(error);
								return null;
							}
						else
							{
								return returnValue.trim();
							}
					}
				else
					{
						String error = "Properties Object is not been initialized";
						logger.error(error);
						try
							{
								activities.addActivity(new Activity(error, Activity.Category.SYSTEM));
							}
						catch (Exception exception)
							{
								logger.error("Unable to Add Activity For Key : " + key, exception);
							}
						return null;
					}
					
			}
	}
