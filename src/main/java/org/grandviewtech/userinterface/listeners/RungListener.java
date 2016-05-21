package org.grandviewtech.userinterface.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.grandviewtech.constants.Icons;
import org.grandviewtech.userinterface.screen.Rung;

public class RungListener implements MouseListener, KeyListener, Icons
	{
		final JPopupMenu	editMenu	= new JPopupMenu("Edit");
		
		private Rung		rung;
		
		public RungListener(Rung rung)
			{
				this.rung = rung;
			}
			
		@Override
		public void mouseClicked(MouseEvent mouseEvent)
			{
				if (SwingUtilities.isRightMouseButton(mouseEvent))
					{
						editMenu.removeAll();
						open();
						editMenu.show(rung, rung.getX() + 22, rung.getY());
					}
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
			
		@Override
		public void keyTyped(KeyEvent keyEvent)
			{
				
			}
			
		@Override
		public void keyPressed(KeyEvent keyEvent)
			{
				
			}
			
		@Override
		public void keyReleased(KeyEvent keyEvent)
			{
				
			}
			
		private void open()
			{
				newMenu(true);
				copyMenu(false);
				cutMenu(true);
				pasteMenu(true);
				deleteMenu(false);
				
			}
			
		private void newMenu(boolean addSeparator)
			{
				JMenuItem menuItem = new JMenuItem("New");
				menuItem.setIcon(NEW);
				int currentRungNumber = rung.getRowNumber();
				int newRungNumber = currentRungNumber + 1;
				menuItem.addActionListener(new NewRowListener(currentRungNumber, newRungNumber));
				editMenu.add(menuItem);
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
			
		private void copyMenu(boolean addSeparator)
			{
				JMenuItem menuItem = new JMenuItem("Copy");
				menuItem.setIcon(COPY);
				editMenu.add(menuItem);
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
			
		private void cutMenu(boolean addSeparator)
			{
				JMenuItem menuItem = new JMenuItem("Cut");
				menuItem.setIcon(CUT);
				editMenu.add(menuItem);
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
			
		private void pasteMenu(boolean addSeparator)
			{
				JMenuItem menuItem = new JMenuItem("Paste");
				menuItem.setIcon(PASTE);
				editMenu.add(menuItem);
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
			
		private void deleteMenu(boolean addSeparator)
			{
				JMenuItem menuItem = new JMenuItem("Delete");
				menuItem.setIcon(DELETE);
				editMenu.add(menuItem);
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
	}
