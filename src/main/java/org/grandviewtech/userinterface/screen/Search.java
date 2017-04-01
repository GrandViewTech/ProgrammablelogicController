package org.grandviewtech.userinterface.screen;

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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.grandviewtech.constants.CustomIcon;
import org.grandviewtech.entity.bo.SearchResult;
import org.grandviewtech.userinterface.listeners.AdvancedSearchMouseListener;
import org.grandviewtech.userinterface.listeners.SearchListener;

final public class Search
	{
		private static int					currentIndex	= -1;
		private static List<SearchResult>	searchResults	= new ArrayList<SearchResult>();
		private static JLabel				searchLabel		= new JLabel("Search :");
		private static JButton				previous		= new JButton(CustomIcon.LEFT_ICON);
		private static JButton				next			= new JButton(CustomIcon.RIGHT_ICON);
		private static JButton				go				= new JButton(CustomIcon.SEARCH_ICON);
		private static JButton				advancedSearch	= new JButton(CustomIcon.ADVANCED_SEARCH_ICON);
		private static JTextField			searchTextField	= new JTextField(10);
		
		static
			{
				searchLabel.setToolTipText("Free Text Search");
				searchTextField.setToolTipText("Enter the Search Value");
				searchTextField.setMaximumSize(searchTextField.getPreferredSize());
				previous.setToolTipText("-Previous-");
				previous.setEnabled(false);
				SearchListener searchListener = new SearchListener();
				previous.addMouseListener(searchListener);
				next.setToolTipText("-Next-");
				next.setName(">");
				previous.setName("<");
				next.addMouseListener(searchListener);
				next.setEnabled(false);
				go.setToolTipText("-Search-");
				go.addMouseListener(searchListener);
				searchTextField.addKeyListener(searchListener);
				advancedSearch.addMouseListener(new AdvancedSearchMouseListener());
			}
			
		public static JLabel getSearchLabel()
			{
				return searchLabel;
			}
			
		public static JButton getPrevious()
			{
				return previous;
			}
			
		public static JButton getNext()
			{
				return next;
			}
			
		public static JButton getGo()
			{
				return go;
			}
			
		public static JTextField getSearchTextField()
			{
				return searchTextField;
			}
			
		public static List<SearchResult> getSearchResults()
			{
				return searchResults;
			}
			
		public static void setSearchResults(List<SearchResult> searchResults)
			{
				Search.searchResults = searchResults;
			}
			
		public static int getCurrentIndex()
			{
				return currentIndex;
			}
			
		public static void setCurrentIndex(int currentIndex)
			{
				Search.currentIndex = currentIndex;
			}
			
		public static JButton getAdvancedSearch()
			{
				return advancedSearch;
			}
			
	}
