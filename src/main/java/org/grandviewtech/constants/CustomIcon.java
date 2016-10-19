package org.grandviewtech.constants;

import java.io.File;

import javax.swing.ImageIcon;

import org.grandviewtech.service.system.PropertyReader;

public class CustomIcon
	{
		private static String	RESOURCE_PATH	= PropertyReader.getProperties("resourcePath");
		private static String	ICON_PATH		= RESOURCE_PATH + File.separator + PropertyReader.getProperties("iconPath");
		public static ImageIcon	NEW				= new ImageIcon(ICON_PATH + File.separator + "new.png");
		public static ImageIcon	COMMENT			= new ImageIcon(ICON_PATH + File.separator + "comment.png");
		public static ImageIcon	DELETE			= new ImageIcon(ICON_PATH + File.separator + "delete.png");
		public static ImageIcon	COPY			= new ImageIcon(ICON_PATH + File.separator + "copy.png");
		public static ImageIcon	PASTE			= new ImageIcon(ICON_PATH + File.separator + "paste.png");
		public static ImageIcon	CUT				= new ImageIcon(ICON_PATH + File.separator + "cut.png");
		public static ImageIcon	SETTING			= new ImageIcon(ICON_PATH + File.separator + "setting.png");
		public static ImageIcon	EDIT			= new ImageIcon(ICON_PATH + File.separator + "edit.png");
		public static ImageIcon	LOAD_2			= new ImageIcon(ICON_PATH + File.separator + "load.ico");
		public static ImageIcon	LOAD			= new ImageIcon(ICON_PATH + File.separator + "load.png");
		
	}
