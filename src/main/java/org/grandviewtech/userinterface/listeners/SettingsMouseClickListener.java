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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.userinterface.screen.ColumnConfigurationScreen;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class SettingsMouseClickListener implements MouseListener
	{
		final static Screen		SCREEN	= Screen.getInstance();
		private ColumnScreen	source;
		
		public SettingsMouseClickListener(ColumnScreen columnScreen)
			{
				this.source = columnScreen;
			}
			
		@Override
		public void mouseClicked(MouseEvent mouseEvent)
			{
				// if (source != null && source.getCoilType() != null &&
				// (source.getCoilType().equals(CoilType.LOAD) ||
				// source.getCoilType().equals(CoilType.PARALLEL) ||
				// source.getCoilType().equals(CoilType.LEFT_LINK) ||
				// source.getCoilType().equals(CoilType.RIGHT_LINK) ||
				// source.getCoilType().equals(CoilType.OUTPUT)))
				if (source != null && source.getCoilType() != null && !(source.getCoilType().equals(CoilType.LINE) || source.getCoilType().equals(CoilType.END)))
					{
						ColumnConfigurationScreen columnConfigurationScreen = new ColumnConfigurationScreen();
						columnConfigurationScreen.initiateInstance(source);
						columnConfigurationScreen.requestFocusInWindow();
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
			
		/*
		 * private void addPopUpMenu() { edit(); delete();
		 * popUpMenu.show(setting, setting.getX() - 10, setting.getY() + 5); }
		 * 
		 * private void edit() { JMenuItem menuItem = new JMenuItem("Edit");
		 * menuItem.setIcon(EDIT); menuItem.addActionListener(event -> {
		 * ColumnConfigurationScreen columnConfigurationScreen =
		 * ColumnConfigurationScreen.getInstance();
		 * columnConfigurationScreen.requestFocusInWindow();
		 * columnConfigurationScreen.initiateInstance(columnScreen); });
		 * popUpMenu.add(menuItem); }
		 * 
		 * private void delete() { JMenuItem menuItem = new JMenuItem("delete");
		 * menuItem.setIcon(DELETE); popUpMenu.add(menuItem); }
		 */
	}
