package org.grandviewtech.userinterface.helper;

/*
 * #%L
 * Programmable Login Controller Inteface
 * %%
 * Copyright (C) 2016 GrandViewTech
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.grandviewtech.constants.PLCConstant;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;

public class ColumnScreenGenerator implements PLCConstant
	
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
				if (nextColumnNumber <= MAX_CELL)
					{
						ColumnScreen next = rowScreen.getColumnScreens(nextColumnNumber);
						columnScreen.setNext(next);
					}
			}
	}
