package org.grandviewtech.userinterface.listeners;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ColumnScreenDropEventListener implements DropTargetListener
	{
		private ColumnScreen	source;
		
	//	private DropTarget		dropTarget;
		
		public ColumnScreenDropEventListener(ColumnScreen source)
			{
				super();
				this.source = source;
				new DropTarget(source, source);
			}
			
		@Override
		public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent)
			{
			}
			
		@Override
		public void dragExit(DropTargetEvent dropTargetEvent)
			{
			}
			
		@Override
		public void drop(DropTargetDropEvent dropTargetDragEvent)
			{
				try
					{
						Transferable transferable = dropTargetDragEvent.getTransferable();
						if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
							{
								dropTargetDragEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
								String dragContents = (String) transferable.getTransferData(DataFlavor.stringFlavor);
								dropTargetDragEvent.getDropTargetContext().dropComplete(true);
								source.getCoilName().setText(source.getCoilName().getText() + " " + dragContents);
							}
						else
							{
								dropTargetDragEvent.rejectDrop();
							}
							
					}
				catch (IOException exception)
					{
						dropTargetDragEvent.rejectDrop();
					}
				catch (UnsupportedFlavorException exception)
					{
						dropTargetDragEvent.rejectDrop();
					}
					
			}
			
		@Override
		public void dragEnter(DropTargetDragEvent dtde)
			{
			}
			
		@Override
		public void dragOver(DropTargetDragEvent dtde)
			{
			}
			
	}
