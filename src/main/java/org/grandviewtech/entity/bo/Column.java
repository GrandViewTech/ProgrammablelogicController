package org.grandviewtech.entity.bo;

import java.io.Serializable;

public class Column implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		private int					rowNumber;
		private int					columnNumber;
		private Coil				coil;
		
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
			
		public Coil getCoil()
			{
				return coil;
			}
			
		public void setCoil(Coil coil)
			{
				this.coil = coil;
			}
			
	}
