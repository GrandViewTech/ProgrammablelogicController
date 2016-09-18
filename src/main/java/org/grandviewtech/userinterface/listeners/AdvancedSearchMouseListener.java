package org.grandviewtech.userinterface.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

import org.grandviewtech.runner.Application;
import org.grandviewtech.userinterface.screen.AdvancedSearchScreen;

public class AdvancedSearchMouseListener implements MouseListener
	{
		
		@Override
		public void mouseClicked(MouseEvent e)
			{
				JFrame frame = new JFrame("Advanced Search");
				frame.setVisible(true);
				frame.setAlwaysOnTop(true);
				frame.add(new AdvancedSearchScreen());
				org.grandviewtech.entity.helper.Dimension dimension = Application.calculateCenterAlignment(frame.getPreferredSize());
				frame.setLocation(dimension.getX(), dimension.getY());
				frame.pack();
				frame.repaint();
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
