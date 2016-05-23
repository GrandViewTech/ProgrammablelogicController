package org.grandviewtech.userinterface.listeners;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

import org.grandviewtech.userinterface.screen.AdvancedSearchScreen;

public class AdvancedSearchMouseListener implements MouseListener
	{
		
		@Override
		public void mouseClicked(MouseEvent e)
			{
				JFrame frame = new JFrame("Advanced Search");
				frame.setVisible(true);
				frame.setAlwaysOnTop(true);
				Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
				int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
				int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
				frame.setLocation(x, y);
				frame.add(new AdvancedSearchScreen());
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
