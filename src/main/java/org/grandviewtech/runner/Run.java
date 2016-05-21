package org.grandviewtech.runner;

import org.grandviewtech.service.system.SystemInformation;
import org.grandviewtech.userinterface.screen.BackGroundLayer;

public class Run
	{
		public static void main(String[] args)
			{
				SystemInformation.getSystemInfomation();
				BackGroundLayer backGroundLayer = new BackGroundLayer();
				backGroundLayer.init();
			}
	}
