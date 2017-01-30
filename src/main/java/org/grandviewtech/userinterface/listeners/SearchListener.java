package org.grandviewtech.userinterface.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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

import javax.swing.JButton;
import javax.swing.JLabel;

import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.bo.SearchResult;
import org.grandviewtech.service.searching.SearchEngine;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.Search;

public class SearchListener implements MouseListener, KeyListener
	{
		
		@Override
		public void mouseClicked(MouseEvent mouseEvent)
			{
				if (mouseEvent.getSource() instanceof JLabel)
					{
						JLabel label = (JLabel) mouseEvent.getSource();
						if (label.isEnabled())
							{
								List<SearchResult> searchResults = Search.getSearchResults();
								String text = label.getText();
								if (text.equalsIgnoreCase("previous") || text.equalsIgnoreCase("<"))
									{
										previous(searchResults);
									}
								else
									{
										next(searchResults);
									}
							}
					}
				else if (mouseEvent.getSource() instanceof JButton)
					{
						search();
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
			
		public void previous(List<SearchResult> searchResults)
			{
				int currentIndex = Search.getCurrentIndex();
				int updatedIndex = currentIndex - 1;
				if (updatedIndex == -1)
					{
						Search.getPrevious().setEnabled(false);
						return;
					}
				else
					{
						SearchResult searchResult = searchResults.get(updatedIndex);
						ColumnScreen columnScreen = Screen.getInstance().getRow(searchResult.getRowNumber()).getColumnScreens(searchResult.getColumnNumber());
						columnScreen.requestFocus();
						Search.setCurrentIndex(updatedIndex);
						if (updatedIndex < searchResults.size())
							{
								Search.getNext().setEnabled(true);
							}
						else
							{
								Search.getNext().setEnabled(false);
							}
					}
			}
			
		public void next(List<SearchResult> searchResults)
			{
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
						ColumnScreen columnScreen = Screen.getInstance().getRow(searchResult.getRowNumber()).getColumnScreens(searchResult.getColumnNumber());
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
			
		public void clearSearch()
			{
				String searchKeyWord = Search.getSearchTextField().getText();
				if (searchKeyWord == null || searchKeyWord.trim().length() == 0)
					{
						Search.getSearchLabel().setText("Search :");
					}
			}
			
		public void search()
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
		public void keyTyped(KeyEvent e)
			{
				
			}
			
		@Override
		public void keyPressed(KeyEvent e)
			{
				search();
			}
			
		@Override
		public void keyReleased(KeyEvent e)
			{
				clearSearch();
			}
			
	}
