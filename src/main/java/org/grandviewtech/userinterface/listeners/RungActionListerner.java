
package org.grandviewtech.userinterface.listeners;

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CLIPBOARD_ACTION;
import org.grandviewtech.service.searching.SearchEngine;
import org.grandviewtech.userinterface.helper.RowGenerator;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;
import org.grandviewtech.userinterface.screen.Rung;
import org.grandviewtech.userinterface.screen.RungComment;

public class RungActionListerner implements ActionListener
	{
		private static Screen	screen	= Screen.getInstance();
		private RungAction		rungAction;
		private int				currentRungNumber;
		
		public static enum RungAction
			{
			CUT, COPY, PASTE, NEW, DELETE, COMMENT;
			}
			
		public RungActionListerner(RungAction rungAction, int currentRungNumber)
			{
				super();
				this.rungAction = rungAction;
				this.currentRungNumber = currentRungNumber;
			}
			
		@Override
		public void actionPerformed(ActionEvent event)
			{
				switch (rungAction)
					{
						case COPY:
							{
								copy();
								ClipBoard.setClipboardAction(CLIPBOARD_ACTION.COPY);
								break;
							}
						case CUT:
							{
								copy();
								ClipBoard.setClipboardAction(CLIPBOARD_ACTION.CUT);
								break;
							}
						case DELETE:
							{
								delete();
								break;
							}
						case NEW:
							{
								_new();
								break;
							}
						case PASTE:
							{
								paste();
								break;
							}
						case COMMENT:
							{
								RungComment rungComment = new RungComment();
								RowScreen rowScreen = screen.getRow(currentRungNumber);
								if (rowScreen != null)
									{
										rungComment.initiateRungComment(rowScreen.getRung());
									}
								break;
							}
						
					}
					
			}
			
		private void delete()
			{
				int newRungNumber = currentRungNumber;
				List<Rung> rungs = ClipBoard.getSelectedRung();
				if (rungs.size() == 0)
					{
						RowGenerator.regenerateRow(newRungNumber, false);
					}
				else
					{
						Set<Integer> rowNumbers = new HashSet<Integer>();
						for (Rung rung : rungs)
							{
								Integer rowNumber = rung.getRowNumber();
								if (rowNumber != null)
									{
										rowNumbers.add(rowNumber);
									}
							}
						RowGenerator.deleteRows(rowNumbers);
					}
				ClipBoard.resetClipBoardSelection();
			}
			
		private void _new()
			{
				int newRungNumber = currentRungNumber;
				RowGenerator.regenerateRow(newRungNumber, true);
			}
			
		private void copy()
			{
				ClipBoard.addCopiedRung();
			}
			
		private void paste()
			{
				int i$ = 0;
				for (int rowNumber = currentRungNumber; rowNumber < currentRungNumber + ClipBoard.getCopiedRung().size(); rowNumber++)
					{
						RowScreen copiedRow = screen.getRow(ClipBoard.getCopiedRung().get(i$).getRowNumber());
						RowScreen pasteRow = screen.getRow(rowNumber);
						int columnIndex = 1;
						for (ColumnScreen pasteColumn : pasteRow.getAllColumnScreens())
							{
								ColumnScreen copiedColumn = copiedRow.getColumnScreens(columnIndex);
								if (copiedColumn.isBlank() == false)
									{
										pasteColumn.setCoilType(copiedColumn.getCoilType());
										pasteColumn.setValue(copiedColumn.getValue());
										pasteColumn.setTag(copiedColumn.getTag());
										pasteColumn.requestFocus();
										SearchEngine.index(pasteColumn);
									}
								columnIndex = columnIndex + 1;
								
							}
						i$ = i$ + 1;
					}
				if (ClipBoard.getClipboardAction() == CLIPBOARD_ACTION.CUT)
					{
						Set<Integer> deletedRowNumbers = new LinkedHashSet<Integer>();
						List<Rung> copiedRungs = ClipBoard.getCopiedRung();
						Collections.sort(copiedRungs);
						for (Rung rung : copiedRungs)
							{
								deletedRowNumbers.add(rung.getRowNumber());
							}
						RowGenerator.deleteRows(deletedRowNumbers);
					}
			}
	}
