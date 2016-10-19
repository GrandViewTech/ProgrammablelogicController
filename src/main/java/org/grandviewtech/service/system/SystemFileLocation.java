package org.grandviewtech.service.system;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.log4j.Logger;
import org.grandviewtech.runner.Application;
import org.grandviewtech.service.runtime.user.useractivity.Activities;
import org.grandviewtech.service.runtime.user.useractivity.Activity;

public class SystemFileLocation
	{
		private static Logger			logger					= Logger.getLogger(SystemFileLocation.class);
		public final static String		PROCESS_ID				= (Application.getProperties("pId") != null) ? Application.getProperties("pId") : "PROCESS_ID";
		public final static String		OUTPUT_FILE_LOCATION	= PropertyReader.getProperties("outputPath");
		public final static String		SYSTEM_FILE_LOCATION	= OUTPUT_FILE_LOCATION + File.separator + PROCESS_ID + File.separator + PropertyReader.getProperties("systemFilePath");
		public final static String		INDEX_FILE_LOCATION		= OUTPUT_FILE_LOCATION + File.separator + PROCESS_ID + File.separator + PropertyReader.getProperties("indexPath");
		public final static String		ACTIVITY_FILE_LOCATION	= OUTPUT_FILE_LOCATION + File.separator + PROCESS_ID + File.separator + PropertyReader.getProperties("activityFilePath");
		public final static Activities	activities				= Activities.getInstance();
		
		synchronized public static void createRequiredFolderStructure()
			{
				try
					{
						
						String clazzName = "org.grandviewtech.service.system.SystemFileLocation";
						Class<?> clazz = Class.forName(clazzName);
						boolean forceAccess = true;
						List<String> folderList = new LinkedList<String>();
						for (Field field : clazz.getDeclaredFields())
							{
								field.setAccessible(true);
								String fieldName = field.getName();
								if ( fieldName.contains("FILE_LOCATION") )
									{
										try
											{
												String value = (String) FieldUtils.readDeclaredStaticField(clazz, fieldName, forceAccess);
												if ( !StringUtils.isBlank(value) && !StringUtils.isEmpty(value) )
													{
														String folderName = value;
														generateFolder(folderName);
														folderList.add(folderName);
													}
											}
										catch (IllegalAccessException illegalAccessException)
											{
												logger.error("Error while Generating Folder Structure Using Reflection For Class : " + clazzName + " | FieldName : " + fieldName, illegalAccessException);
											}
									}
							}
						for (String folder : folderList)
							{
								activities.addActivity(new Activity("Generating Folder : " + folder, Activity.Category.SYSTEM));
							}
					}
				catch (ClassNotFoundException classNotFoundException)
					{
						logger.error("Error while Generating Folder Structure Using Reflection", classNotFoundException);
					}
			}
			
		private static void generateFolder(String folderName)
			{
				File folder = new File(folderName);
				if ( !folder.exists() )
					{
						try
							{
								FileUtils.forceMkdir(folder);
							}
						catch (IOException ioException)
							{
								logger.error("Error while Generating Folder : " + folderName, ioException);
							}
					}
			}
	}
