package org.grandviewtech.service.runtime.user.useractivity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.grandviewtech.service.system.PropertyReader;
import org.grandviewtech.service.system.SystemFileLocation;

public class Activities
	{
		private static Logger		logger						= Logger.getLogger(Activities.class);
		final private static String	USER_ACTIVITY_FILE_NAME		= PropertyReader.getProperties("userActivityFileName");
		final private static String	SYSTEM_ACTIVITY_FILE_NAME	= PropertyReader.getProperties("systemActivityFileName");
		private static boolean		activityFileGenerated		= false;
		private Queue<Activity>		activityQueue				= new LinkedList<Activity>();
		
		public static Activities getInstance()
			{
				return ActivityInitializer.getActivities();
			}
			
		private Activities()
			{
				
			}
			
		private static class ActivityInitializer
			{
				private static Activities activities;
				
				public static Activities getActivities()
					{
						if (activities == null)
							{
								activities = new Activities();
							}
						return activities;
					}
					
			}
			
		public void addActivity(Activity activity)
			{
				logger.debug(activity.toString());
				switch (activity.getCategory())
					{
						case SYSTEM:
							{
								systemActivityWriter(activity);
								break;
							}
						case USER:
							{
								userActivityWriter(activity);
								break;
							}
						case DEFAULT:
							{
								systemActivityWriter(activity);
								break;
							}
						default:
							{
								systemActivityWriter(activity);
								break;
							}
					}
				this.activityQueue.add(activity);
			}
			
		private void userActivityWriter(Activity activity)
			{
				writeOutput(SystemFileLocation.ACTIVITY_FILE_LOCATION + File.separator + USER_ACTIVITY_FILE_NAME, activity.getMessage());
			}
			
		private void systemActivityWriter(Activity activity)
			{
				writeOutput(SystemFileLocation.ACTIVITY_FILE_LOCATION + File.separator + SYSTEM_ACTIVITY_FILE_NAME, activity.getMessage());
			}
			
		private void writeOutput(String fileName, String text)
			{
				try
					{
						generateActivityFiles();
						Files.write(Paths.get(fileName), text.getBytes(), StandardOpenOption.APPEND);
					}
				catch (IOException ioException)
					{
						logger.error("Exception while writing activity : " + text + " to file " + fileName, ioException);
						
					}
			}
			
		private void generateActivityFiles()
			{
				if (!activityFileGenerated)
					{
						File userActivity = new File(SystemFileLocation.ACTIVITY_FILE_LOCATION + File.separator + USER_ACTIVITY_FILE_NAME);
						if (userActivity.exists() == false)
							{
								try
									{
										userActivity.createNewFile();
									}
								catch (IOException ioException)
									{
										logger.error("Exception while creating user activity file", ioException);
									}
							}
						File systemActivity = new File(SystemFileLocation.ACTIVITY_FILE_LOCATION + File.separator + USER_ACTIVITY_FILE_NAME);
						if (systemActivity.exists() == false)
							{
								try
									{
										systemActivity.createNewFile();
									}
								catch (IOException ioException)
									{
										logger.error("Exception while creating system activity file", ioException);
									}
							}
						activityFileGenerated = true;
					}
			}
	}
