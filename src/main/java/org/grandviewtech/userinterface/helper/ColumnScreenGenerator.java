package org.grandviewtech.userinterface.helper;

import org.grandviewtech.constants.ApplicationConstant;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;

public class ColumnScreenGenerator
	
	{
		private static Screen screen = Screen.getInstance();
		
		public static void createColumnNeighbourHood(RowScreen rowScreen, ColumnScreen columnScreen)
			{
				if (columnScreen == null || rowScreen == null)
					{
						return;
					}
				left(rowScreen, columnScreen);
				right(rowScreen, columnScreen);
				above(rowScreen, columnScreen);
				below(rowScreen, columnScreen);
			}
			
		private static void above(RowScreen rowScreen, ColumnScreen columnScreen)
			{
				int aboveRowNumber = rowScreen.getRowNumber() - 1;
				if (aboveRowNumber > 0)
					{
						RowScreen aboveRow = screen.getRow(aboveRowNumber);
						ColumnScreen aboveColumnScreen = aboveRow.getColumnScreens(columnScreen.getColumnNumber());
						columnScreen.setAbove(aboveColumnScreen);
					}
			}
			
		private static void below(RowScreen rowScreen, ColumnScreen columnScreen)
			{
				int belowRowNumber = rowScreen.getRowNumber() + 1;
				if (belowRowNumber < screen.getTotalRow())
					{
						RowScreen belowRow = screen.getRow(belowRowNumber);
						ColumnScreen belowColumnScreen = belowRow.getColumnScreens(columnScreen.getColumnNumber());
						columnScreen.setBelow(belowColumnScreen);
					}
			}
			
		private static void left(RowScreen rowScreen, ColumnScreen columnScreen)
			{
				int priviousColumnNumber = columnScreen.getColumnNumber() - 1;
				if (priviousColumnNumber > 0)
					{
						ColumnScreen previous = rowScreen.getColumnScreens(priviousColumnNumber);
						columnScreen.setPrevious(previous);
					}
			}
			
		private static void right(RowScreen rowScreen, ColumnScreen columnScreen)
			{
				int nextColumnNumber = columnScreen.getColumnNumber() + 1;
				if (nextColumnNumber <= ApplicationConstant.MAX_CELL)
					{
						ColumnScreen next = rowScreen.getColumnScreens(nextColumnNumber);
						columnScreen.setNext(next);
					}
			}
	}
