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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.grandviewtech.constants.CustomDimension;
import org.grandviewtech.constants.CustomIcon;
import org.grandviewtech.entity.bo.PlcFile;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.helper.RowGenerator;
import org.grandviewtech.userinterface.listeners.DefaultKeyListener;
import org.grandviewtech.userinterface.ui.Ruler;

public class BackGroundLayer extends JFrame
	{
		private static final long	serialVersionUID	= 1098998552962951470L;
		
		private Screen				screen				= Screen.getInstance();
		
		private Container			container;
		
		private PlcFile				plcFile				= PlcFile.getInstance();
		
		public BackGroundLayer()
			{
				addKeyListener(new DefaultKeyListener());
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				setVisible(true);
				setPositioning();
				container = getContentPane();
				container.add(addRulerJScrolBar());
				postConstruct();
			}
			
		private void setPositioning()
			{
				Point point = new Point((Toolkit.getDefaultToolkit().getScreenSize().width - getSize().width) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - getSize().height) / 2);
				setLocation(point);
			}
			
		private Ruler addRulerJScrolBar()
			{
				Ruler jScrollPane = new Ruler(screen.getSheet(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				jScrollPane.addKeyListener(new DefaultKeyListener());
				jScrollPane.initializeScrollPane();
				JScrollBar verticalScrollBar = jScrollPane.getVerticalScrollBar();
				verticalScrollBar.setUnitIncrement(15);
				InputMap im = verticalScrollBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				im.put(KeyStroke.getKeyStroke("DOWN"), "positiveUnitIncrement");
				im.put(KeyStroke.getKeyStroke("UP"), "negativeUnitIncrement");
				return jScrollPane;
			}
			
		private void postConstruct()
			{
				setHeader();
				setMinMaxDimension();
				plcFile.setFileName("temp_" + System.nanoTime());
				setIconImage(CustomIcon.ULTRA_LOGO.getImage());
			}
			
		private void setHeader()
			{
				setJMenuBar(CustomHeader.getJMenuBar());
				container.add(CustomHeader.getJToolBar(), BorderLayout.NORTH);
				container.add(CustomHeader.getColoumnMarker(), BorderLayout.SOUTH);
				
			}
			
		public void setMinMaxDimension()
			{
				setExtendedState(JFrame.MAXIMIZED_BOTH);
				setMinimumSize(CustomDimension.MINIMUM_FRAME_SIZE);
			}
			
		public void init()
			{
				RowGenerator.regenerateRow();
			}
	}
