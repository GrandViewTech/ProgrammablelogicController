package org.grandviewtech.userinterface.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.grandviewtech.constants.Borders;
import org.grandviewtech.userinterface.misc.ActionBot;

public class PasteButtonListener implements MouseListener, Borders
	{
		public PasteButtonListener()
			{
			}
			
		@Override
		public void mouseClicked(MouseEvent mouseEvent)
			{
				ActionBot.pasteColumn();
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
		public void mouseEntered(MouseEvent mouseEvent)
			{
			}
			
		@Override
		public void mouseExited(MouseEvent mouseEvent)
			{
			}
			
	}
