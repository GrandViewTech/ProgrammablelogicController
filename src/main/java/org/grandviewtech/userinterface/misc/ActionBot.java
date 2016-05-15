package org.grandviewtech.userinterface.misc;

import java.awt.Color;

import javax.swing.BorderFactory;

import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.service.system.Printer;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.helper.CustomBorder;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public final class ActionBot
	{
		final static Screen SCREEN = Screen.getInstance();
		
		public static void defaultHalt()
			{
				try
					{
						Thread.sleep(100);
					}
				catch (InterruptedException exception)
					{
						exception.printStackTrace();
					}
			}
			
		public static void setBackGroundColor(ColumnScreen columnScreen)
			{
				columnScreen.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
			}
			
		public static void resetBackGroundColor(ColumnScreen columnScreen)
			{
				columnScreen.setBorder(new CustomBorder());
			}
			
		public static void focusGained(ColumnScreen columnScreen)
			{
				Printer.print("Focus Gained For " + columnScreen.getColumnNumber());
				ColumnScreenGenerator.createColumnNeighbourHood(SCREEN.getRow(columnScreen.getRowNumber()), columnScreen);
				setBackGroundColor(columnScreen);
				columnScreen.requestFocus();
			}
			
		public static void focusLost(ColumnScreen columnScreen)
			{
				Printer.print("Focus Lost For " + columnScreen.getColumnNumber());
				columnScreen.setAbove(null);
				columnScreen.setBelow(null);
				columnScreen.setPrevious(null);
				columnScreen.setNext(null);
				resetBackGroundColor(columnScreen);
				
			}
	}
