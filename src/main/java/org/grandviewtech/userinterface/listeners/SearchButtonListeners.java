package org.grandviewtech.userinterface.listeners;

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
