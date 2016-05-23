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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.enums.CLIPBOARD_ACTION;
import org.grandviewtech.userinterface.misc.ActionBot;

public class CutButtonListener implements MouseListener
	{
		
		@Override
		public void mouseClicked(MouseEvent mouseEvent)
			{
				ClipBoard.setClipboardAction(CLIPBOARD_ACTION.CUT);
				ActionBot.cutColumn();
			}
			
		@Override
		public void mousePressed(MouseEvent mouseEvent)
			{
			}
			
		@Override
		public void mouseReleased(MouseEvent mouseEvent)
			{
			}
			
		@Override
		public void mouseEntered(MouseEvent e)
			{
			}
			
		@Override
		public void mouseExited(MouseEvent e)
			{
			}
			
	}
