package org.grandviewtech.userinterface.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.grandviewtech.constants.Borders;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.enums.CLIPBOARD_ACTION;
import org.grandviewtech.userinterface.misc.ActionBot;

public class CopyButtonListerner implements MouseListener, Borders
	{
		
		
		public CopyButtonListerner()
			{
			}
			
		@Override
		public void mouseClicked(MouseEvent e)
			{
				ClipBoard.setClipboardAction(CLIPBOARD_ACTION.COPY);
				ActionBot.copyColumn();
			}
			
		@Override
		public void mousePressed(MouseEvent e)
			{
				
			}
			
		@Override
		public void mouseReleased(MouseEvent e)
			{
				
			}
			
		@Override
		public void mouseEntered(MouseEvent e)
			{
				// TODO Auto-generated method stub
				
			}
			
		@Override
		public void mouseExited(MouseEvent e)
			{
				// TODO Auto-generated method stub
				
			}
			
	}
