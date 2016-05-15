package org.grandviewtech.userinterface.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.grandviewtech.userinterface.misc.ActionBot;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ColumnScreenMouseClickListener implements MouseListener
	{
		private ColumnScreen	source;
		private boolean			isSelected	= false;
		
		public ColumnScreenMouseClickListener(ColumnScreen columnScreen)
			{
				source = columnScreen;
			}
			
		@Override
		public void mouseClicked(MouseEvent eventvent)
			{
				//source.requestFocus();
				if (isSelected == false)
					{
						isSelected = true;
						ActionBot.focusGained(source);
					}
				else
					{
						isSelected = false;
						ActionBot.focusLost(source);
					}

			}
			
		@Override
		public void mousePressed(MouseEvent event)
			{
			}
			
		@Override
		public void mouseReleased(MouseEvent event)
			{
			}
			
		@Override
		public void mouseEntered(MouseEvent event)
			{
			}
			
		@Override
		public void mouseExited(MouseEvent event)
			{
				
			}
			
	}
