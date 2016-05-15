package org.grandviewtech.userinterface.misc;

import java.awt.Component;
import java.awt.Cursor;

public class Helper
	{
		final public static void setCursor(Component component)
			{
				component.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}		
	}
