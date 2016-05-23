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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ColumnValueKeyPressListener implements KeyListener
	{
		private ColumnScreen columnScreen;
		
		public ColumnValueKeyPressListener(ColumnScreen columnScreen)
			{
				this.columnScreen = columnScreen;
			}
			
		@Override
		public void keyTyped(KeyEvent keyEvent)
			{
				
			}
			
		@Override
		public void keyPressed(KeyEvent keyEvent)
			{
				
			}
			
		@Override
		public void keyReleased(KeyEvent keyEvent)
			{
			}
			
	}
