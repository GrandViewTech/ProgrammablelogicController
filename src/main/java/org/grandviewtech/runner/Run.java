package org.grandviewtech.runner;

import org.grandviewtech.service.system.SystemInfomation;
import org.grandviewtech.userinterface.screen.BackGroundLayer;

public class Run
	{
		public static void main(String[] args)
			{
				BackGroundLayer backGroundLayer = new BackGroundLayer();
				backGroundLayer.init();
				SystemInfomation.getSystemInfomation();
			}
	}
