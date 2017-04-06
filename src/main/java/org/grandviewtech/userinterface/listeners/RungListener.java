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

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.grandviewtech.constants.CustomIcon;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.userinterface.misc.CustomToolBar;
import org.grandviewtech.userinterface.screen.Rung;

public class RungListener implements MouseListener, KeyListener
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
				CustomToolBar.setRungComment(rung.getRowNumber(), rung.getComment());
				if (SwingUtilities.isRightMouseButton(mouseEvent))
					{
						editMenu.removeAll();
						open();
						editMenu.show(rung, rung.getX() + 22, rung.getY());
						//
					}
					
				if (ClipBoard.isControlKeyActive() == false)
					{
						rung.setBackground(Color.GREEN);
					}
					
				ClipBoard.addTempRung(rung);
				
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
				deleteMenu(true);
				addComment(false);
			}
			
		private void newMenu(boolean addSeparator)
			{
				int currentRowIndex = rung.getRowNumber();
				JMenuItem menuItem = new JMenuItem("New");
				menuItem.setIcon(CustomIcon.NEW);
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
				int currentRowIndex = rung.getRowNumber();
				JMenuItem menuItem = new JMenuItem("Copy");
				menuItem.setIcon(CustomIcon.COPY);
				menuItem.addActionListener(new RungActionListerner(RungActionListerner.RungAction.COPY, currentRowIndex));
				editMenu.add(menuItem);
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
			
		private void cutMenu(boolean addSeparator)
			{
				int currentRowIndex = rung.getRowNumber();
				JMenuItem menuItem = new JMenuItem("Cut");
				menuItem.setIcon(CustomIcon.CUT);
				editMenu.add(menuItem);
				menuItem.addActionListener(new RungActionListerner(RungActionListerner.RungAction.CUT, currentRowIndex));
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
			
		private void pasteMenu(boolean addSeparator)
			{
				int currentRowIndex = rung.getRowNumber();
				JMenuItem menuItem = new JMenuItem("Paste");
				menuItem.setIcon(CustomIcon.PASTE);
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
				int currentRowIndex = rung.getRowNumber();
				JMenuItem menuItem = new JMenuItem("Delete");
				menuItem.setIcon(CustomIcon.DELETE);
				menuItem.addActionListener(new RungActionListerner(RungActionListerner.RungAction.DELETE, currentRowIndex));
				menuItem.setToolTipText("Delete Rung Number " + currentRowIndex);
				editMenu.add(menuItem);
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
			
		private void addComment(boolean addSeparator)
			{
				int currentRowIndex = rung.getRowNumber();
				JMenuItem menuItem = new JMenuItem("Comment");
				menuItem.setIcon(CustomIcon.COMMENT);
				menuItem.addActionListener(new RungActionListerner(RungActionListerner.RungAction.COMMENT, currentRowIndex));
				menuItem.setToolTipText("Comment on Rung Number " + currentRowIndex);
				editMenu.add(menuItem);
				if (addSeparator)
					{
						editMenu.addSeparator();
					}
			}
	}
