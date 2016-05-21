package org.grandviewtech.userinterface.screen;

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
