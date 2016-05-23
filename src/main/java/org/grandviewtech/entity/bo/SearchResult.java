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
