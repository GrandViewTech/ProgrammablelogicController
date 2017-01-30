package org.grandviewtech.userinterface.misc;

import java.util.Iterator;
import java.util.List;

import org.grandviewtech.constants.CustomBorderList;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CLIPBOARD_ACTION;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public final class ActionBot extends BaseBot
	{
		final private static Screen SCREEN = Screen.getInstance();
		
		public static void defaultHalt()
			{
				try
					{
						Thread.sleep(10);
					}
				catch (InterruptedException exception)
					{
						exception.printStackTrace();
					}
			}
			
		public static void select(ColumnScreen columnScreen)
			{
				/*
				 * if (ClipBoard.isControlKeyActive() == false) {
				 * columnScreen.setBorder(BorderFactory.createLineBorder(Color.
				 * blue, 1)); } else { if (columnScreen.isBlank() == false) {
				 * columnScreen.setBorder(CustomBorderList.DASHED_BORDER);
				 * ClipBoard.addCopiedCell(columnScreen); } }
				 */
			}
			
		public static void resetSelection(ColumnScreen columnScreen)
			{
				if (ClipBoard.isControlKeyActive() == false)
					{
						columnScreen.setBorder(CustomBorderList.CUSTOM_BORDER);
					}
			}
			
		public static void focusGained(ColumnScreen columnScreen)
			{
				// select(columnScreen);
				columnScreen.requestFocus();
				// SCREEN.setActiveColumn(columnScreen);
				
			}
			
		public static void focusLost(ColumnScreen columnScreen)
			{
				// SCREEN.setActiveColumn(null);
				resetSelection(columnScreen);
			}
			
		public static void pasteColumn()
			{
				ClipBoard.cutLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
				ClipBoard.copyLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
				ClipBoard.pasteLabel.setBorder(CustomBorderList.PADDED_DASHED_BORDER);
				System.out.println("Pasting : " + ClipBoard.getCopiedCell());
				ColumnScreen target = ClipBoard.SCREEN.getRow(ClipBoard.getCurrentRowNumber()).getColumnScreens(ClipBoard.getCurrentColumnNumber());
				for (ColumnScreen columnScreen : ClipBoard.getCopiedCell())
					{
						pasteIndividualColumn(columnScreen, target);
						target = target.getNext(false);
					}
				if (ClipBoard.getClipboardAction().equals(CLIPBOARD_ACTION.CUT))
					{
						List<ColumnScreen> copied = ClipBoard.getCopiedCell();
						Iterator<ColumnScreen> i$ = copied.iterator();
						while (i$.hasNext())
							{
								ColumnScreen columnScreen = i$.next();
								columnScreen.reset();
								columnScreen.repaint();
							}
					}
					
			}
			
		public static void copyColumn()
			{
				cutOrcopy();
			}
			
		public static void cutColumn()
			{
				cutOrcopy();
			}
			
		private static void cutOrcopy()
			{
				switch (ClipBoard.getClipboardAction())
					{
						case COPY:
							{
								ClipBoard.cutLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
								ClipBoard.copyLabel.setBorder(CustomBorderList.PADDED_DASHED_BORDER);
								ClipBoard.pasteLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
								ClipBoard.setClipBoardColumnSelection(false);
								break;
							}
						case CUT:
							{
								ClipBoard.cutLabel.setBorder(CustomBorderList.PADDED_DASHED_BORDER);
								ClipBoard.copyLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
								ClipBoard.pasteLabel.setBorder(CustomBorderList.PADDED_DEFAULT);
								ClipBoard.setClipBoardColumnSelection(true);
								break;
							}
						default:
							{
								break;
							}
							
					}
					
			}
	}
