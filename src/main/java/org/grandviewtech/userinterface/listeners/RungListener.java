package org.grandviewtech.userinterface.listeners;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.grandviewtech.constants.Icons;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.userinterface.screen.Rung;

public class RungListener implements MouseListener, KeyListener, Icons
	{
		
		final JPopupMenu	editMenu	= new JPopupMenu("Edit");
		
		private Rung		rung;
		
		private int			currentRowIndex;
		
		public RungListener(Rung rung)
			{
				this.rung = rung;
				this.currentRowIndex = rung.getRowNumber();
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
				ClipBoard.addTempRung(rung);
				if (ClipBoard.isControlKeyActive() == false)
					{
						rung.setBackground(Color.GREEN);
					}
			}
			
		@Override
		public void mousePressed(MouseEvent mouseEvent)
			{
				rung.setBackground(Color.GREEN);
			}
			
		@Override
		public void mouseReleased(MouseEvent mouseEvent)
			{
				if (ClipBoard.isControlKeyActive() == false)
					{
						rung.setBackground(null);
					}
			}
			
		@Override
		public void mouseEntered(MouseEvent mouseEvent)
			{
				if (ClipBoard.isControlKeyActive() == false)
					{
						rung.setBackground(Color.LIGHT_GRAY);
					}
			}
			
		@Override
		public void mouseExited(MouseEvent mouseEvent)
			{
				
				if (ClipBoard.isControlKeyActive() == false)
					{
						rung.setBackground(null);
					}
			}
			
		@Override
		public void keyTyped(KeyEvent keyEvent)
			{
				
			}
			
		@Override
		public void keyPressed(KeyEvent keyEvent)
			{
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(true);
					}
			}
			
		@Override
		public void keyReleased(KeyEvent keyEvent)
			{
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(false);
					}
					
			}
			
		private void open()
			{
				boolean isCopyDataAvaliable = (ClipBoard.getCopiedRung().size() > 0) ? true : false;
				newMenu(true);
				copyMenu(false);
				cutMenu(true);
				if (isCopyDataAvaliable == true)
					{
						pasteMenu(true);
					}
				deleteMenu(false);
				
			}
			
		private void newMenu(boolean addSeparator)
			{
				JMenuItem menuItem = new JMenuItem("New");
				menuItem.setIcon(NEW);
				menuItem.addActionListener(new RungActionListerner(RungActionListerner.RungAction.NEW, currentRowIndex));
				menuItem.setToolTipText("Add New Rung Below Rung Number " + currentRowIndex);
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
				menuItem.addActionListener(new RungActionListerner(RungActionListerner.RungAction.COPY, currentRowIndex));
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
				menuItem.addActionListener(new RungActionListerner(RungActionListerner.RungAction.CUT, currentRowIndex));
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
				menuItem.setToolTipText("Paste Below Rung Number " + currentRowIndex);
				menuItem.addActionListener(new RungActionListerner(RungActionListerner.RungAction.PASTE, currentRowIndex));
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
			
		private void deleteMenu(boolean addSeparator)
			{
				JMenuItem menuItem = new JMenuItem("Delete");
				menuItem.setIcon(DELETE);
				menuItem.addActionListener(new RungActionListerner(RungActionListerner.RungAction.DELETE, currentRowIndex));
				menuItem.setToolTipText("Delete Rung Number " + currentRowIndex);
				editMenu.add(menuItem);
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
	}
