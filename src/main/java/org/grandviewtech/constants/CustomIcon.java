package org.grandviewtech.constants;

import java.io.File;

import javax.swing.ImageIcon;

import org.grandviewtech.service.system.PropertyReader;

public class CustomIcon
	{
		final private static String		RESOURCE_PATH			= PropertyReader.getProperties("resourcePath");
		final public static String		ICON_PATH				= RESOURCE_PATH + File.separator + PropertyReader.getProperties("iconPath");
		final private static String		COIL_PATH				= ICON_PATH + File.separator + PropertyReader.getProperties("coilTypeIconPath");
		final private static String		FILE_PATH				= ICON_PATH + File.separator + PropertyReader.getProperties("fileIconPath");
		final private static String		MISC_PATH				= ICON_PATH + File.separator + PropertyReader.getProperties("miscIconPath");
		final public static ImageIcon	NEW						= new ImageIcon(FILE_PATH + File.separator + "new.png");
		final public static ImageIcon	COMMENT					= new ImageIcon(FILE_PATH + File.separator + "comment.png");
		final public static ImageIcon	DELETE					= new ImageIcon(FILE_PATH + File.separator + "delete.png");
		final public static ImageIcon	COPY					= new ImageIcon(FILE_PATH + File.separator + "copy.png");
		final public static ImageIcon	PASTE					= new ImageIcon(FILE_PATH + File.separator + "paste.png");
		final public static ImageIcon	CUT						= new ImageIcon(FILE_PATH + File.separator + "cut.png");
		final public static ImageIcon	SETTING					= new ImageIcon(MISC_PATH + File.separator + "setting.png");
		final public static ImageIcon	EDIT					= new ImageIcon(FILE_PATH + File.separator + "edit.png");
		final public static ImageIcon	COIL_LOAD				= new ImageIcon(COIL_PATH + File.separator + "load.png");
		final public static ImageIcon	COIL_OUTPUT				= new ImageIcon(COIL_PATH + File.separator + "output.png");
		final public static ImageIcon	ULTRA_LOGO				= new ImageIcon(MISC_PATH + File.separator + "ultra.png");
		final public static ImageIcon	COIL_LEFT_LINK			= new ImageIcon(COIL_PATH + File.separator + "leftLink.png");
		final public static ImageIcon	COIL_RIGHT_LINK			= new ImageIcon(COIL_PATH + File.separator + "rightLink.png");
		final public static ImageIcon	COIL_PARALLEL			= new ImageIcon(COIL_PATH + File.separator + "Parallel.png");
		final public static ImageIcon	COIL_LINE				= new ImageIcon(COIL_PATH + File.separator + "line.png");
		final public static ImageIcon	COIL_JUMP				= new ImageIcon(COIL_PATH + File.separator + "jump.png");
		final public static ImageIcon	COIL_END				= new ImageIcon(COIL_PATH + File.separator + "end.png");
		final public static ImageIcon	COIL_ROUTINE			= new ImageIcon(COIL_PATH + File.separator + "routine.png");
		final public static ImageIcon	EDGE_ROUTINE			= new ImageIcon(COIL_PATH + File.separator + "edge.png");
		
		final public static ImageIcon	COIL_DELETE				= new ImageIcon(COIL_PATH + File.separator + "delete.png");
		final public static ImageIcon	COIL_LABEL				= new ImageIcon(COIL_PATH + File.separator + "label.png");
		final public static ImageIcon	ADVANCED_SEARCH_ICON	= new ImageIcon(MISC_PATH + File.separator + "advancedsearch.png");
		final public static ImageIcon	SEARCH_ICON				= new ImageIcon(MISC_PATH + File.separator + "search.png");
		final public static ImageIcon	RIGHT_ICON				= new ImageIcon(MISC_PATH + File.separator + "right.png");
		final public static ImageIcon	LEFT_ICON				= new ImageIcon(MISC_PATH + File.separator + "left.png");
		
	}
