package org.grandviewtech.entity.bo;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.grandviewtech.constants.ApplicationConstant;
import org.grandviewtech.service.runtime.user.useractivity.Activities;
import org.grandviewtech.service.runtime.user.useractivity.Activity;
import org.grandviewtech.service.system.Printer;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;
import org.grandviewtech.userinterface.screen.Sheet;

public class Screen implements Serializable
	{
		private static Activities activities	   = Activities.getInstance();
		private static final long serialVersionUID = 5158611639703073759L;
		
		private Screen()
			{
			}
			
		private static class ScreenFactoryInstance
			{
				final static Screen instance = new Screen();
			}
			
		public static Screen getInstance()
			{
				return ScreenFactoryInstance.instance;
			}
			
		private Sheet		 sheet			 = new Sheet();
		private int			 totalRow		 = 1;
		private int			 endColumnNumber = ApplicationConstant.MAX_CELL;
		
		private int			 maxNumberOffRow = 50;
		private int			 endRowNumber	 = maxNumberOffRow;
		private ColumnScreen activeColumn	 = null;
		
		public Sheet getSheet()
			{
				return sheet;
			}
			
		public void setSheet(Sheet sheet)
			{
				this.sheet = sheet;
			}
			
		public int getEndRowNumber()
			{
				return endRowNumber;
			}
			
		public void setEndRowNumber(int endRowNumber)
			{
				this.endRowNumber = endRowNumber;
			}
			
		public int getEndColumnNumber()
			{
				return endColumnNumber;
			}
			
		public void setEndColumnNumber(int endColumnNumber)
			{
				this.endColumnNumber = endColumnNumber;
			}
			
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
		 *              Do not modify the index before retriving the reduction
		 *              of index from n to n-1 is taken care by the method
		 *              itself.
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
				return null;
				
			}
			
		public void removeAll()
			{
				rows.clear();
			}
			
		public void addRow(int index, RowScreen rowScreen)
			{
				int i$ = index - 1;
				if (i$ <= getTotalRow())
					{
						rows.add(i$, rowScreen);
					}
				else
					{
						throw new RuntimeException("Invald RowNumber " + i$);
					}
			}
			
		public List<RowScreen> getRows()
			{
				return rows;
			}
			
		public ColumnScreen getActiveColumn()
			{
				return activeColumn;
			}
			
		/*public void setActiveColumn(ColumnScreen activeColumn)
			{
				if (this.activeColumn != null)
					{
						// this.activeColumn.setBackground(null);
						activities.addActivity(new Activity("Cell( " + this.activeColumn.getRowNumber() + " , " + this.activeColumn.getColumnNumber() + " ) is Turned In-Active And Lost Focus", Activity.Category.USER));
					}
				if (activeColumn != null)
					{
						// activeColumn.setBackground(Color.LIGHT_GRAY);
						if (activeColumn.isBlank())
							{
								activities.addActivity(new Activity("Cell( " + activeColumn.getRowNumber() + " , " + activeColumn.getColumnNumber() + " ) is Turned Active But is Blank", Activity.Category.USER));
							}
						else
							{
								activities.addActivity(new Activity("Cell( " + activeColumn.getRowNumber() + " , " + activeColumn.getColumnNumber() + " ) is Turned Active", Activity.Category.USER));
							}
					}
				else
					{
						activities.addActivity(new Activity("Attempted was made to make empty cell active", Activity.Category.USER));
					}
					
				this.activeColumn = activeColumn;
				
			}*/
			
		public void setActiveColumn(ColumnScreen activeColumn)
			{
				if (this.activeColumn != null)
					{
						activities.addActivity(new Activity("Cell( " + this.activeColumn.getRowNumber() + " , " + this.activeColumn.getColumnNumber() + " ) is Turned In-Active And Lost Focus", Activity.Category.USER));
					}
				if (activeColumn != null)
					{
						if (activeColumn.isBlank())
							{
								activities.addActivity(new Activity("Cell( " + activeColumn.getRowNumber() + " , " + activeColumn.getColumnNumber() + " ) is Turned Active But is Blank", Activity.Category.USER));
							}
						else
							{
								activities.addActivity(new Activity("Cell( " + activeColumn.getRowNumber() + " , " + activeColumn.getColumnNumber() + " ) is Turned Active", Activity.Category.USER));
							}
					}
				else
					{
						activities.addActivity(new Activity("Attempted was made to make empty cell active", Activity.Category.USER));
					}
				this.activeColumn = activeColumn;
			}
	}
