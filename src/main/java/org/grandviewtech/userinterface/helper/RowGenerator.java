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
import java.util.Comparator;
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
			
		public static void regenerateRow(int rowNumber, boolean add)
			{
				List<RowScreen> updatedRowScreen = new ArrayList<RowScreen>();
				List<RowScreen> rowScreens = screen.getRows();
				Collections.sort(rowScreens, new Comparator<RowScreen>()
					{
						@Override
						public int compare(RowScreen o1, RowScreen o2)
							{
								Integer r1 = o1.getRowNumber();
								Integer r2 = o2.getRowNumber();
								return r1.compareTo(r2);
							}
					});
				for (RowScreen rowScreen : rowScreens)
					{
						int currentRowNumber = rowScreen.getRowNumber();
						
						if (currentRowNumber < rowNumber)
							{
								updatedRowScreen.add(rowScreen);
							}
						else if (currentRowNumber > rowNumber)
							{
								if (add)
									{
										currentRowNumber = currentRowNumber + 1;
										rowScreen.resetColumnScreen(currentRowNumber);
										updatedRowScreen.add(rowScreen);
									}
								else
									{
										currentRowNumber = currentRowNumber - 1;
										rowScreen.resetColumnScreen(currentRowNumber);
										updatedRowScreen.add(rowScreen);
									}
							}
						else if (currentRowNumber == rowNumber)
							{
								if (add)
									{
										updatedRowScreen.add(rowScreen);
										currentRowNumber = currentRowNumber + 1;
										updatedRowScreen.add(new RowScreen(currentRowNumber));
									}
							}
							
					}
				repaintScreen(updatedRowScreen);
			}
			
		public static void deleteRows(Set<Integer> rowNumbers)
			{
				List<RowScreen> updatedRowScreen = new ArrayList<RowScreen>();
				List<RowScreen> rowScreens = screen.getRows();
				Collections.sort(rowScreens, new Comparator<RowScreen>()
					{
						@Override
						public int compare(RowScreen o1, RowScreen o2)
							{
								Integer r1 = o1.getRowNumber();
								Integer r2 = o2.getRowNumber();
								return r1.compareTo(r2);
							}
					});
				int updatedRowNumber = 1;
				for (RowScreen rowScreen : rowScreens)
					{
						int currentRowNumber = rowScreen.getRowNumber();
						if (rowNumbers.contains(currentRowNumber) == false)
							{
								rowScreen.resetColumnScreen(updatedRowNumber);
								updatedRowScreen.add(rowScreen);
								updatedRowNumber = updatedRowNumber + 1;
							}
					}
				repaintScreen(updatedRowScreen);
			}
			
		public static void repaintScreen(List<RowScreen> rowScreens)
			{
				sheet.removeAll();
				//sheet.repaint();
				
				screen.setTotalRow(rowScreens.size());
				Collections.sort(rowScreens, new Comparator<RowScreen>()
					{
						@Override
						public int compare(RowScreen o1, RowScreen o2)
							{
								Integer r1 = o1.getRowNumber();
								Integer r2 = o2.getRowNumber();
								return r1.compareTo(r2);
							}
					});
				int i$ = 1;
				gridBagConstraints.gridx = 1;
				for (RowScreen rowScreen : rowScreens)
					{
						gridBagConstraints.gridy = i$;
						sheet.add(rowScreen, gridBagConstraints);
						screen.addRow(rowScreen.getRowNumber(), rowScreen);
						i$ = i$ + 1;
					}
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
								updatedRowScreen.add(updateColumnNumber(rowScreen, currentRowNumber - 1));
							}
					}
				updateScreen(updatedRowScreen);
			}
			
		private static RowScreen updateColumnNumber(RowScreen rowScreen, int rowNumber)
			{
				Rung rung = rowScreen.getRung();
				rung.setRowNumber(rowNumber);
				int index = 1;
				for (ColumnScreen columnScreen : rowScreen.getAllColumnScreens())
					{
						columnScreen.setRowNumber(rowNumber);
						rowScreen.addColumnScreen(index, columnScreen);
						index = index + 1;
					}
					
				rowScreen.setRung(rung);
				rowScreen.setRowNumber(rowNumber);
				//rowScreen.revalidate();
				//rowScreen.repaint();
				return rowScreen;
			}
			
		private static void updateScreen(List<RowScreen> updatedRowScreen)
			{
				gridBagConstraints.gridx = 1;
				//screen.getRows().clear();
				//sheet.removeAll();
				//sheet.revalidate();
				//sheet.repaint();
				screen.setTotalRow(updatedRowScreen.size());
				int i$ = 1;
				for (RowScreen rowScreen : updatedRowScreen)
					{
						//int i$ = rowScreen.getRowNumber();
						gridBagConstraints.gridy = i$;
						rowScreen.revalidate();
						rowScreen.repaint();
						//sheet.add(rowScreen, gridBagConstraints);
						screen.addRow(rowScreen.getRowNumber(), rowScreen);
						i$ = i$ + 1;
					}
				sheet.revalidate();
				sheet.repaint();
			}
	}
