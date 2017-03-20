package org.grandviewtech.userinterface.screen;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.grandviewtech.constants.CustomDimension;
import org.grandviewtech.entity.helper.Dimension;
import org.grandviewtech.runner.Application;

public class PreferenceScreen extends JFrame
	{
		
		private static final long	serialVersionUID	= -8037817505353170621L;
		final private static Logger	LOGGER				= Logger.getLogger(PreferenceScreen.class);
		final JPanel				panel				= new JPanel();
		final JLabel				input				= new JLabel("Input : Min ");
		
		public void init()
			{
				LOGGER.info("INITIALIZING FRAME");
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				setTitle("Preference");
				panel.setPreferredSize(new java.awt.Dimension(400, 200));
				panel.setLayout(null);
				addInputPreference();
				invokeFrame();
			}
			
		public void addInputPreference()
			{
				input.setBounds(10, 10, 150, 25);
				add(input);
			}
			
		private void invokeFrame()
			{
				setLayout(null);
//				add(panel);
				Dimension dimension = Application.calculateCenterAlignment(getPreferredSize());
				//setUndecorated(true);
				setLocation(dimension.getX(), dimension.getY());
				java.awt.Dimension frameDimension = CustomDimension.PREFERENCE_SCREEN;
				setPreferredSize(frameDimension);
				//AWTUtilities.setWindowShape(this, new RoundRectangle2D.Double(dimension.getX(), dimension.getY(), frameDimension.getWidth(), frameDimension.getHeight(), 50, 50));
				pack();
				setVisible(true);
			}
	}
