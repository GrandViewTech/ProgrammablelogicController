
package org.grandviewtech.userinterface.screen;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.grandviewtech.constants.PLCConstant;

public class RowScreen extends JPanel implements PLCConstant
	{
		
		private static final long	serialVersionUID	= 8194999359481289598L;
		
		private GridBagConstraints	gridBagConstraints	= new GridBagConstraints();
		
		private GridBagLayout		gridBagLayout		= new GridBagLayout();
		
		private int					rowNumber;
		
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
			
		public void createColumnScreen()
			{
				
				gridBagConstraints.gridx = 1;
				gridBagConstraints.gridy = 1;
				gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
				add(new Rung(rowNumber), gridBagConstraints);
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
	}
