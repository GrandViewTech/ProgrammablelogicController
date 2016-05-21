package org.grandviewtech.entity.bo;

import org.apache.lucene.document.Document;

public class SearchResult
	{
		public SearchResult()
			{
			}
			
		public SearchResult(Document document)
			{
				this.columnId = document.get("columnId");
				
				this.rowNumber = (document.get("rowNumber") == null) ? null : Integer.parseInt(document.get("rowNumber"));
				this.columnNumber = (document.get("columnNumber") == null) ? null : Integer.parseInt(document.get("columnNumber"));
				this.value = document.get("value");
				this.coil = document.get("coil");
				this.tag = document.get("tag");
				
			}
			
		private String	columnId;
		private Integer	rowNumber;
		private Integer	columnNumber;
		private String	tag;
		private String	value;
		private String	coil;
		
		public String getColumnId()
			{
				return columnId;
			}
			
		public void setColumnId(String columnId)
			{
				this.columnId = columnId;
			}
			
		public Integer getRowNumber()
			{
				return rowNumber;
			}
			
		public void setRowNumber(Integer rowNumber)
			{
				this.rowNumber = rowNumber;
			}
			
		public Integer getColumnNumber()
			{
				return columnNumber;
			}
			
		public void setColumnNumber(Integer columnNumber)
			{
				this.columnNumber = columnNumber;
			}
			
		public String getTag()
			{
				return tag;
			}
			
		public void setTag(String tag)
			{
				this.tag = tag;
			}
			
		public String getValue()
			{
				return value;
			}
			
		public void setValue(String value)
			{
				this.value = value;
			}
			
		public String getCoil()
			{
				return coil;
			}
			
		public void setCoil(String coil)
			{
				this.coil = coil;
			}
			
	}
