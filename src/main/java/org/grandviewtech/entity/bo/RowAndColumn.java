package org.grandviewtech.entity.bo;

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