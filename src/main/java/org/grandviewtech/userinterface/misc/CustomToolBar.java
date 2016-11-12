package org.grandviewtech.userinterface.misc;

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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.grandviewtech.constants.CustomIcon;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.service.runtime.user.useractivity.Activities;
import org.grandviewtech.service.runtime.user.useractivity.Activity;
import org.grandviewtech.service.validation.RowValidation;
import org.grandviewtech.userinterface.listeners.CopyButtonListerner;
import org.grandviewtech.userinterface.listeners.CutButtonListener;
import org.grandviewtech.userinterface.listeners.PasteButtonListener;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.ui.DragLabel;
import org.grandviewtech.userinterface.ui.ToolBarLabel;

public class CustomToolBar
	{
		private static Activities	activities			= Activities.getInstance();
		final static Screen			screen				= Screen.getInstance();
		final static JToolBar		toolBar				= new JToolBar("Ribbon");
		final static JToolBar		columnsBar			= new JToolBar("Columns");
		private static JLabel		pointerValue		= new JLabel("");
		private static JLabel		rungComment			= new JLabel("Comment : ");
		private static JLabel		rungCommentValue	= new JLabel("");
		private static JLabel		pointerLabel		= new JLabel("Pointer : ");
		
		public static JToolBar getToolBar()
			{
				// toolBar.setLayout(null);
				toolBar.setPreferredSize(new Dimension(1200, 25));
				toolBar.setBackground(Color.white);
				toolBar.setRollover(true);
				toolBar.setFloatable(false);
				// toolBar.setCursor(new Cursor(Cursor.HAND_CURSOR));
				Helper.setCursor(toolBar);
				setDefaultFunction();
				toolBar.addSeparator();
				setCoilTypeFunctions();
				toolBar.add(Box.createHorizontalGlue());
				// toolBar.add(separator);
				toolBar.add(rungComment);
				toolBar.add(rungCommentValue);
				JSeparator separator2 = new JSeparator(SwingConstants.VERTICAL);
				Dimension dimension = new Dimension(1, 20);
				separator2.setMaximumSize(dimension);
				separator2.setBackground(Color.BLACK);
				toolBar.add(separator2);
				JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
				separator.setMaximumSize(dimension);
				separator.setBackground(Color.BLACK);
				toolBar.add(ClipBoard.getSelection());
				toolBar.add(separator);
				toolBar.add(pointerLabel);
				toolBar.add(pointerValue);
				return toolBar;
			}
			
		public static JToolBar getColoumnMarker()
			{
				columnsBar.setPreferredSize(new Dimension(1200, 20));
				columnsBar.setBackground(Color.white);
				columnsBar.setRollover(true);
				// columnsBar.setCursor(new Cursor(Cursor.HAND_CURSOR));
				Helper.setCursor(toolBar);
				return columnsBar;
			}
			
		private static void setDefaultFunction()
			{
				
				ToolBarLabel cut = new ToolBarLabel(CustomIcon.CUT);
				// cut.setBounds(5, 5, 25, 25);
				cut.setToolTipText("cut");
				ToolBarLabel copy = new ToolBarLabel(CustomIcon.COPY);
				// copy.setBounds(30, 5, 25, 25);
				copy.setToolTipText("copy");
				ToolBarLabel paste = new ToolBarLabel(CustomIcon.PASTE);
				paste.setToolTipText("paste");
				// paste.setBounds(55, 5, 25, 25);
				cut.addMouseListener(new CutButtonListener());
				copy.addMouseListener(new CopyButtonListerner());
				paste.addMouseListener(new PasteButtonListener());
				toolBar.add(cut);
				// toolBar.addSeparator();
				toolBar.add(copy);
				// toolBar.addSeparator();
				toolBar.add(paste);
				JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
				separator.setBounds(3, toolBar.getPreferredSize().height / 2, 600, 10);
				// toolBar.add(separator);
				ClipBoard.cutLabel = cut;
				ClipBoard.copyLabel = copy;
				ClipBoard.pasteLabel = paste;
			}
			
		public static void setCoilTypeFunctions()
			{
				DragLabel load = new DragLabel(CustomIcon.COIL_LOAD, CoilType.LOAD.getCoilType());
				DragLabel line = new DragLabel(CustomIcon.COIL_LINE, CoilType.LINE.getCoilType());
				//
				DragLabel parellel = new DragLabel(CustomIcon.COIL_PARALLEL, CoilType.PARALLEL.getCoilType());
				DragLabel leftLink = new DragLabel(CustomIcon.COIL_LEFT_LINK, CoilType.LEFT_LINK.getCoilType());
				DragLabel righLink = new DragLabel(CustomIcon.COIL_RIGHT_LINK, CoilType.RIGHT_LINK.getCoilType());
				//
				//DragLabel delete = new DragLabel(CustomIcon.COIL_DELETE, CoilType.DELETE.getCoilType());
				DragLabel end = new DragLabel(CustomIcon.COIL_END, CoilType.END.getCoilType());
				// DragLabel compile = new
				// DragLabel(CoilType.COMPLIE.getCoil());
				//
				DragLabel output = new DragLabel(CustomIcon.COIL_OUTPUT, CoilType.OUTPUT.getCoilType());
				DragLabel jump = new DragLabel(CustomIcon.COIL_JUMP, CoilType.JUMP.getCoilType());
				
				DragLabel routine = new DragLabel(CustomIcon.COIL_ROUTINE, CoilType.ROUTINE.getCoilType());
				DragLabel label = new DragLabel(CustomIcon.COIL_LABEL, CoilType.LABEL.getCoilType());
				//
				JButton delete = new JButton(CustomIcon.COIL_DELETE);
				delete.setOpaque(true);
				Border paddingBorder = BorderFactory.createEmptyBorder(2, 5, 5, 2);
				Border border = BorderFactory.createLineBorder(Color.white);
				delete.setBorder(BorderFactory.createCompoundBorder(border, paddingBorder));
				delete.addActionListener(event ->
					{
						ColumnScreen activeColumn = screen.getActiveColumn();
						String message = "";
						if (activeColumn != null)
							{
								activeColumn.reset();
								activities.addActivity(new Activity("Cell( " + activeColumn.getRowNumber() + " , " + activeColumn.getColumnNumber() + " ) " + activeColumn.getRowNumber() + " is deleted", Activity.Category.USER));
								message = "Cell( " + activeColumn.getRowNumber() + " , " + activeColumn.getColumnNumber() + " ) Successfully Deleted.";
								RowValidation.validateNeighBourHood(activeColumn);
							}
						else
							{
								message = "No Cell is Selected To Delete.";
							}
						JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
						JDialog dialog = optionPane.createDialog(null, "Delete Cell");
						dialog.setModal(false);
						dialog.setVisible(true);
						// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html#stayup
						Timer timer = new Timer(600, timerEvent ->
					        {
						        dialog.setVisible(false);
						        dialog.dispose();
					        });
						timer.start();
					});
				toolBar.add(load);
				toolBar.add(line);
				// toolBar.add(compile);
				
				toolBar.add(output);
				toolBar.add(parellel);
				toolBar.add(leftLink);
				toolBar.add(righLink);
				toolBar.add(routine);
				toolBar.add(label);
				toolBar.add(jump);
				toolBar.add(end);
				toolBar.add(delete);
				toolBar.repaint();
			}
			
		public static void setPointerValue(String pointerValue)
			{
				CustomToolBar.pointerValue.setText(pointerValue);
			}
			
		public static JLabel getRungComment()
			{
				return rungComment;
			}
			
		public static void setRungComment(int rungNumber, String rungComment)
			{
				if (rungComment != null && rungComment.trim().length() > 0)
					{
						String tempText = rungComment;
						if (rungComment.length() > 200)
							{
								tempText = rungComment.substring(0, 199);
							}
						CustomToolBar.rungCommentValue.setText(tempText);
						CustomToolBar.rungCommentValue.setForeground(Color.RED);
						CustomToolBar.rungCommentValue.setToolTipText(rungComment);
					}
				else
					{
						CustomToolBar.rungCommentValue.setText("");
					}
			}
			
	}
