package org.grandviewtech.runner;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.grandviewtech.entity.bo.SystemPreference;
import org.grandviewtech.entity.bo.SystemPreference.CleanUpActivity;
import org.grandviewtech.service.system.Printer;
import org.grandviewtech.service.system.SystemFileLocation;
import org.grandviewtech.userinterface.screen.BackGroundLayer;

public class Application
	{
		
		private static org.apache.log4j.Logger	logger				= org.apache.log4j.Logger.getLogger(Application.class);
		
		private static Dimension				screenSize			= Toolkit.getDefaultToolkit().getScreenSize();
		private static SystemPreference			systemPreference	= new SystemPreference();
		
		public static void generateProcessInformation()
			{
				try
					{
						Double width = screenSize.getWidth();
						Double height = screenSize.getHeight();
						Printer.print("Deteching Screen Resolution  : Width : " + width.intValue() + " X  Hieght : " + height.intValue());
						String motherBoardUniqueInformation = getNetworkCardInformationToGetMACAddress();
						Printer.print(motherBoardUniqueInformation);
					}
				catch (Exception exception)
					{
						logger.error("Exception while loading System Properties", exception);
					}
			}
			
		private static void addHockToShutdownListener()
			{
				
				if (systemPreference.getCleanUpActivity().equals(CleanUpActivity.ON_SHUTDOWN))
					{
						Runtime.getRuntime().addShutdownHook(new Thread()
							{
								@Override
								public void run()
									{
										super.run();
										Application.deletePreviousFiles();
									}
							});
					}
			}
			
		public static void deletePreviousFiles()
			{
				try
					{
						if (!StringUtils.isBlank(SystemFileLocation.OUTPUT_FILE_LOCATION))
							{
								File folder = new File(SystemFileLocation.OUTPUT_FILE_LOCATION);
								if (folder.exists())
									{
										File[] files = folder.listFiles();
										for (File file : files)
											{
												FileUtils.forceDelete(file);
											}
									}
							}
					}
				catch (Exception exception)
					{
						logger.error(exception.getLocalizedMessage(), exception);
					}
			}
			
		private static String getNetworkCardInformationToGetMACAddress() throws UnknownHostException, SocketException
			{
				NetworkInterface networkInterface = null;
				boolean isMacFound = false;
				for (Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements();)
					{
						NetworkInterface netInterface = enumeration.nextElement();
						if (netInterface.getHardwareAddress() != null)
							{
								isMacFound = true;
								networkInterface = netInterface;
								break;
							}
					}
				if (isMacFound == true)
					{
						byte[] mac = networkInterface.getHardwareAddress();
						StringBuilder stringBuilder = new StringBuilder();
						for (int i = 0; i < mac.length; i++)
							{
								stringBuilder.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
							}
						String motherBoardUniqueInformation = stringBuilder.toString();
						motherBoardUniqueInformation = motherBoardUniqueInformation.replaceAll("-", "");
						return motherBoardUniqueInformation;
					}
				return "";
			}
			
		public static org.grandviewtech.entity.helper.Dimension calculateCenterAlignment(Dimension dimension)
			{
				org.grandviewtech.entity.helper.Dimension dm = new org.grandviewtech.entity.helper.Dimension();
				dm.setScreenHeight(screenSize.getHeight());
				dm.setScreenWidth(screenSize.getWidth());
				dm.setHeight(dimension.getHeight());
				dm.setWidth(dimension.getWidth());
				return dm;
			}
			
		public static void run()
			{
				try
					{
						if (systemPreference.getCleanUpActivity().equals(CleanUpActivity.ON_START_UP))
							{
								deletePreviousFiles();
							}
						generateProcessInformation();
						addHockToShutdownListener();
						SystemFileLocation.createRequiredFolderStructure();
						BackGroundLayer backGroundLayer = new BackGroundLayer();
						backGroundLayer.init();
					}
				catch (Exception exception)
					{
						logger.error(exception.getLocalizedMessage(), exception);
					}
			}
			
	}
