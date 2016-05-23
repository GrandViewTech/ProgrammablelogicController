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

import org.grandviewtech.entity.bo.SearchResult;
import org.grandviewtech.userinterface.listeners.NextButtonMouseListener;
import org.grandviewtech.userinterface.listeners.PreviousButtonMouseListener;
import org.grandviewtech.userinterface.listeners.SearchButtonListeners;

final public class Search
	{
		private static int					currentIndex	= -1;
		private static List<SearchResult>	searchResults	= new ArrayList<SearchResult>();
		private static JLabel				searchLabel		= new JLabel("Search :");
		private static JLabel				previous		= new JLabel("previous");
		private static JLabel				next			= new JLabel("next");
		private static JButton				go				= new JButton("Go");
		private static JTextField			searchTextField	= new JTextField(10);
		
		static
			{
				searchLabel.setToolTipText("Free Text Search");
				searchTextField.setToolTipText("Enter the Search Value");
				searchTextField.setMaximumSize(searchTextField.getPreferredSize());
				previous.setToolTipText("Previous");
				previous.setEnabled(false);
				previous.addMouseListener(new PreviousButtonMouseListener());
				next.setToolTipText("Next");
				next.addMouseListener(new NextButtonMouseListener());
				next.setEnabled(false);
				go.setToolTipText("Proceed For Search");
				go.addMouseListener(new SearchButtonListeners());
			}
			
		public static JLabel getSearchLabel()
			{
				return searchLabel;
			}
			
		public static JLabel getPrevious()
			{
				return previous;
			}
			
		public static JLabel getNext()
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
			
	}
