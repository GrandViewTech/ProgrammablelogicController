package org.grandviewtech.userinterface.helper;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;

import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.screen.RowScreen;
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
			
	}
