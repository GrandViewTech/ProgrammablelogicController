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

import org.grandviewtech.entity.bo.SearchResult;
import org.grandviewtech.service.searching.SearchEngine;
import org.grandviewtech.userinterface.screen.Search;

public class SearchButtonListeners implements MouseListener
	{
		
		@Override
		public void mouseClicked(MouseEvent e)
			{
				String searchKeyWord = Search.getSearchTextField().getText();
				List<SearchResult> searchResults = SearchEngine.search(searchKeyWord);
				if (searchResults.isEmpty())
					{
						Search.getSearchLabel().setText("No Result Found");
					}
				else
					{
						Search.getSearchLabel().setText(searchResults.size() + " Result Found");
						Search.getNext().setEnabled(true);
						Search.setCurrentIndex(-1);
						Search.setSearchResults(searchResults);
					}
					
			}
			
		@Override
		public void mousePressed(MouseEvent e)
			{
				 
				
			}
			
		@Override
		public void mouseReleased(MouseEvent e)
			{
				 
				
			}
			
		@Override
		public void mouseEntered(MouseEvent e)
			{
				 
				
			}
			
		@Override
		public void mouseExited(MouseEvent e)
			{

				
			}
			
	}
