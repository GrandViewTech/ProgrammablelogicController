package org.grandviewtech.userinterface.misc;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.grandviewtech.constants.Coils;
import org.grandviewtech.constants.Icons;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.userinterface.listeners.CopyButtonListerner;
import org.grandviewtech.userinterface.listeners.CutButtonListener;
import org.grandviewtech.userinterface.listeners.PasteButtonListener;
import org.grandviewtech.userinterface.ui.DragLabel;
import org.grandviewtech.userinterface.ui.ToolBarLabel;

public class CustomToolBar
	{
		final static JToolBar	toolBar			= new JToolBar("Ribbon");
		final static JToolBar	columnsBar		= new JToolBar("Columns");
		private static JLabel	pointerValue	= new JLabel("");
		
		public static JToolBar getToolBar()
			{
				toolBar.setPreferredSize(new Dimension(1200, 25));
				toolBar.setBackground(Color.white);
				toolBar.setRollover(true);
				toolBar.setFloatable(false);
				toolBar.setCursor(new Cursor(Cursor.HAND_CURSOR));
				Helper.setCursor(toolBar);
				setDefaultFunction();
				toolBar.addSeparator();
				setCoilsFunctions();
				toolBar.add(Box.createHorizontalGlue());
				toolBar.add(ClipBoard.getSelection());
				JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
				Dimension dimension = new Dimension(1, 20);
				separator.setMaximumSize(dimension);
				separator.setBackground(Color.BLACK);
				toolBar.add(separator);
				toolBar.add(new JLabel("Pointer : "));
				toolBar.add(pointerValue);
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
				cut.addMouseListener(new CutButtonListener());
				copy.addMouseListener(new CopyButtonListerner());
				paste.addMouseListener(new PasteButtonListener());
				toolBar.add(cut);
				//toolBar.addSeparator();
				toolBar.add(copy);
				//toolBar.addSeparator();
				toolBar.add(paste);
				ClipBoard.cutLabel = cut;
				ClipBoard.copyLabel = copy;
				ClipBoard.pasteLabel = paste;
			}
			
		public static void setCoilsFunctions()
			{
				DragLabel load = new DragLabel(Coils.LOAD);
				DragLabel line = new DragLabel(Coils.LINE);
				DragLabel output = new DragLabel(Coils.OUTPUT);
				DragLabel jump = new DragLabel(Coils.JUMP);
				toolBar.add(load);
				toolBar.add(line);
				toolBar.add(jump);
				toolBar.add(output);
			}
			
		public static void setPointerValue(String pointerValue)
			{
				CustomToolBar.pointerValue.setText(pointerValue);
			}
			
	}
