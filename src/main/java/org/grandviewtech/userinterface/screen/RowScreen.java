
package org.grandviewtech.userinterface.screen;

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
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.grandviewtech.constants.PLCConstant;

public class RowScreen extends JPanel implements PLCConstant, Comparable<RowScreen>
	{
		
		private static final long	serialVersionUID	= 8194999359481289598L;
		
		private GridBagConstraints	gridBagConstraints	= new GridBagConstraints();
		
		private GridBagLayout		gridBagLayout		= new GridBagLayout();
		
		private int					rowNumber;
		
		private Rung				rung;
		
		public RowScreen(int rowNumber)
			{
				this.rowNumber = rowNumber;
				setLayout(gridBagLayout);
				createColumnScreen();
			}
			
		private ColumnScreen[] columnScreens = new ColumnScreen[MAX_CELL];
		
		public ColumnScreen[] getAllColumnScreens()
			{
				return columnScreens;
			}
			
		public ColumnScreen getColumnScreens(int i$)
			{
				return columnScreens[i$ - 1];
			}
			
		public void addColumnScreen(int i$, ColumnScreen columnScreen)
			{
				this.columnScreens[i$ - 1] = (columnScreen);
			}
			
		public int getRowNumber()
			{
				return this.rowNumber;
			}
			
		public void resetColumnScreen()
			{
				gridBagConstraints.gridx = 1;
				gridBagConstraints.gridy = 1;
				gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
				rung = new Rung(rowNumber);
				add(rung, gridBagConstraints);
				int columnNumber = 1;
				for (ColumnScreen columnScreen : columnScreens)
					{
						gridBagConstraints.gridx = columnNumber + 1;
						columnScreen.setColumnNumber(columnNumber);
						columnScreen.setRowNumber(rowNumber);
						columnScreen.revalidate();
						columnScreen.repaint();
						add(columnScreen, gridBagConstraints);
						columnNumber = columnNumber + 1;
					}
				revalidate();
				repaint();
			}
			
		public void createColumnScreen()
			{
				gridBagConstraints.gridx = 1;
				gridBagConstraints.gridy = 1;
				gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
				rung = new Rung(rowNumber);
				add(rung, gridBagConstraints);
				int columnNumber = 1;
				while (columnNumber <= MAX_CELL)
					{
						gridBagConstraints.gridx = columnNumber + 1;
						ColumnScreen columnScreen = new ColumnScreen();
						columnScreen.setColumnNumber(columnNumber);
						columnScreen.setRowNumber(rowNumber);
						columnScreen.revalidate();
						columnScreen.repaint();
						addColumnScreen(columnNumber, columnScreen);
						add(columnScreen, gridBagConstraints);
						columnNumber = columnNumber + 1;
					}
				revalidate();
				repaint();
			}
			
		public Rung getRung()
			{
				return rung;
			}
			
		public void setRung(Rung rung)
			{
				this.rung = rung;
			}
			
		public void setRowNumber(int rowNumber)
			{
				this.rowNumber = rowNumber;
			}
			
		@Override
		public int compareTo(RowScreen comparableRowScreen)
			{
				Integer currentRowNumber = rowNumber;
				Integer comparableRowNumber = comparableRowScreen.getRowNumber();
				return currentRowNumber.compareTo(comparableRowNumber);
			}
			
	}
