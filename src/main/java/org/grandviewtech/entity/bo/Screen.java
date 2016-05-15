package org.grandviewtech.entity.bo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.grandviewtech.constants.PLCConstant;
import org.grandviewtech.service.system.Printer;
import org.grandviewtech.userinterface.screen.RowScreen;
import org.grandviewtech.userinterface.screen.Sheet;

public class Screen implements Serializable
	{
		
		private static final long serialVersionUID = 5158611639703073759L;
		
		private Screen()
			{
			}
			
		private static class ScreenFactoryInstance
			{
				final static Screen instance = new Screen();
			};
			
		public static Screen getInstance()
			{
				return ScreenFactoryInstance.instance;
			}
			
		final private Sheet	sheet			= new Sheet();
		
		private int			totalRow		= 1;
		
		final private int	maxNumberOffRow	= 50;
		
		public int getMaxNumberOffRow()
			{
				return maxNumberOffRow;
			}
			
		public int getTotalRow()
			{
				return totalRow;
			}
			
		public void setTotalRow(int totalRow)
			{
				this.totalRow = totalRow;
			}
			
		public Sheet getSheet()
			{
				return sheet;
			}
			
		private List<RowScreen> rows = new ArrayList<RowScreen>();
		
		/**
		 * <br>
		 * 
		 * @Author : puneetsharma <br>
		 * @createdDate : 12-May-2016 <br>
		 * @createdTime : 11:14:21 am <br>
		 * @methodName : getRow <br>
		 * @methodPackage : org.grandviewtech.entity.bo <br>
		 * @Description : <br>
		 *              Do not modify the index before retriving the reduction of index from n to n-1 is taken care by the method itself.
		 * @StepsWithExplanation : <br>
		 * @TODO : <br>
		 * @param index
		 *            <br>
		 * @return
		 */
		public RowScreen getRow(int index)
			{
				int i$ = index - 1;
				if (i$ >= 0)
					{
						Printer.print("Retieving Row Number " + i$);
						return rows.get(i$);
					}
				else
					{
						throw new RuntimeException("Invald RowNumber "+i$);
					}
					
			}
			
		public void addRow(int index, RowScreen rowScreen)
			{
				int i$ = index - 1;
				if (i$ <= getMaxNumberOffRow())
					{
						Printer.print("Inserting Row Number " + i$);
						rows.add(i$, rowScreen);
					}
				else
					{
						throw new RuntimeException("Invald RowNumber" + i$);
					}
			}
			
	}
