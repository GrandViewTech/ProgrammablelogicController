package org.grandviewtech.service.system;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SystemInfomation
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
				InetAddress inetAddress = InetAddress.getLocalHost();
				NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
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
			
	}
