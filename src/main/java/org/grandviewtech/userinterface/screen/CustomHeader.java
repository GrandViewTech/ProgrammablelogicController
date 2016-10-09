package org.grandviewtech.userinterface.screen;

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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import org.grandviewtech.userinterface.listeners.AdvancedSearchMouseListener;
import org.grandviewtech.userinterface.misc.CustomToolBar;
import org.grandviewtech.userinterface.misc.Helper;

public class CustomHeader
	{
		
		public static JMenuBar getJMenuBar()
			{
				JMenuBar menuBar = new JMenuBar();
				menuBar.add(getFileMenu());
				Helper.setCursor(menuBar);
				JButton advancedSearch = new JButton("Advanced Search");
				advancedSearch.addMouseListener(new AdvancedSearchMouseListener());
				advancedSearch.setToolTipText("Advanced Search");
				menuBar.add(advancedSearch);
				// PARTITION
				menuBar.add(Box.createHorizontalGlue());
				menuBar.add(Search.getSearchLabel());
				menuBar.add(Search.getSearchTextField());
				menuBar.add(Search.getGo());
				menuBar.add(Search.getPrevious());
				JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
				Dimension dimension = new Dimension(1, 20);
				separator.setMaximumSize(dimension);
				separator.setBackground(Color.BLACK);
				menuBar.add(separator);
				menuBar.add(Search.getNext());
				menuBar.add(new JLabel("  "));
				return menuBar;
			}
			
		public static JMenu getFileMenu()
			{
				JMenu fileMenu = new JMenu("File");
				JMenuItem _new = new JMenuItem("New");
				JMenuItem _newTab = new JMenuItem("New Tab");
				JMenuItem save = new JMenuItem("Save");
				JMenuItem saveAs = new JMenuItem("Save As");
				JMenuItem open = new JMenuItem("Open");
				JMenuItem close = new JMenuItem("Close");
				JMenuItem exit = new JMenuItem("Exit");
				JMenuItem preference = new JMenuItem("Preference");
				exit.addActionListener(event ->
					{
						System.exit(0);
					});
				fileMenu.add(_new);
				fileMenu.add(_newTab);
				fileMenu.addSeparator();
				fileMenu.add(save);
				fileMenu.add(saveAs);
				fileMenu.addSeparator();
				fileMenu.add(open);
				fileMenu.addSeparator();
				fileMenu.add(close);
				fileMenu.add(exit);
				fileMenu.addSeparator();
				fileMenu.add(preference);
				return fileMenu;
			}
			
		public static JToolBar getJToolBar()
			{
				return CustomToolBar.getToolBar();
			}
			
		public static JToolBar getColoumnMarker()
			{
				return CustomToolBar.getColoumnMarker();
			}
			
		public static class DragMouseAdapter extends MouseAdapter
			{
				@Override
				public void mousePressed(MouseEvent event)
					{
						JComponent component = (JComponent) event.getSource();
						TransferHandler handler = component.getTransferHandler();
						handler.exportAsDrag(component, event, TransferHandler.COPY);
					}
			}
			
	}
