package org.grandviewtech.constants;

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

import java.awt.Dimension;

public interface PreferredDimension extends PLCConstant
	{
		
		Dimension	TOOLBAR_BUTTON			= new Dimension(15, 10);
		Dimension	MINIMUM_FRAME_SIZE		= new Dimension(1200, 700);
		Dimension	CELL_SIZE				= new Dimension(SECTION_WIDTH, SECTION_HEIGHT);
		Dimension	RUNG_SIZE				= new Dimension(20, SECTION_HEIGHT);
		Dimension	CONFIGURATION_SCREEN	= new Dimension(350, 300);
		//Dimension	CONFIGURATION_SCREEN	= new Dimension(500, 200);
	}
