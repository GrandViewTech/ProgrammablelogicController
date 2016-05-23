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

public class RowAndColumn
	{
		
		public RowAndColumn()
			{
				
			}
			
		private String	name;
		
		private int		rowNumber;
		
		private int		columnNumber;
		
		public int getRowNumber()
			{
				return rowNumber;
			}
			
		public void setRowNumber(int rowNumber)
			{
				this.rowNumber = rowNumber;
			}
			
		public int getColumnNumber()
			{
				return columnNumber;
			}
			
		public void setColumnNumber(int columnNumber)
			{
				this.columnNumber = columnNumber;
			}
			
		public String getName()
			{
				return name;
			}
			
		public void setName(String name)
			{
				this.name = name;
			}
	}