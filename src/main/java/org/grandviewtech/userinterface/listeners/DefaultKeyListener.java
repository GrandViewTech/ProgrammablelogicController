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

import org.grandviewtech.constants.CustomBorderList;
import org.grandviewtech.entity.bo.ClipBoard;

public class DefaultKeyListener implements KeyListener
	{
		@Override
		public void keyTyped(KeyEvent keyEvent)
			{
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(true);
					}
				else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					{
						actionOnEscape();
					}
			}
			
		@Override
		public void keyPressed(KeyEvent keyEvent)
			{
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(true);
					}
				else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					{
						actionOnEscape();
					}
			}
			
		@Override
		public void keyReleased(KeyEvent keyEvent)
			{
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(false);
					}
			}
			
		private void actionOnEscape()
			{
				ClipBoard.cutLabel.setBorder(null);
				ClipBoard.copyLabel.setBorder(null);
				ClipBoard.pasteLabel.setBorder(null);
				ClipBoard.cutLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
				ClipBoard.copyLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
				ClipBoard.pasteLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
				ClipBoard.resetClipBoardSelection();
			}
	}
