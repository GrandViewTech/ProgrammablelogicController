package org.grandviewtech.entity.bo;

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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.border.Border;

import org.grandviewtech.constants.CustomBorderList;
import org.grandviewtech.entity.enums.CLIPBOARD_ACTION;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;
import org.grandviewtech.userinterface.screen.Rung;

final public class ClipBoard
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
			
		private static JLabel				selection			= new JLabel("SELECTION : OFF ");
		private static CLIPBOARD_ACTION		clipboardAction		= CLIPBOARD_ACTION.DEFUALT;
		public static JLabel				cutLabel;
		public static JLabel				copyLabel;
		public static JLabel				pasteLabel;
		
		private static boolean				isControlKeyActive	= false;
		
		private static String				clipBoardType;
		private static List<Rung>			copiedTempRung		= new ArrayList<Rung>();
		private static List<Rung>			copiedRung			= new ArrayList<Rung>();
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
			
		public static List<Rung> getCopiedRung()
			{
				return copiedRung;
			}
			
		public static List<Rung> getSelectedRung()
			{
				return copiedTempRung;
			}
			
		public static void addTempRung(Rung rung)
			{
				if (copiedTempRung.contains(rung) == false)
					{
						if (ClipBoard.isControlKeyActive)
							{
								copiedTempRung.add(rung);
							}
						else
							{
								copiedTempRung.clear();
								copiedTempRung.add(rung);
							}
						rung.setBackground(java.awt.Color.LIGHT_GRAY);
					}
			}
			
		public static void addCopiedRung()
			{
				for (Rung rung : copiedTempRung)
					{
						if (copiedRung.contains(rung) == false)
							{
								int rowNumber = rung.getRowNumber();
								RowScreen rowScreen = SCREEN.getRow(rowNumber);
								if (clipboardAction == CLIPBOARD_ACTION.COPY)
									{
										rowScreen.setBackground(java.awt.Color.GREEN);
									}
								else if (clipboardAction == CLIPBOARD_ACTION.CUT)
									{
										rowScreen.setBackground(java.awt.Color.RED);
									}
								else
									{
										rowScreen.setBackground(java.awt.Color.RED);
									}
								copiedRung.add(rung);
							}
					}
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
			}
			
		public static boolean isControlKeyActive()
			{
				return ClipBoard.isControlKeyActive;
			}
			
		public static void setControlKeyActive(boolean isControlKeyActive)
			{
				ClipBoard.isControlKeyActive = isControlKeyActive;
				if (ClipBoard.isControlKeyActive == true)
					{
						selection.setText("SELECTION : ON ");
					}
				else
					{
						selection.setText("SELECTION : OFF ");
					}
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
				Border cellBorder = (isCutAction) ? CustomBorderList.DASHED_BORDER_RED : CustomBorderList.DASHED_BORDER;
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
			
		public static void resetClipBoardSelection()
			{
				if (ClipBoard.copiedCell.isEmpty() == false)
					{
						for (ColumnScreen columnScreen : ClipBoard.copiedCell)
							{
								columnScreen.setBorder(CustomBorderList.CUSTOM_BORDER);
							}
						ClipBoard.copiedCell.clear();
					}
				if (ClipBoard.copiedRung.isEmpty() == false)
					{
						for (Rung copiedRung : ClipBoard.copiedRung)
							{
								copiedRung.setBackground(null);
							}
						ClipBoard.copiedCell.clear();
					}
				if (ClipBoard.copiedTempRung.isEmpty() == false)
					{
						for (Rung copiedRung : ClipBoard.copiedRung)
							{
								copiedRung.setBackground(null);
							}
						ClipBoard.copiedTempRung.clear();
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
			
		public static JLabel getSelection()
			{
				return selection;
			}
			
	}
