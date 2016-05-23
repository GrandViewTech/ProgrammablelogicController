package org.grandviewtech.userinterface.misc;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;

import org.grandviewtech.constants.Borders;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.enums.CLIPBOARD_ACTION;
import org.grandviewtech.service.system.Printer;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public final class ActionBot extends BaseBot
	{
		
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
				if (ClipBoard.isControlKeyActive() == false)
					{
						columnScreen.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
					}
				else
					{
						if (columnScreen.isBlank() == false)
							{
								columnScreen.setBorder(Borders.DASHED_BORDER);
								ClipBoard.addCopiedCell(columnScreen);
							}
					}
			}
			
		public static void resetSelection(ColumnScreen columnScreen)
			{
				if (ClipBoard.isControlKeyActive() == false)
					{
						columnScreen.setBorder(Borders.CUSTOM_BORDER);
					}
			}
			
		public static void focusGained(ColumnScreen columnScreen)
			{
				Printer.print("Focus Gained For " + columnScreen.getColumnNumber());
				ColumnScreenGenerator.createColumnNeighbourHood(ClipBoard.SCREEN.getRow(columnScreen.getRowNumber()), columnScreen);
				select(columnScreen);
				columnScreen.requestFocus();
				CustomToolBar.setPointerValue("Row : " + columnScreen.getRowNumber() + " Column : " + columnScreen.getColumnNumber());
				ClipBoard.setCurrentRowNumber(columnScreen.getRowNumber());
				ClipBoard.setCurrentColumnNumber(columnScreen.getColumnNumber());
			}
			
		public static void focusLost(ColumnScreen columnScreen)
			{
				Printer.print("Focus Lost For " + columnScreen.getColumnNumber());
				columnScreen.setAbove(null);
				columnScreen.setBelow(null);
				columnScreen.setPrevious(null);
				columnScreen.setNext(null);
				resetSelection(columnScreen);
			}
			
		public static void pasteColumn()
			{
				ClipBoard.cutLabel.setBorder(PADDED_DEFAULT);
				ClipBoard.copyLabel.setBorder(PADDED_DEFAULT);
				ClipBoard.pasteLabel.setBorder(PADDED_DASHED_BORDER);
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
								ClipBoard.cutLabel.setBorder(PADDED_DEFAULT);
								ClipBoard.copyLabel.setBorder(PADDED_DASHED_BORDER);
								ClipBoard.pasteLabel.setBorder(PADDED_DEFAULT);
								ClipBoard.setClipBoardColumnSelection(false);
								break;
							}
						case CUT:
							{
								ClipBoard.cutLabel.setBorder(PADDED_DASHED_BORDER);
								ClipBoard.copyLabel.setBorder(PADDED_DEFAULT);
								ClipBoard.pasteLabel.setBorder(PADDED_DEFAULT);
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
