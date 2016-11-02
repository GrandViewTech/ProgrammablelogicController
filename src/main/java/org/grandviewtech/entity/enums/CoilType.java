package org.grandviewtech.entity.enums;

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

public enum CoilType
	{
	LABEL("LABEL"), END("END"), JUMP("JUMP"), OUTPUT("OUTPUT"), LINE("LINE"), LOAD("LOAD"), ROUTINE("ROUTINE"), LEFT_LINK("LEFT_LINK"), RIGHT_LINK("RIGHT_LINK"), PARALLEL("PARALLEL"), DEFAULT("DEFAULT"), DELETE("DELETE");
		private String coilType;
		
		private CoilType(String coilType)
			{
				this.coilType = coilType;
			}
			
		public String getCoilType()
			{
				return coilType;
			}
	}
