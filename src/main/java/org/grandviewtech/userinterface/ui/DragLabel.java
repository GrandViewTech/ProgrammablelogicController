package org.grandviewtech.userinterface.ui;

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

import java.awt.Cursor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.grandviewtech.constants.Borders;
import org.grandviewtech.service.system.Printer;

public class DragLabel extends JLabel implements DragGestureListener, DragSourceListener, Borders
	{
		private static final long	serialVersionUID	= 4128980357528030474L;
		private DragSource			dragSource;
		
		public DragLabel(String text)
			{
				setText(text);
				setBorder(BorderFactory.createCompoundBorder(BORDER, PADDING_BORDER));
				dragSource = new DragSource();
				dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
			
		@Override
		public void dragEnter(DragSourceDragEvent dragSourceDragEvent)
			{
				Printer.print("DragLabel dragEnter");
			}
			
		@Override
		public void dragOver(DragSourceDragEvent dragSourceDragEvent)
			{
				Printer.print("DragLabel dragOver");
			}
			
		@Override
		public void dropActionChanged(DragSourceDragEvent dragSourceDragEvent)
			{
				Printer.print("DragLabel dropActionChanged");
			}
			
		@Override
		public void dragExit(DragSourceEvent dse)
			{
				Printer.print("DragLabel dragExit");
			}
			
		@Override
		public void dragDropEnd(DragSourceDropEvent dragSourceDragEvent)
			{
				Printer.print("DragLabel dragDropEnd");
			}
			
		@Override
		public void dragGestureRecognized(DragGestureEvent dragSourceDragEvent)
			{
				Printer.print("DragLabel dragGestureRecognized");
				Transferable transferable = new StringSelection(getText());
				dragSource.startDrag(dragSourceDragEvent, DragSource.DefaultCopyDrop, transferable, this);
			}
			
	}
