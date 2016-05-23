
package org.grandviewtech.userinterface.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CLIPBOARD_ACTION;
import org.grandviewtech.userinterface.helper.RowGenerator;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;
import org.grandviewtech.userinterface.screen.Rung;

public class RungActionListerner implements ActionListener
	{
		private static Screen	screen	= Screen.getInstance();
		private RungAction		rungAction;
		private int				currentRungNumber;
		
		public static enum RungAction
			{
			CUT, COPY, PASTE, NEW, DELETE;
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
						
					}
					
			}
			
		private void delete()
			{
				RowGenerator.deleteRow(currentRungNumber);
			}
			
		private void _new()
			{
				int newRungNumber = currentRungNumber + 1;
				RowGenerator.regenerateRow(newRungNumber);
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
										pasteColumn.setCoil(copiedColumn.getCoil());
										pasteColumn.setValue(copiedColumn.getValue());
										pasteColumn.setTag(copiedColumn.getTag());
										pasteColumn.requestFocus();
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
						System.out.println("deletedRowNumbers : " + deletedRowNumbers);
						RowGenerator.deleteRow(deletedRowNumbers);
					}
			}
	}
