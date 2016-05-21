package org.grandviewtech.userinterface.listeners;

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
