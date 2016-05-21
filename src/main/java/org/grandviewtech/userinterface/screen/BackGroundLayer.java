package org.grandviewtech.userinterface.screen;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.grandviewtech.constants.PreferredDimension;
import org.grandviewtech.entity.bo.PlcFile;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.helper.RowGenerator;
import org.grandviewtech.userinterface.ui.Ruler;

public class BackGroundLayer extends JFrame implements PreferredDimension
	{
		private static final long	serialVersionUID	= 1098998552962951470L;
		
		private Screen				screen				= Screen.getInstance();
		
		private Container			container;
		
		private PlcFile				plcFile				= PlcFile.getInstance();
		
		public BackGroundLayer()
			{
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				setVisible(true);
				setPositioning();
				container = getContentPane();
				container.add(addRulerJScrolBar());
				postConstruct();
			}
			
		private void setPositioning()
			{
				Point point = new Point((Toolkit.getDefaultToolkit().getScreenSize().width - getSize().width) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - getSize().height) / 2);
				setLocation(point);
			}
			
		private Ruler addRulerJScrolBar()
			{
				Ruler jScrollPane = new Ruler(screen.getSheet(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				jScrollPane.initializeScrollPane();
				JScrollBar verticalScrollBar = jScrollPane.getVerticalScrollBar();
				verticalScrollBar.setUnitIncrement(15);
				InputMap im = verticalScrollBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				im.put(KeyStroke.getKeyStroke("DOWN"), "positiveUnitIncrement");
				im.put(KeyStroke.getKeyStroke("UP"), "negativeUnitIncrement");
				return jScrollPane;
			}
			
		private void postConstruct()
			{
				setHeader();
				setMinMaxDimension();
				plcFile.setFileName("temp_" + System.nanoTime());
			}
			
		private void setHeader()
			{
				setJMenuBar(CustomHeader.getJMenuBar());
				
				container.add(CustomHeader.getJToolBar(), BorderLayout.NORTH);
				
			}
			
		public void setMinMaxDimension()
			{
				setExtendedState(JFrame.MAXIMIZED_BOTH);
				setMinimumSize(MINIMUM_FRAME_SIZE);
			}
			
		public void init()
			{
				RowGenerator.regenerateRow();
			}
	}
