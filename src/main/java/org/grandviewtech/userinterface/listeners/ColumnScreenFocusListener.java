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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.grandviewtech.userinterface.misc.ActionBot;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ColumnScreenFocusListener implements FocusListener
	{
		
		private ColumnScreen	source;
		
		//private JLabel			setting;
		
		public ColumnScreenFocusListener(ColumnScreen source)
			{
				this.source = source;
				//this.setting = source.getSetting();
			}
			
		@Override
		public void focusGained(FocusEvent event)
			{
				/**if (source.isBlank() == false)
					{
						source.add(setting);
					}
					**/
				ActionBot.focusGained(source);
			}
			
		@Override
		public void focusLost(FocusEvent event)
			{
				/**if (source.isBlank() == false)
					{
						source.remove(setting);
					}**/
				ActionBot.focusLost(source);
			}
			
	}
