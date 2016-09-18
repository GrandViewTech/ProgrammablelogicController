package org.grandviewtech.service.system;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.management.ManagementFactory;

/*
 * #%L
 * Programmable Login Controller Inteface
 * %%
 * Copyright (C) 2016 GrandViewTech
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class SystemInformation
	{
		
		public static void getSystemInfomation()
			{
				try
					{
						Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
						Double width = screenSize.getWidth();
						Double height = screenSize.getHeight();
						Printer.print("Deteching Screen Resolution  : Width : " + width.intValue() + " X  Hieght : " + height.intValue());
						String motherBoardUniqueInformation = getNetworkCardInformationToGetMACAddress();
						Printer.print(motherBoardUniqueInformation);
						String name = ManagementFactory.getRuntimeMXBean().getName();
						Printer.print("Launching Process with PID : " + name);
					}
				catch (Exception exception)
					{
						exception.printStackTrace();
					}
			}
			
		private static String getNetworkCardInformationToGetMACAddress() throws UnknownHostException, SocketException
			{
				//	InetAddress inetAddress = InetAddress.getLocalHost();
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
			
	}
