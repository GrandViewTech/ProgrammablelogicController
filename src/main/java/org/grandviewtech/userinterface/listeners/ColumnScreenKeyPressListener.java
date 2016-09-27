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

import org.grandviewtech.constants.Borders;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.misc.ActionBot;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ColumnScreenKeyPressListener implements KeyListener, Borders
	{
		final static Screen		SCREEN	= Screen.getInstance();
		
		private ColumnScreen	source;
		
		public ColumnScreenKeyPressListener(ColumnScreen source)
			{
				this.source = source;
			}
			
		@Override
		public void keyTyped(KeyEvent keyEvent)
			{
				boolean defaultWait = true;
				onFocus(keyEvent, defaultWait);
				if ( keyEvent.getKeyCode() == KeyEvent.VK_CONTROL )
					{
						ClipBoard.setControlKeyActive(true);
					}
				else if ( keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE )
					{
						ClipBoard.cutLabel.setBorder(null);
						ClipBoard.copyLabel.setBorder(null);
						ClipBoard.pasteLabel.setBorder(null);
						actionOnEscape();
					}
			}
			
		@Override
		public void keyPressed(KeyEvent keyEvent)
			{
				if ( keyEvent.getKeyCode() == KeyEvent.VK_CONTROL )
					{
						ClipBoard.setControlKeyActive(true);
					}
				else if ( keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE )
					{
						actionOnEscape();
					}
			}
			
		@Override
		public void keyReleased(KeyEvent keyEvent)
			{
				boolean defaultWait = false;
				onFocus(keyEvent, defaultWait);
				if ( keyEvent.getKeyCode() == KeyEvent.VK_CONTROL )
					{
						ClipBoard.setControlKeyActive(false);
					}
			}
			
		private void onFocus(KeyEvent keyEvent, boolean defaultWait)
			{
				if ( defaultWait )
					{
						ActionBot.defaultHalt();
					}
				ColumnScreenGenerator.createColumnNeighbourHood(SCREEN.getRow(source.getRowNumber()), source);
				switch (keyEvent.getKeyCode())
					{
						case KeyEvent.VK_LEFT:
							{
								source.getPrevious(true);
								break;
							}
						case KeyEvent.VK_RIGHT:
							{
								source.getNext(true);
								break;
							}
						case KeyEvent.VK_UP:
							{
								source.getAbove(true);
								break;
							}
						case KeyEvent.VK_DOWN:
							{
								source.getBelow(true);
								break;
							}
					}
			}
			
		private void actionOnEscape()
			{
				ClipBoard.cutLabel.setBorder(PADDED_DEFAULT);
				ClipBoard.copyLabel.setBorder(PADDED_DEFAULT);
				ClipBoard.pasteLabel.setBorder(PADDED_DEFAULT);
				ClipBoard.resetClipBoardSelection();
			}
			
	}
