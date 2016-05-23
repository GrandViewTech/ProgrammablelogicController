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

final public class PlcFile
	{
		private PlcFile()
			{
			};
			
		private final static class PlCFileInstanceFactory
			{
				static PlcFile instance = new PlcFile();
			}
			
		public final static PlcFile getInstance()
			{
				return PlCFileInstanceFactory.instance;
			}
			
		private String fileName;
		
		public String getFileName()
			{
				return fileName;
			}
			
		public void setFileName(String fileName)
			{
				this.fileName = fileName;
			}
			
	}
