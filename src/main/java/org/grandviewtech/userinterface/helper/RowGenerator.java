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

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;
import org.grandviewtech.userinterface.screen.Rung;
import org.grandviewtech.userinterface.screen.Sheet;

public class RowGenerator
	{
		private static Screen				screen				= Screen.getInstance();
		private static Sheet				sheet				= screen.getSheet();
		private static GridBagConstraints	gridBagConstraints	= sheet.gridBagConstraints;
		
		public static void regenerateRow()
			{
				gridBagConstraints.gridx = 1;
				int startIndex = screen.getTotalRow();
				int endIndex = screen.getTotalRow() + screen.getMaxNumberOffRow();
				screen.setTotalRow(endIndex);
				for (int i = startIndex; i < endIndex; i++)
					{
						gridBagConstraints.gridy = i;
						RowScreen row = new RowScreen(i);
						sheet.add(row, gridBagConstraints);
						screen.addRow(i, row);
					}
				sheet.revalidate();
				sheet.repaint();
			}
			
		public static void regenerateRow(int rowNumber)
			{
				RowScreen row = new RowScreen(rowNumber);
				List<RowScreen> updatedRowScreen = new ArrayList<RowScreen>();
				for (RowScreen rowScreen : screen.getRows())
					{
						int currentRowNumber = rowScreen.getRowNumber();
						
						if (currentRowNumber < rowNumber)
							{
								updatedRowScreen.add(rowScreen);
							}
						else if (currentRowNumber > rowNumber)
							{
								updatedRowScreen.add(updateColumnNumber(rowScreen, currentRowNumber + 1));
							}
						else if (currentRowNumber == rowNumber)
							{
								updatedRowScreen.add(row);
								updatedRowScreen.add(updateColumnNumber(rowScreen, currentRowNumber + 1));
							}
					}
				updateScreen(updatedRowScreen);
			}
			
		public static void deleteRow(Set<Integer> deletedRowNumbers)
			{
				List<RowScreen> updatedRowScreen = new ArrayList<RowScreen>();
				for (RowScreen rowScreen : screen.getRows())
					{
						int currentRowNumber = rowScreen.getRowNumber();
						if (deletedRowNumbers.contains(currentRowNumber) == false)
							{
								updatedRowScreen.add(rowScreen);
							}
					}
				gridBagConstraints.gridx = 1;
				screen.getRows().clear();
				sheet.removeAll();
				screen.setTotalRow(updatedRowScreen.size());
				int i$ = 1;
				Collections.sort(updatedRowScreen);
				for (RowScreen rowScreen : updatedRowScreen)
					{
						gridBagConstraints.gridy = i$;
						updateColumnNumber(rowScreen, i$);
						sheet.add(rowScreen, gridBagConstraints);
						screen.addRow(rowScreen.getRowNumber(), rowScreen);
						i$ = i$ + 1;
					}
				sheet.revalidate();
				sheet.repaint();
			}
			
		public static void deleteRow(int rowNumber)
			{
				List<RowScreen> updatedRowScreen = new ArrayList<RowScreen>();
				for (RowScreen rowScreen : screen.getRows())
					{
						int currentRowNumber = rowScreen.getRowNumber();
						
						if (currentRowNumber < rowNumber)
							{
								updatedRowScreen.add(rowScreen);
							}
						else if (currentRowNumber > rowNumber)
							{
								updatedRowScreen.add(updateColumnNumber(rowScreen, rowNumber - 1));
							}
					}
				updateScreen(updatedRowScreen);
			}
			
		private static RowScreen updateColumnNumber(RowScreen rowScreen, int rowNumber)
			{
				Rung rung = rowScreen.getRung();
				rung.setRowNumber(rowNumber);
				for (ColumnScreen columnScreen : rowScreen.getAllColumnScreens())
					{
						columnScreen.setRowNumber(rowNumber);
					}
				rowScreen.setRowNumber(rowNumber);
				return rowScreen;
			}
			
		private static void updateScreen(List<RowScreen> updatedRowScreen)
			{
				gridBagConstraints.gridx = 1;
				screen.getRows().clear();
				sheet.removeAll();
				screen.setTotalRow(updatedRowScreen.size());
				for (RowScreen rowScreen : updatedRowScreen)
					{
						int i$ = rowScreen.getRowNumber();
						gridBagConstraints.gridy = i$;
						sheet.add(rowScreen, gridBagConstraints);
						screen.addRow(rowScreen.getRowNumber(), rowScreen);
					}
				sheet.revalidate();
				sheet.repaint();
			}
	}
