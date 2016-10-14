package org.grandviewtech.runner;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.grandviewtech.service.system.Printer;
import org.grandviewtech.service.system.PropertyReader;
import org.grandviewtech.userinterface.screen.BackGroundLayer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
@SpringBootApplication
@ComponentScan(basePackages = { "com.grandviewtech" })
public class Application implements CommandLineRunner
	{
		
		private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Application.class);
		
		@Bean
		public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException
			{
				PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
				return propertySourcesPlaceholderConfigurer;
			}
			
		private static Properties	properties;
		
		private final static String	FOLDER		= "output" + java.io.File.separator + "system";
		private static String		FILENAME	= FOLDER + java.io.File.separator + "system.properties";
		
		private static Dimension	screenSize	= Toolkit.getDefaultToolkit().getScreenSize();
		
		static
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
						logger.error(exception.getLocalizedMessage(), exception);
					}
			}
			
		public static void getSystemInfomation()
			{
				try
					{
						Double width = screenSize.getWidth();
						Double height = screenSize.getHeight();
						Printer.print("Deteching Screen Resolution  : Width : " + width.intValue() + " X  Hieght : " + height.intValue());
						String motherBoardUniqueInformation = getNetworkCardInformationToGetMACAddress();
						Printer.print(motherBoardUniqueInformation);
						String name = ManagementFactory.getRuntimeMXBean().getName();
						Printer.print("Launching Process with PID : " + name);
						
						properties = new Properties();
						String pid_HostName = ManagementFactory.getRuntimeMXBean().getName();
						if (pid_HostName != null)
							{
								if (pid_HostName.contains("@"))
									{
										String[] data = org.apache.commons.lang3.StringUtils.split(pid_HostName, "@");
										properties.put("pId".toLowerCase(), data[0]);
										properties.put("hostName".toLowerCase(), data[1]);
										properties.put("serialNumber".toLowerCase(), "" + (new BigInteger(data[1].getBytes())));
									}
							}
						File folder = new File(FOLDER);
						if (!folder.exists())
							{
								FileUtils.forceMkdir(folder);
							}
						File file = new File(FILENAME);
						OutputStream output = new FileOutputStream(file);
						DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
						properties.store(output, "updating system properties on " + dateFormat.format(new Date()));
					}
				catch (Exception exception)
					{
						exception.printStackTrace();
					}
			}
			
		private static void addHockToShutdownListener()
			{
				Runtime.getRuntime().addShutdownHook(new Thread()
					{
						@Override
						public void run()
							{
								super.run();
								Application.deletePreviousIndexes();
							}
					});
			}
			
		public static void deletePreviousIndexes()
			{
				try
					{
						String indexPath = PropertyReader.getProperties("indexPath");
						if (!StringUtils.isBlank(indexPath))
							{
								File folder = new File(indexPath);
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
				// InetAddress inetAddress = InetAddress.getLocalHost();
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
			
		public static String getProperties(String key)
			{
				key = key.trim().toLowerCase();
				Object object = properties.get(key);
				if (object != null)
					{
						return (String) object;
					}
				else
					{
						return "";
					}
			}
			
		@Override
		public void run(String... arg0) throws Exception
			{
				try
					{
						deletePreviousIndexes();
						getSystemInfomation();
						addHockToShutdownListener();
						BackGroundLayer backGroundLayer = new BackGroundLayer();
						backGroundLayer.init();
					}
				catch (Exception exception)
					{
						logger.error(exception.getLocalizedMessage(), exception);
					}
			}
			
	}
