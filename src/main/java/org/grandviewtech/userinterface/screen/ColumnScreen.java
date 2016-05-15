package org.grandviewtech.userinterface.screen;

import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import org.grandviewtech.constants.Coils;
import org.grandviewtech.constants.PreferredDimension;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.service.validation.ValidateDragOption;
import org.grandviewtech.userinterface.coils.PaintCoilsOnScreen;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.helper.CustomBorder;
import org.grandviewtech.userinterface.listeners.ColumnScreenFocusListener;
import org.grandviewtech.userinterface.listeners.ColumnScreenKeyPressListener;
import org.grandviewtech.userinterface.listeners.ColumnScreenMouseClickListener;

public class ColumnScreen extends JPanel implements PreferredDimension, DropTargetListener
	{
		private static final long	serialVersionUID	= -4357735797077739462L;
		private ColumnScreen		previous;
		private ColumnScreen		next;
		private ColumnScreen		above;
		private ColumnScreen		below;
		private int					rowNumber;
		private int					columnNumber;
		private boolean				paintDefault		= true;
		private JLabel				coilName			= new JLabel();
		private String				coil;
		final static Screen			SCREEN				= Screen.getInstance();
		
		public ColumnScreen()
			{
				init();
			}
			
		private void init()
			{
				add(coilName);
				setPreferredSize(CELL_SIZE);
				setBorder(new CustomBorder());
				setTransferHandler(new TransferHandler("icon"));
				new DropTarget(this, this);
				addMouseListener(new ColumnScreenMouseClickListener(this));
				addKeyListener(new ColumnScreenKeyPressListener(this));
				addFocusListener(new ColumnScreenFocusListener(this));
				
			}
			
		public JLabel getCoilName()
			{
				return coilName;
			}
			
		public void setCoilName(JLabel coilName)
			{
				this.coilName = coilName;
			}
			
		public int getColumnNumber()
			{
				return columnNumber;
			}
			
		public void setColumnNumber(int columnNumber)
			{
				this.columnNumber = columnNumber;
			}
			
		@Override
		protected void paintComponent(java.awt.Graphics graphics)
			{
				super.paintComponent(graphics);
				PaintCoilsOnScreen.paint(graphics, coil, paintDefault);
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
								String dragContents = (String) transferable.getTransferData(DataFlavor.stringFlavor);
								ColumnScreenGenerator.createColumnNeighbourHood(SCREEN.getRow(getRowNumber()), this);
								boolean validateDragOption = ValidateDragOption.validateDragOption(this, dragContents);
								if (validateDragOption == true)
									{
										dropTargetDragEvent.getDropTargetContext().getComponent().setCursor(DragSource.DefaultCopyDrop);
										dropTargetDragEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
										selectTheRigthCoilUsingDragOption(dragContents);
										Graphics graphics = getGraphics();
										paintComponent(graphics);
										dropTargetDragEvent.getDropTargetContext().dropComplete(true);
									}
								else
									{
										dropTargetDragEvent.getDropTargetContext().getComponent().setCursor(DragSource.DefaultCopyNoDrop);
										dropTargetDragEvent.rejectDrop();
										//setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
										
										dropTargetDragEvent.getDropTargetContext().dropComplete(false);
									}
									
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
			
		public ColumnScreen getPrevious(boolean isFocusRequired)
			{
				if (previous != null)
					{
						if (isFocusRequired == true)
							{
								previous.requestFocus();
							}
					}
				return previous;
			}
			
		public void setPrevious(ColumnScreen previous)
			{
				this.previous = previous;
			}
			
		public ColumnScreen getNext(boolean isFocusRequired)
			{
				if (next != null)
					{
						if (isFocusRequired == true)
							{
								next.requestFocus();
							}
					}
				return next;
			}
			
		public void setNext(ColumnScreen next)
			{
				this.next = next;
			}
			
		public ColumnScreen getAbove(boolean isFocusRequired)
			{
				if (above != null)
					{
						if (isFocusRequired == true)
							{
								above.requestFocus();
							}
					}
				return above;
			}
			
		public void setAbove(ColumnScreen above)
			{
				this.above = above;
			}
			
		public ColumnScreen getBelow(boolean isFocusRequired)
			{
				if (below != null)
					{
						if (isFocusRequired == true)
							{
								below.requestFocus();
							}
					}
				return below;
			}
			
		public void setBelow(ColumnScreen below)
			{
				this.below = below;
			}
			
		public int getRowNumber()
			{
				return rowNumber;
			}
			
		public void setRowNumber(int rowNumber)
			{
				this.rowNumber = rowNumber;
			}
			
		public boolean hasNext()
			{
				return (next == null) ? false : true;
			}
			
		public String getCoil()
			{
				return coil;
			}
			
		public void setCoil(String coil)
			{
				this.paintDefault = false;
				this.coil = coil;
			}
			
		public boolean isPaintDefault()
			{
				return paintDefault;
			}
			
		public void setPaintDefault(boolean paintDefault)
			{
				this.paintDefault = paintDefault;
			}
			
		
		private void selectTheRigthCoilUsingDragOption(String dragContents)
			{
				switch (dragContents)
					{
						case Coils.LINE:
							{
								setCoil(Coils.LINE);
								break;
							}
						case Coils.OUTPUT:
							{
								setCoil(Coils.OUTPUT);
								break;
							}
						case Coils.LOAD:
							{
								setCoil(Coils.LOAD);
								break;
							}
					}
			}
	}
