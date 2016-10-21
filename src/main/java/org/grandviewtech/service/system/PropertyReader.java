package org.grandviewtech.service.system;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.grandviewtech.service.runtime.user.useractivity.Activities;
import org.grandviewtech.service.runtime.user.useractivity.Activity;

public class PropertyReader
	{
		private static Properties properties = new Properties();
		
		public static Properties getProperties()
			{
				return properties;
			}
			
		public static void setProperties(Properties properties)
			{
				PropertyReader.properties = properties;
			}
			
		private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PropertyConfigurator.class);
		
		public static String getProperties(String key)
			{
				Activities activities = Activities.getInstance();
				if (properties != null)
					{
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
