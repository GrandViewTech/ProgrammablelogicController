package org.grandviewtech.entity.bo;

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
