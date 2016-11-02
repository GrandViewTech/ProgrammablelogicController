package org.grandviewtech.userinterface.listeners;

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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.service.runtime.user.useractivity.Activities;
import org.grandviewtech.service.runtime.user.useractivity.Activity;
import org.grandviewtech.userinterface.misc.ActionBot;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ColumnScreenMouseClickListener implements MouseListener
	{
		private ColumnScreen	source;
		private boolean			isSelected	= false;
		Activities				activities	= Activities.getInstance();
		private Screen			screen		= Screen.getInstance();
		
		public ColumnScreenMouseClickListener(ColumnScreen columnScreen)
			{
				source = columnScreen;
			}
			
		@Override
		public void mouseClicked(MouseEvent eventvent)
			{
				if (isSelected == false)
					{
						isSelected = true;
						source.setBackground(Color.GREEN);
						activities.addActivity(new Activity("Row : " + source.getRowNumber() + " | Column : " + source.getColumnNumber() + " Gained Focus", Activity.Category.USER));
						screen.setActiveColumn(source);
						ActionBot.focusGained(source);
						
					}
				else
					{
						activities.addActivity(new Activity("Row : " + source.getRowNumber() + " | Column : " + source.getColumnNumber() + " Lost Focus", Activity.Category.USER));
						isSelected = false;
						screen.setActiveColumn(source);
						ActionBot.focusLost(source);
					}
					
			}
			
		@Override
		public void mousePressed(MouseEvent event)
			{
				source.setBackground(Color.GREEN);
			}
			
		@Override
		public void mouseReleased(MouseEvent event)
			{
				source.setBackground(null);
			}
			
		@Override
		public void mouseEntered(MouseEvent event)
			{
				//CustomToolBar.setPointerValue("Row : " + source.getRowNumber() + " Column : " + source.getColumnNumber());
				source.setToolTipText("Row : " + source.getRowNumber() + " Column : " + source.getColumnNumber());
				source.setBackground(Color.GRAY);
			}
			
		@Override
		public void mouseExited(MouseEvent event)
			{
				source.setBackground(null);
				//CustomToolBar.setPointerValue("");
			}
			
	}
