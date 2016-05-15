package org.grandviewtech.userinterface.misc;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.JToolBar;

import org.grandviewtech.constants.Coils;
import org.grandviewtech.constants.Icons;
import org.grandviewtech.userinterface.ui.DragLabel;
import org.grandviewtech.userinterface.ui.ToolBarLabel;

public class CustomToolBar
	{
		final static JToolBar	toolBar		= new JToolBar("Ribbon");
		final static JToolBar	columnsBar	= new JToolBar("Columns");
		
		public static JToolBar getToolBar()
			{
				toolBar.setPreferredSize(new Dimension(1200, 25));
				toolBar.setBackground(Color.white);
				toolBar.setRollover(true);
				toolBar.setCursor(new Cursor(Cursor.HAND_CURSOR));
				Helper.setCursor(toolBar);
				setDefaultFunction();
				toolBar.addSeparator();
				setCoilsFunctions();
				return toolBar;
			}
			
		public static JToolBar getColoumnMarker()
			{
				columnsBar.setPreferredSize(new Dimension(1200, 20));
				columnsBar.setBackground(Color.white);
				columnsBar.setRollover(true);
				columnsBar.setCursor(new Cursor(Cursor.HAND_CURSOR));
				Helper.setCursor(toolBar);
				return columnsBar;
			}
			
		private static void setDefaultFunction()
			{
				ToolBarLabel cut = new ToolBarLabel(Icons.CUT);
				cut.setToolTipText("cut");
				ToolBarLabel copy = new ToolBarLabel(Icons.COPY);
				copy.setToolTipText("copy");
				ToolBarLabel paste = new ToolBarLabel(Icons.PASTE);
				paste.setToolTipText("paste");
				toolBar.add(cut);
				//toolBar.addSeparator();
				toolBar.add(copy);
				//toolBar.addSeparator();
				toolBar.add(paste);
			}
			
		public static void setCoilsFunctions()
			{
				DragLabel load = new DragLabel(Coils.LOAD);
				DragLabel line = new DragLabel(Coils.LINE);
				DragLabel output = new DragLabel(Coils.OUTPUT);
				toolBar.add(load);
				toolBar.add(line);
				toolBar.add(output);
			}
	}
