package org.grandviewtech.userinterface.listeners;

import java.awt.Color;
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
				if (isSelected == false)
					{
						isSelected = true;
						source.setBackground(Color.GREEN);
						ActionBot.focusGained(source);
					}
				else
					{
						isSelected = false;
						source.setBackground(null);
						ActionBot.focusLost(source);
					}
					
			}
			
		@Override
		public void mousePressed(MouseEvent event)
			{
				source.setBackground(Color.GREEN);
			}
			
		@Override
		public void mouseReleased(MouseEvent event)
			{
				source.setBackground(null);
			}
			
		@Override
		public void mouseEntered(MouseEvent event)
			{
				//CustomToolBar.setPointerValue("Row : " + source.getRowNumber() + " Column : " + source.getColumnNumber());
				source.setToolTipText("Row : " + source.getRowNumber() + " Column : " + source.getColumnNumber());
				source.setBackground(Color.GRAY);
			}
			
		@Override
		public void mouseExited(MouseEvent event)
			{
				source.setBackground(null);
				//CustomToolBar.setPointerValue("");
			}
			
	}
