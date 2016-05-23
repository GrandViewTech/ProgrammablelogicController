package org.grandviewtech.userinterface.screen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import org.grandviewtech.constants.Icons;
import org.grandviewtech.userinterface.listeners.AdvancedSearchMouseListener;
import org.grandviewtech.userinterface.misc.CustomToolBar;
import org.grandviewtech.userinterface.misc.Helper;

public class CustomHeader implements Icons
	{
		
		public static JMenuBar getJMenuBar()
			{
				JMenuBar menuBar = new JMenuBar();
				JMenu fileMenu = new JMenu("File");
				menuBar.add(fileMenu);
				Helper.setCursor(menuBar);
				JButton advancedSearch = new JButton("Advanced Search");
				advancedSearch.addMouseListener(new AdvancedSearchMouseListener());
				advancedSearch.setToolTipText("Advanced Search");
				menuBar.add(advancedSearch);
				// PARTITION
				menuBar.add(Box.createHorizontalGlue());
				menuBar.add(Search.getSearchLabel());
				menuBar.add(Search.getSearchTextField());
				menuBar.add(Search.getGo());
				menuBar.add(Search.getPrevious());
				JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
				Dimension dimension = new Dimension(1, 20);
				separator.setMaximumSize(dimension);
				separator.setBackground(Color.BLACK);
				menuBar.add(separator);
				menuBar.add(Search.getNext());
				menuBar.add(new JLabel("  "));
				return menuBar;
			}
			
		public static JToolBar getJToolBar()
			{
				return CustomToolBar.getToolBar();
			}
			
		public static JToolBar getColoumnMarker()
			{
				return CustomToolBar.getColoumnMarker();
			}
			
		public static class DragMouseAdapter extends MouseAdapter
			{
				public void mousePressed(MouseEvent event)
					{
						JComponent component = (JComponent) event.getSource();
						TransferHandler handler = component.getTransferHandler();
						handler.exportAsDrag(component, event, TransferHandler.COPY);
					}
			}
			
	}
