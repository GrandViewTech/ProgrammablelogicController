package org.grandviewtech.userinterface.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.grandviewtech.userinterface.screen.AdvancedSearchScreen;

public class AdvancedSearchMouseListener implements MouseListener
	{
		private static AdvancedSearchScreen advancedSearchScreen;
		
		@Override
		public void mouseClicked(MouseEvent e)
			{
				if (advancedSearchScreen == null)
					{
						advancedSearchScreen = new AdvancedSearchScreen();
						advancedSearchScreen.invoke();
					}
				else
					{
						advancedSearchScreen.invoke();
						advancedSearchScreen.reset();
						advancedSearchScreen.requestFocus();
					}
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
				
			}
			
		@Override
		public void mouseExited(MouseEvent e)
			{
				
			}
			
	}
