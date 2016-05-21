package org.grandviewtech.userinterface.helper;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;

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
				List<RowScreen> rowScreens = new ArrayList<RowScreen>();
				for (int i = startIndex; i < endIndex; i++)
					{
						gridBagConstraints.gridy = i;
						RowScreen row = new RowScreen(i);
						sheet.add(row, gridBagConstraints);
						screen.addRow(i, row);
						rowScreens.add(row);
					}
				sheet.revalidate();
				sheet.repaint();
			}
			
		public static void regenerateRow(int rowNumber)
			{
				gridBagConstraints.gridx = 1;
				gridBagConstraints.gridy = rowNumber;
				RowScreen row = new RowScreen(rowNumber);
				sheet.add(row, gridBagConstraints);
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
								updatedRowScreen.add(updateColumnNumber(rowScreen));
							}
						else if (currentRowNumber == rowNumber)
							{
								updatedRowScreen.add(row);
								updatedRowScreen.add(updateColumnNumber(rowScreen));
							}
							
					}
				screen.getRows().clear();
				screen.getRows().addAll(updatedRowScreen);
				sheet.revalidate();
				sheet.repaint();
			}
			
		
		
		private static RowScreen updateColumnNumber(RowScreen rowScreen)
			{
				Rung rung = rowScreen.getRung();
				rung.setRowNumber(rung.getRowNumber() + 1);
				for (ColumnScreen columnScreen : rowScreen.getAllColumnScreens())
					{
						columnScreen.setRowNumber(columnScreen.getRowNumber() + 1);
					}
				return rowScreen;
			}
			
	}
