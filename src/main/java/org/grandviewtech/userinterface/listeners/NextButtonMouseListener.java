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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.bo.SearchResult;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.Search;

public class NextButtonMouseListener implements MouseListener
	{
		private static volatile Screen screen = Screen.getInstance();
		
		@Override
		public void mouseClicked(MouseEvent mouseEvent)
			{
				List<SearchResult> searchResults = Search.getSearchResults();
				int currentIndex = Search.getCurrentIndex();
				int updatedIndex = currentIndex + 1;
				if (updatedIndex >= searchResults.size())
					{
						Search.getNext().setEnabled(false);
						return;
					}
				else
					{
						SearchResult searchResult = searchResults.get(updatedIndex);
						ColumnScreen columnScreen = screen.getRow(searchResult.getRowNumber()).getColumnScreens(searchResult.getColumnNumber());
						columnScreen.requestFocus();
						Search.setCurrentIndex(updatedIndex);
						if (updatedIndex > 0)
							{
								Search.getPrevious().setEnabled(true);
							}
						else
							{
								Search.getPrevious().setEnabled(false);
							}
					}
			}
			
		@Override
		public void mousePressed(MouseEvent mouseEvent)
			{
				
			}
			
		@Override
		public void mouseReleased(MouseEvent mouseEvent)
			{
				
			}
			
		@Override
		public void mouseEntered(MouseEvent mouseEvent)
			{
				
			}
			
		@Override
		public void mouseExited(MouseEvent mouseEvent)
			{
				
			}
			
	}
