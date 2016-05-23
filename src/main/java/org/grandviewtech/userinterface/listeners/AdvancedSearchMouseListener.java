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
