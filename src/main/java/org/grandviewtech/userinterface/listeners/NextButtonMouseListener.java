package org.grandviewtech.userinterface.listeners;

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
