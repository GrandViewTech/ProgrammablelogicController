package org.grandviewtech.userinterface.ui;

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

import javax.swing.JLabel;

import org.grandviewtech.service.system.Printer;

public class DragLabel extends JLabel implements DragGestureListener, DragSourceListener
	{
		private static final long	serialVersionUID	= 4128980357528030474L;
		private DragSource			dragSource;
		
		public DragLabel(String text)
			{
				setText(text);
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
