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

public class Row implements Serializable
	{
		
		private static final long	serialVersionUID	= 5568769366616053277L;
		
		private int					rowNumber;
		
		private List<Column>		coloumns			= new ArrayList<Column>();
		
		public int getRowNumber()
			{
				return rowNumber;
			}
			
		public void setRowNumber(int rowNumber)
			{
				this.rowNumber = rowNumber;
			}
			
		public List<Column> getColoumns()
			{
				return coloumns;
			}
			
		public void setColoumns(List<Column> coloumns)
			{
				this.coloumns = coloumns;
			}
			
	}
