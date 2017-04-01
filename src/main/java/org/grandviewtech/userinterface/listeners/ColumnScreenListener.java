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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.grandviewtech.constants.CustomBorderList;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.service.runtime.user.useractivity.Activities;
import org.grandviewtech.service.runtime.user.useractivity.Activity;
import org.grandviewtech.service.system.PropertyReader;
import org.grandviewtech.service.system.SystemFileLocation;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.misc.ActionBot;
import org.grandviewtech.userinterface.misc.CustomToolBar;
import org.grandviewtech.userinterface.screen.ColumnConfigurationScreen;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ColumnScreenListener implements MouseListener, FocusListener, KeyListener
	{
		
		final static Screen	 SCREEN		= Screen.getInstance();
		
		private ColumnScreen source;
		
		private Activities	 activities	= Activities.getInstance();
		
		public ColumnScreenListener(ColumnScreen columnScreen)
			{
				source = columnScreen;
			}
			
		@Override
		public void mouseClicked(MouseEvent eventvent)
			{
				ColumnScreen active = Screen.getInstance().getActiveColumn();
				boolean selected = (active != null && (active.equals(source) == true));
				if (!selected)
					{
						activities.addActivity(new Activity("Row : " + source.getRowNumber() + " | Column : " + source.getColumnNumber() + " Gained Focus", Activity.Category.USER));
						Screen.getInstance().setActiveColumn(source);
						ColumnScreenGenerator.createColumnNeighbourHood(ClipBoard.SCREEN.getRow(source.getRowNumber()), source);
						String pointer = "Cell( " + source.getRowNumber() + "," + source.getColumnNumber() + " ) ";
						CustomToolBar.setPointerValue(pointer);
						//active.setToolTipText("Row : " + active.getRowNumber() + " |  Column : " + active.getColumnNumber());
						ClipBoard.setCurrentRowNumber(source.getRowNumber());
						ClipBoard.setCurrentColumnNumber(source.getColumnNumber());
						ActionBot.focusGained(source);
						
					}
				else
					{
						Screen.getInstance().setActiveColumn(null);
						activities.addActivity(new Activity("Row : " + source.getRowNumber() + " | Column : " + source.getColumnNumber() + " Lost Focus", Activity.Category.USER));
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
				this.source.setBackground(null);
			}
			
		@Override
		public void mouseEntered(MouseEvent event)
			{
			}
			
		@Override
		public void mouseExited(MouseEvent event)
			{
			}
			
		@Override
		public void focusGained(FocusEvent event)
			{
				/**
				 * if (source.isBlank() == false) { setting.setVisible(true); //
				 * source.add(setting); } ActionBot.focusGained(source);
				 **/
				ActionBot.focusGained(source);
				
			}
			
		@Override
		public void focusLost(FocusEvent event)
			{
				/**
				 * if (source.isBlank() == false) {
				 * 
				 * setting.setVisible(false); // source.remove(setting); } //
				 * ActionBot.focusLost(source);
				 * 
				 * source.remove(setting); }
				 **/
				ActionBot.focusLost(source);
				
			}
			
		@Override
		public void keyTyped(KeyEvent keyEvent)
			{
				boolean defaultWait = true;
				onFocus(keyEvent, defaultWait);
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(true);
					}
				else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					{
						ClipBoard.cutLabel.setBorder(null);
						ClipBoard.copyLabel.setBorder(null);
						ClipBoard.pasteLabel.setBorder(null);
						actionOnEscape();
					}
			}
			
		@Override
		public void keyPressed(KeyEvent keyEvent)
			{
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(true);
					}
				else if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_S)
					{
						String saveCounter = PropertyReader.getProperties("saveCounter");
						Integer counter = Integer.parseInt(saveCounter) + 1;
						SystemFileLocation.regenerateFolderStructure(counter);
						PropertyReader.setProperty(saveCounter, "" + saveCounter);
					}
				else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					{
						actionOnEscape();
					}
				else if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
					{
						if (source != null && source.getCoilType() != null && (source.getCoilType().equals(CoilType.LOAD) || source.getCoilType().equals(CoilType.ROUTINE) || source.getCoilType().equals(CoilType.OUTPUT)))
							{
								ColumnConfigurationScreen columnConfigurationScreen = new ColumnConfigurationScreen();
								columnConfigurationScreen.initiateInstance(source);
								columnConfigurationScreen.requestFocusInWindow();
							}
					}
			}
			
		@Override
		public void keyReleased(KeyEvent keyEvent)
			{
				boolean defaultWait = false;
				onFocus(keyEvent, defaultWait);
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(false);
					}
			}
			
		private void onFocus(KeyEvent keyEvent, boolean defaultWait)
			{
				if (defaultWait)
					{
						ActionBot.defaultHalt();
					}
				ColumnScreenGenerator.createColumnNeighbourHood(SCREEN.getRow(source.getRowNumber()), source);
				switch (keyEvent.getKeyCode())
					{
						case KeyEvent.VK_LEFT:
							{
								source.getPrevious(true);
								break;
							}
						case KeyEvent.VK_RIGHT:
							{
								source.getNext(true);
								break;
							}
						case KeyEvent.VK_UP:
							{
								source.getAbove(true);
								break;
							}
						case KeyEvent.VK_DOWN:
							{
								source.getBelow(true);
								break;
							}
					}
			}
			
		private void actionOnEscape()
			{
				ClipBoard.cutLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
				ClipBoard.copyLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
				ClipBoard.pasteLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
				ClipBoard.resetClipBoardSelection();
			}
			
	}
