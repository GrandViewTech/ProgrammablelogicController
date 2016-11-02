package org.grandviewtech.constants;

import java.io.File;

import javax.swing.ImageIcon;

import org.grandviewtech.service.system.PropertyReader;

public class CustomIcon
	{
		final private static String		RESOURCE_PATH	= PropertyReader.getProperties("resourcePath");
		final public static String		ICON_PATH		= RESOURCE_PATH + File.separator + PropertyReader.getProperties("iconPath");
		final private static String		COIL_PATH		= ICON_PATH + File.separator + PropertyReader.getProperties("coilType");
		final public static ImageIcon	NEW				= new ImageIcon(ICON_PATH + File.separator + "new.png");
		final public static ImageIcon	COMMENT			= new ImageIcon(ICON_PATH + File.separator + "comment.png");
		final public static ImageIcon	DELETE			= new ImageIcon(ICON_PATH + File.separator + "delete.png");
		final public static ImageIcon	COPY			= new ImageIcon(ICON_PATH + File.separator + "copy.png");
		final public static ImageIcon	PASTE			= new ImageIcon(ICON_PATH + File.separator + "paste.png");
		final public static ImageIcon	CUT				= new ImageIcon(ICON_PATH + File.separator + "cut.png");
		final public static ImageIcon	SETTING			= new ImageIcon(ICON_PATH + File.separator + "setting.png");
		final public static ImageIcon	EDIT			= new ImageIcon(ICON_PATH + File.separator + "edit.png");
		final public static ImageIcon	COIL_LOAD		= new ImageIcon(COIL_PATH + File.separator + "load.png");
		final public static ImageIcon	COIL_OUTPUT		= new ImageIcon(COIL_PATH + File.separator + "output.png");
		final public static ImageIcon	ULTRA_LOGO		= new ImageIcon(ICON_PATH + File.separator + "ultra_logo.png");
		final public static ImageIcon	COIL_LEFT_LINK	= new ImageIcon(COIL_PATH + File.separator + "leftLink.png");
		final public static ImageIcon	COIL_RIGHT_LINK	= new ImageIcon(COIL_PATH + File.separator + "rightLink.png");
		final public static ImageIcon	COIL_PARALLEL	= new ImageIcon(COIL_PATH + File.separator + "Parallel.png");
		final public static ImageIcon	COIL_LINE		= new ImageIcon(COIL_PATH + File.separator + "line.png");
		final public static ImageIcon	COIL_JUMP		= new ImageIcon(COIL_PATH + File.separator + "jump.png");
		final public static ImageIcon	COIL_END		= new ImageIcon(COIL_PATH + File.separator + "end.png");
		final public static ImageIcon	COIL_ROUTINE	= new ImageIcon(COIL_PATH + File.separator + "routine.png");
		final public static ImageIcon	COIL_DELETE		= new ImageIcon(COIL_PATH + File.separator + "delete.png");
		final public static ImageIcon	COIL_LABEL		= new ImageIcon(COIL_PATH + File.separator + "label.png");
		
	}
