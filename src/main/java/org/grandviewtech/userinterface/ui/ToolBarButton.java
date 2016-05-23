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

import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JButton;

import org.grandviewtech.constants.PreferredDimension;

public class ToolBarButton extends JButton implements Serializable, PreferredDimension
	{
		
		private static final long serialVersionUID = -5924372108403206263L;
		
		public ToolBarButton()
			{
				setOpaque(false);
				setContentAreaFilled(false);
			}
			
		public ToolBarButton(Icon icon)
			{
				super(icon);
			}
			
		public ToolBarButton(String text, Icon icon)
			{
				super(text, icon);
			}
			
		public ToolBarButton(String text)
			{
				super(text);
			}
			
		/*@Override
		public Dimension getMaximumSize()
			{
				return TOOLBAR_BUTTON;
			}
			
		@Override
		public Dimension getMinimumSize()
			{
				return TOOLBAR_BUTTON;
			}*/
	}
