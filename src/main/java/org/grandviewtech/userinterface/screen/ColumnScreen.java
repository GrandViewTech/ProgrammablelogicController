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
import org.grandviewtech.constants.Icons;
import org.grandviewtech.constants.PreferredDimension;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.service.validation.ValidateDragOption;
import org.grandviewtech.userinterface.coils.PaintCoilsOnScreen;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.helper.CustomBorder;
import org.grandviewtech.userinterface.listeners.ColumnScreenFocusListener;
import org.grandviewtech.userinterface.listeners.ColumnScreenKeyPressListener;
import org.grandviewtech.userinterface.listeners.ColumnScreenMouseClickListener;
import org.grandviewtech.userinterface.listeners.SettingsMouseClickListener;

public class ColumnScreen extends JPanel implements PreferredDimension, DropTargetListener, Icons
	{
		private static final long	serialVersionUID	= -4357735797077739462L;
		private ColumnScreen		previous;
		private ColumnScreen		next;
		private ColumnScreen		above;
		private ColumnScreen		below;
		private int					rowNumber;
		private int					columnNumber;
		private boolean				paintDefault		= true;
		final static Screen			SCREEN				= Screen.getInstance();
		private JLabel				setting				= new JLabel(SETTING);
		private JLabel				valueLabel			= new JLabel("");
		private boolean				isBlank				= true;
		private String				tag					= "";
		private String				value;
		private String				coil;
		
		public ColumnScreen()
			{
				init();
			}
			
		private void init()
			{
				setLayout(null);
				valueLabel.setBounds(10, 10, 50, 20);
				add(valueLabel);
				setPreferredSize(CELL_SIZE);
				setting.setBounds(getX() + 55, 0, 50, 20);
				setting.addMouseListener(new SettingsMouseClickListener(this));
				setBorder(new CustomBorder());
				setTransferHandler(new TransferHandler("icon"));
				new DropTarget(this, this);
				addMouseListener(new ColumnScreenMouseClickListener(this));
				addKeyListener(new ColumnScreenKeyPressListener(this));
				addFocusListener(new ColumnScreenFocusListener(this));
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
				this.valueLabel.setText(this.value);
				PaintCoilsOnScreen.paint(this, graphics);
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
										//dropTargetDragEvent.getDropTargetContext().dropComplete(false);
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
				if (coil != null)
					{
						this.paintDefault = false;
						this.coil = coil;
						setBlank(false);
					}
				else
					{
						this.paintDefault = true;
						this.coil = coil;
						setBlank(true);
					}
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
								ColumnConfigurationScreen columnConfigurationScreen = ColumnConfigurationScreen.getInstance();
								columnConfigurationScreen.initiateInstance(this);
								columnConfigurationScreen.requestFocusInWindow();
								ClipBoard.setCurrentRowNumber(rowNumber);
								ClipBoard.setCurrentColumnNumber(columnNumber);
								setCoil(Coils.LOAD);
								break;
							}
						case Coils.JUMP:
							{
								setCoil(Coils.JUMP);
								break;
							}
					}
			}
			
		public JLabel getSetting()
			{
				return setting;
			}
			
		public String getValue()
			{
				return value;
			}
			
		public void setValue(String value)
			{
				this.value = value;
			}
			
		public JLabel getValueLabel()
			{
				return valueLabel;
			}
			
		public void setValueLabel(JLabel valueLabel)
			{
				this.valueLabel = valueLabel;
			}
			
		public boolean isBlank()
			{
				return isBlank;
			}
			
		public void setBlank(boolean isBlank)
			{
				this.isBlank = isBlank;
			}
			
		public void reset()
			{
				remove(setting);
				setBlank(true);
				setCoil(null);
				setValue(null);
			}
			
		public String getTag()
			{
				return tag;
			}
			
		public void setTag(String tag)
			{
				this.tag = tag;
			}
			
		@Override
		public String toString()
			{
				return "ColumnScreen [rowNumber=" + rowNumber + ", columnNumber=" + columnNumber + ", paintDefault=" + paintDefault + ", coil=" + coil + ", value=" + value + "]";
			}
			
	}
