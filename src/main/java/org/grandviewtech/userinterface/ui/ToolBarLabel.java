package org.grandviewtech.userinterface.ui;

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
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.Border;

public class ToolBarLabel extends JLabel
	{
		private static final long	serialVersionUID	= 1L;
		
		Border						paddingBorder		= BorderFactory.createEmptyBorder(2, 5, 5, 2);
		Border						border				= BorderFactory.createLineBorder(Color.white);
		
		public ToolBarLabel(Icon icon)
			{
				super();
				setBorder(BorderFactory.createCompoundBorder(border, paddingBorder));
				setIcon(icon);
			}
	}
