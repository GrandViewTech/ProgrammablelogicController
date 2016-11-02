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

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.grandviewtech.userinterface.helper.CustomBorder;

public class CustomBorderList
	{
		public static Border	PADDING					= BorderFactory.createEmptyBorder(2, 5, 5, 2);
		public static Border	WHITE_BORDER			= BorderFactory.createLineBorder(Color.white);
		public static Border	PADDED_DEFAULT			= BorderFactory.createCompoundBorder(WHITE_BORDER, PADDING);
		public static Border	DASHED_BORDER			= BorderFactory.createDashedBorder(java.awt.Color.BLUE);
		public static Border	DASHED_BORDER_RED		= BorderFactory.createDashedBorder(java.awt.Color.RED);
		public static Border	PADDED_DASHED_BORDER	= BorderFactory.createCompoundBorder(DASHED_BORDER, PADDING);
		public static Border	CUSTOM_BORDER			= new CustomBorder();
		public static Border	PADDING_BORDER			= BorderFactory.createEmptyBorder(2, 5, 5, 2);
		public static Border	BORDER					= BorderFactory.createLineBorder(Color.white);
		public static Border	ERROR_BORDER			= BorderFactory.createLineBorder(Color.RED);
		public static Border	EMPTY_BORDER			= BorderFactory.createEmptyBorder();
	}
