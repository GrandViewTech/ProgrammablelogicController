package org.grandviewtech.service.system;

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
						String motherBoardUniqueInformation = getNetworkCardInformationToGetMACAddress();
						Printer.print(motherBoardUniqueInformation);
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
