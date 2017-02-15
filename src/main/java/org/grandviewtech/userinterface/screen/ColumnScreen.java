
package org.grandviewtech.userinterface.screen;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.border.Border;

import org.grandviewtech.constants.CustomBorderList;
import org.grandviewtech.constants.CustomDimension;
import org.grandviewtech.constants.CustomIcon;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.bo.Response;
import org.grandviewtech.entity.bo.Routine;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.Behavior;
import org.grandviewtech.entity.enums.BehaviorLayout;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.entity.enums.Edge;
import org.grandviewtech.entity.enums.InputType;
import org.grandviewtech.entity.enums.NoNc;
import org.grandviewtech.service.validation.RowValidation;
import org.grandviewtech.service.validation.ValidateDragOption;
import org.grandviewtech.userinterface.coils.PaintCoilsOnScreen;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.helper.CustomBorder;
import org.grandviewtech.userinterface.listeners.ColumnScreenListener;
import org.grandviewtech.userinterface.listeners.SettingsMouseClickListener;

public class ColumnScreen extends JPanel implements DropTargetListener, Comparable<ColumnScreen>
	{
		private static final long		serialVersionUID	= -4357735797077739462L;
		private ColumnScreen			previous;
		private ColumnScreen			next;
		private ColumnScreen			above;
		private ColumnScreen			below;
		private int						rowNumber;
		private int						columnNumber;
		static Screen					SCREEN				= Screen.getInstance();
		private JLabel					setting				= new JLabel(CustomIcon.SETTING);
		private JLabel					valueLabel			= new JLabel("");
		private boolean					isBlank				= true;
		private JLabel					tagLabel			= new JLabel();
		private String					tag					= "";
		private String					value;
		private CoilType				temp				= CoilType.DEFAULT;
		private CoilType				coilType			= CoilType.DEFAULT;
		private CoilType				childType			= CoilType.DEFAULT;
		private String					comment;
		private InputType				inputType;
		private NoNc					nonc				= NoNc.DEFAULT;
		private Edge					edge				= Edge.DEFAULT;
		private boolean					parent				= false;
		private boolean					error				= false;
		private Routine					routine;
		// Need To Check Its Usage
		private Behavior				behavior			= Behavior.DEFAULT;
		private List<BehaviorLayout>	behaviorLayouts		= new ArrayList<>();
		
		public List<BehaviorLayout> getBehaviorLayouts()
			{
				return behaviorLayouts;
			}
			
		public void addBehaviorLayout(BehaviorLayout behaviorLayout)
			{
				if (this.behaviorLayouts == null)
					{
						this.behaviorLayouts = new ArrayList<>();
					}
				if (behaviorLayout != null)
					{
						this.behaviorLayouts.add(behaviorLayout);
					}
			}
			
		public Behavior getBehavior()
			{
				return behavior;
			}
			
		public void setBehavior(Behavior behavior)
			{
				this.behavior = behavior;
			}
			
		public int getColumnNumber()
			{
				return columnNumber;
			}
			
		public void setColumnNumber(int columnNumber)
			{
				this.columnNumber = columnNumber;
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
			
		public CoilType getCoilType()
			{
				return coilType;
			}
			
		public void setCoilType(CoilType coilType)
			{
				if (coilType != null)
					{
						this.setTemp(coilType);
						this.coilType = coilType;
						setBlank(false);
					}
				else
					{
						this.coilType = CoilType.DEFAULT;
						this.setTemp(CoilType.DEFAULT);
						setBlank(true);
					}
			}
			
		public void apply()
			{
				// SCREEN.setActiveColumn(this);
				setCoilType(temp);
				RowValidation.validate(this);
				repaint();
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
				setCoilType(null);
				setValue(null);
				setTag("");
				valueLabel.setText("");
				setError(false);
				setBorder(CustomBorderList.CUSTOM_BORDER);
				repaint();
			}
			
		public String getTag()
			{
				return tag;
			}
			
		public void setTag(String tag)
			{
				this.tag = tag;
				String tempText = (tag.length() > 20) ? tag.substring(0, 16) + "..." : tag;
				this.tagLabel.setText(tempText);
				this.tagLabel.setToolTipText(tag);
			}
			
		public String getComment()
			{
				return comment;
			}
			
		public void setComment(String comment)
			{
				this.comment = comment;
			}
			
		public InputType getInputType()
			{
				return inputType;
			}
			
		public void setInputType(InputType inputType)
			{
				this.inputType = inputType;
			}
			
		public NoNc getNonc()
			{
				return nonc;
			}
			
		public void setNonc(NoNc nonc)
			{
				if (nonc == null)
					{
						nonc = NoNc.DEFAULT;
					}
				this.nonc = nonc;
			}
			
		public Edge getEdge()
			{
				return edge;
			}
			
		public void setEdge(Edge edge)
			{
				if (edge == null)
					{
						edge = Edge.DEFAULT;
					}
				this.edge = edge;
			}
			
		@Override
		public int compareTo(ColumnScreen comparableColumnScreen)
			{
				Integer currentColumnNumber = columnNumber;
				Integer comparableColumnNumber = comparableColumnScreen.getColumnNumber();
				return currentColumnNumber.compareTo(comparableColumnNumber);
			}
			
		@Override
		protected void paintComponent(java.awt.Graphics graphics)
			{
				super.paintComponent(graphics);
				this.valueLabel.setText(this.valueLabel.getText());
				if (isBlank)
					{
						PaintCoilsOnScreen.paintDefault(this, graphics);
					}
				else
					{
						PaintCoilsOnScreen.paintDragOption(this, graphics);
					}
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
								String dragContent = (String) transferable.getTransferData(DataFlavor.stringFlavor);
								ColumnScreenGenerator.createColumnNeighbourHood(SCREEN.getRow(getRowNumber()), this);
								CoilType temp = CoilType.valueOf(dragContent.toUpperCase());
								Response response = ValidateDragOption.validateDragOption(this, temp);
								boolean isError = response.isError();
								if (!isError)
									{
										// dropTargetDragEvent.getDropTargetContext().getComponent().setCursor(DragSource.DefaultCopyDrop);
										dropTargetDragEvent.acceptDrop(DnDConstants.ACTION_MOVE);
										selectTheRigthCoilUsingDragOption(temp);
										// Graphics graphics = getGraphics();
										// paintComponent(graphics);
										DropTargetContext dropTargetContext = dropTargetDragEvent.getDropTargetContext();
										dropTargetContext.dropComplete(true);
									}
								else
									{
										// dropTargetDragEvent.getDropTargetContext().getComponent().setCursor(DragSource.DefaultCopyNoDrop);
										dropTargetDragEvent.rejectDrop();
										StringBuffer stringBuffer = new StringBuffer();
										int i = 1;
										for (String message : response.getMessages())
											{
												String text = "";
												if (i > 1)
													{
														text = "\n";
													}
												if (i <= 9)
													{
														text += ("0" + i + " : ");
													}
												else
													{
														text += (i + " : ");
													}
													
												stringBuffer.append(text + message);
												i = i + 1;
											}
										JOptionPane.showMessageDialog(null, stringBuffer.toString());
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
			
		public ColumnScreen()
			{
				init();
			}
			
		private void init()
			{
				setLayout(null);
				setOpaque(true);
				setBackground(Color.white);
				valueLabel.setBounds(35, 6, 50, 20);
				add(valueLabel);
				tagLabel.setBounds(10, 40, 50, 20);
				add(tagLabel);
				setPreferredSize(CustomDimension.CELL_SIZE);
				setting.setBounds(getX() + 55, 0, 50, 20);
				setting.addMouseListener(new SettingsMouseClickListener(this));
				setBorder(new CustomBorder());
				setTransferHandler(new TransferHandler("icon"));
				new DropTarget(this, this);
				ColumnScreenListener columnScreenListener = new ColumnScreenListener(this);
				addMouseListener(columnScreenListener);
				addKeyListener(columnScreenListener);
				addFocusListener(columnScreenListener);
				setToolTipText("Row : " + getRowNumber() + " |  Column : " + getColumnNumber());
				repaint();
			}
			
		private void selectTheRigthCoilUsingDragOption(CoilType coilType)
			{
				setTemp(coilType);
				switch (coilType)
					{
						case LINE:
							{
								apply();
								break;
							}
						case OUTPUT:
							{
								initColumnConfigurationScreen();
								break;
							}
						case LEFT_LINK:
							{
								initColumnConfigurationScreen();
								break;
							}
						case RIGHT_LINK:
							{
								initColumnConfigurationScreen();
								break;
							}
						case PARALLEL:
							{
								initColumnConfigurationScreen();
								break;
							}
						case LOAD:
							{
								initColumnConfigurationScreen();
								break;
							}
						case JUMP:
							{
								apply();
								break;
							}
						case END:
							{
								SCREEN.setEndRowNumber(rowNumber);
								SCREEN.setEndColumnNumber(columnNumber);
								apply();
								break;
							}
						case ROUTINE:
							{
								/*ColumnConfigurationScreen columnConfigurationScreen = new ColumnConfigurationScreen();
								columnConfigurationScreen.initiateInstance(this);
								columnConfigurationScreen.requestFocusInWindow();
								ClipBoard.setCurrentRowNumber(rowNumber);
								ClipBoard.setCurrentColumnNumber(columnNumber);*/
								initColumnConfigurationScreen();
								break;
							}
						case DEFAULT:
							{
								break;
							}
						case DELETE:
							{
								break;
							}
						case LABEL:
							{
								apply();
								break;
							}
							
					}
			}
			
		public CoilType getChildType()
			{
				return childType;
			}
			
		public void setChildType(CoilType childType)
			{
				this.parent = true;
				this.childType = childType;
			}
			
		@Override
		public void setBackground(Color bg)
			{
				super.setBackground(bg);
				if (bg == null)
					{
						setOpaque(true);
						setBackground(Color.white);
					}
			}
			
		public boolean isParent()
			{
				return parent;
			}
			
		public void setParent(boolean parent)
			{
				this.parent = parent;
			}
			
		public CoilType getTemp()
			{
				return temp;
			}
			
		public void setTemp(CoilType temp)
			{
				this.temp = temp;
			}
			
		public Routine getRoutine()
			{
				return routine;
			}
			
		public void setRoutine(Routine routine)
			{
				this.routine = routine;
			}
			
		private void initColumnConfigurationScreen()
			{
				ColumnConfigurationScreen columnConfigurationScreen = new ColumnConfigurationScreen();
				columnConfigurationScreen.initiateInstance(this);
				columnConfigurationScreen.requestFocusInWindow();
				ClipBoard.setCurrentRowNumber(rowNumber);
				ClipBoard.setCurrentColumnNumber(columnNumber);
				apply();
			}
			
		public boolean isError()
			{
				return error;
			}
			
		public void setError(boolean error)
			{
				this.error = error;
				if (error == true)
					{
						setBorder(CustomBorderList.ERROR_BORDER);
					}
				else
					{
						setBorder(CustomBorderList.CUSTOM_BORDER);
					}
				repaint();
			}
			
		@Override
		public void setBorder(Border border)
			{
				if (!error)
					{
						super.setBorder(border);
					}
				else
					{
						super.setBorder(CustomBorderList.ERROR_BORDER);
					}
			}
			
		@Override
		public String toString()
			{
				return "ColumnScreen [previous=" + previous + ", next=" + next + ", above=" + above + ", below=" + below + ", rowNumber=" + rowNumber + ", columnNumber=" + columnNumber + ", isBlank=" + isBlank + ", tag=" + tag + ", value=" + value + ", coilType=" + coilType + ", comment=" + comment + ", inputType=" + inputType + ", nonc=" + nonc + ", edge=" + edge + "]";
			}
			
		@Override
		public boolean equals(Object object)
			{
				if (object != null && object instanceof ColumnScreen)
					{
						ColumnScreen columnScreen = (ColumnScreen) object;
						int row = columnScreen.getRowNumber();
						int column = columnScreen.getColumnNumber();
						return ((row == this.rowNumber) && (this.columnNumber == column));
					}
				return false;
			}
	}
