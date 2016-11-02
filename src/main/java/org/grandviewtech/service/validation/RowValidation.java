package org.grandviewtech.service.validation;

import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.service.runtime.user.useractivity.Activities;
import org.grandviewtech.service.runtime.user.useractivity.Activity;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.screen.ColumnScreen;

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

public class RowValidation
	{
		private static Activities	activities		= Activities.getInstance();
		final static Screen			SCREEN			= Screen.getInstance();
		static boolean				isFocusRequired	= false;
		static boolean				error			= true;
		
		public static void validate(ColumnScreen columnScreen)
			{
				switch (columnScreen.getCoilType())
					{
						case DEFAULT:
							{
								break;
							}
						case END:
							{
								validateEnd(columnScreen);
								break;
							}
						case JUMP:
							{
								break;
							}
						case LABEL:
							{
								
								break;
							}
						case LEFT_LINK:
							{
								break;
							}
						case LINE:
							{
								break;
							}
						case LOAD:
							{
								validateLoad(columnScreen);
								break;
							}
						case OUTPUT:
							{
								break;
							}
						case PARALLEL:
							{
								break;
							}
						case RIGHT_LINK:
							{
								break;
							}
						case ROUTINE:
							{
								break;
							}
						
					}
					
			}
			
		public static void validateLoad(ColumnScreen columnScreen)
			{
				ColumnScreenGenerator.createColumnNeighbourHood(SCREEN.getRow(columnScreen.getRowNumber()), columnScreen);
				ColumnScreen previous = columnScreen.getPrevious(isFocusRequired);
				ColumnScreen next = columnScreen.getNext(isFocusRequired);
				ColumnScreen above = columnScreen.getAbove(isFocusRequired);
				ColumnScreen below = columnScreen.getBelow(isFocusRequired);
				if (previous != null && previous.isBlank())
					{
						hasError(columnScreen);
						columnScreen.setError(error);
					}
				else if (next != null && next.isBlank())
					{
						hasError(columnScreen);
						columnScreen.setError(error);
					}
				else
					{
						isErrorFree(columnScreen);
						columnScreen.setError(!error);
					}
				validateNeighBourHood(previous, next, above, below);
			}
			
		public static void validateEnd(ColumnScreen columnScreen)
			{
				ColumnScreenGenerator.createColumnNeighbourHood(SCREEN.getRow(columnScreen.getRowNumber()), columnScreen);
				ColumnScreen previous = columnScreen.getPrevious(isFocusRequired);
				ColumnScreen next = columnScreen.getNext(isFocusRequired);
				ColumnScreen above = columnScreen.getAbove(isFocusRequired);
				ColumnScreen below = columnScreen.getBelow(isFocusRequired);
				if (above == null || above.isBlank())
					{
						hasError(columnScreen);
						columnScreen.setError(error);
					}
				else if (previous != null && previous.isBlank())
					{
						hasError(columnScreen);
						columnScreen.setError(error);
					}
				else
					{
						isErrorFree(columnScreen);
						columnScreen.setError(!error);
					}
				validateNeighBourHood(previous, next, above, below);
			}
			
		public static void validateNeighBourHood(ColumnScreen columnScreen)
			{
				ColumnScreenGenerator.createColumnNeighbourHood(SCREEN.getRow(columnScreen.getRowNumber()), columnScreen);
				ColumnScreen previous = columnScreen.getPrevious(isFocusRequired);
				ColumnScreen next = columnScreen.getNext(isFocusRequired);
				ColumnScreen above = columnScreen.getAbove(isFocusRequired);
				ColumnScreen below = columnScreen.getBelow(isFocusRequired);
				validateNeighBourHood(previous, next, above, below);
			}
			
		private static void validateNeighBourHood(ColumnScreen previous, ColumnScreen next, ColumnScreen above, ColumnScreen below)
			{
				if (previous != null && previous.isError())
					{
						validate(previous);
					}
				if (next != null && next.isError())
					{
						validate(next);
					}
				if (above != null && above.isError())
					{
						validate(above);
					}
				if (below != null && below.isError())
					{
						validate(below);
					}
					
			}
			
		private static void isErrorFree(ColumnScreen columnScreen)
			{
				activities.addActivity(new Activity("Row : " + columnScreen.getRowNumber() + " Column : " + columnScreen.getColumnNumber() + " | Is Error Free", Activity.Category.USER));
			}
			
		private static void hasError(ColumnScreen columnScreen)
			{
				activities.addActivity(new Activity("Row : " + columnScreen.getRowNumber() + " Column : " + columnScreen.getColumnNumber() + " | Has Error", Activity.Category.USER));
			}
	}
