package org.grandviewtech.constants;

import java.io.File;

import javax.swing.ImageIcon;

import org.grandviewtech.service.system.PropertyReader;

public class CustomIcon
	{
		final private static String		RESOURCE_PATH	= PropertyReader.getProperties("resourcePath");
		final private static String		ICON_PATH		= RESOURCE_PATH + File.separator + PropertyReader.getProperties("iconPath");
		final public static ImageIcon	NEW				= new ImageIcon(ICON_PATH + File.separator + "new.png");
		final public static ImageIcon	COMMENT			= new ImageIcon(ICON_PATH + File.separator + "comment.png");
		final public static ImageIcon	DELETE			= new ImageIcon(ICON_PATH + File.separator + "delete.png");
		final public static ImageIcon	COPY			= new ImageIcon(ICON_PATH + File.separator + "copy.png");
		final public static ImageIcon	PASTE			= new ImageIcon(ICON_PATH + File.separator + "paste.png");
		final public static ImageIcon	CUT				= new ImageIcon(ICON_PATH + File.separator + "cut.png");
		final public static ImageIcon	SETTING			= new ImageIcon(ICON_PATH + File.separator + "setting.png");
		final public static ImageIcon	EDIT			= new ImageIcon(ICON_PATH + File.separator + "edit.png");
		final public static ImageIcon	LOAD			= new ImageIcon(ICON_PATH + File.separator + "load.png");
		final public static ImageIcon	OUTPUT			= new ImageIcon(ICON_PATH + File.separator + "output.png");
		
	}
