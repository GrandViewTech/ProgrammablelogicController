package org.grandviewtech.entity.bo;

import java.io.Serializable;

import org.grandviewtech.entity.enums.CoilType;

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

public abstract class Coil implements Serializable
	{
		private static final long	serialVersionUID	= 8104513205270112318L;
		private String				displayValue;
		private String				comment;
		private String				tag;
		private CoilType			coilType;
		
		public String getDisplayValue()
			{
				return displayValue;
			}
			
		public void setDisplayValue(String displayValue)
			{
				this.displayValue = displayValue;
			}
			
		public String getComment()
			{
				return comment;
			}
			
		public void setComment(String comment)
			{
				this.comment = comment;
			}
			
		public String getTag()
			{
				return tag;
			}
			
		public void setTag(String tag)
			{
				this.tag = tag;
			}
			
		public CoilType getCoilType()
			{
				return coilType;
			}
			
		public void setCoilType(CoilType coilType)
			{
				this.coilType = coilType;
			}
			
	}
