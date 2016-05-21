package org.grandviewtech.entity.bo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.border.Border;

import org.grandviewtech.constants.Borders;
import org.grandviewtech.entity.enums.CLIPBOARD_ACTION;
import org.grandviewtech.userinterface.screen.ColumnScreen;

final public class ClipBoard implements Borders
	{
		private ClipBoard()
			{
			}
			
		public static final Screen SCREEN = Screen.getInstance();
		
		private final static class ClipBoardInstanceFactory
			{
				final static ClipBoard instance = new ClipBoard();
			}
			
		public static ClipBoard getInstance()
			{
				return ClipBoardInstanceFactory.instance;
			}
			
		private static CLIPBOARD_ACTION		clipboardAction;
		public static JLabel				cutLabel;
		public static JLabel				copyLabel;
		public static JLabel				pasteLabel;
		
		private static boolean				isControlKeyActive	= false;
		
		private static String				clipBoardType;
		
		private static List<ColumnScreen>	tempData			= new ArrayList<ColumnScreen>();
		private static List<ColumnScreen>	copiedCell			= new ArrayList<ColumnScreen>();
		
		private static int					currentRowNumber;
		private static int					currentColumnNumber;
		
		public static String getClipBoardType()
			{
				return clipBoardType;
			}
			
		public static void setClipBoardType(String clipBoardType)
			{
				ClipBoard.clipBoardType = clipBoardType;
			}
			
		public static List<ColumnScreen> getCopiedCell()
			{
				return ClipBoard.copiedCell;
			}
			
		public static void addCopiedCell(ColumnScreen columnScreen)
			{
				if (ClipBoard.isControlKeyActive)
					{
						if (columnScreen.isBlank() == false)
							{
								ClipBoard.tempData.add(columnScreen);
							}
					}
				System.out.println(ClipBoard.tempData.toString());
			}
			
		public static boolean isControlKeyActive()
			{
				return ClipBoard.isControlKeyActive;
			}
			
		public static void setControlKeyActive(boolean isControlKeyActive)
			{
				ClipBoard.isControlKeyActive = isControlKeyActive;
			}
			
		public static int getCurrentRowNumber()
			{
				return ClipBoard.currentRowNumber;
			}
			
		public static void setCurrentRowNumber(int currentRowNumber)
			{
				ClipBoard.currentRowNumber = currentRowNumber;
			}
			
		public static int getCurrentColumnNumber()
			{
				return ClipBoard.currentColumnNumber;
			}
			
		public static void setCurrentColumnNumber(int currentColumnNumber)
			{
				ClipBoard.currentColumnNumber = currentColumnNumber;
			}
			
		public static void setClipBoardColumnSelection(boolean isCutAction)
			{
				setClipBoardType("Column");
				Border cellBorder = (isCutAction) ? DASHED_BORDER_RED : DASHED_BORDER;
				if (ClipBoard.tempData.isEmpty() == false)
					{
						for (ColumnScreen columnScreen : ClipBoard.tempData)
							{
								
								columnScreen.setBorder(cellBorder);
								ClipBoard.copiedCell.add(columnScreen);
							}
					}
				else
					{
						ColumnScreen columnScreen = SCREEN.getRow(ClipBoard.currentRowNumber).getColumnScreens(ClipBoard.currentColumnNumber);
						columnScreen.setBorder(cellBorder);
						ClipBoard.copiedCell.add(columnScreen);
					}
			}
			
		public static void resetClipBoardColumnSelection()
			{
				if (ClipBoard.copiedCell.isEmpty() == false)
					{
						for (ColumnScreen columnScreen : ClipBoard.copiedCell)
							{
								columnScreen.setBorder(CUSTOM_BORDER);
							}
					}
			}
			
		public static CLIPBOARD_ACTION getClipboardAction()
			{
				return ClipBoard.clipboardAction;
			}
			
		public static void setClipboardAction(CLIPBOARD_ACTION clipboardAction)
			{
				ClipBoard.clipboardAction = clipboardAction;
			}
			
	}
