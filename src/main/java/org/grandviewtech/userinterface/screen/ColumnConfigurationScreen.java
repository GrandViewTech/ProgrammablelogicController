package org.grandviewtech.userinterface.screen;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.grandviewtech.constants.PreferredDimension;
import org.grandviewtech.service.searching.SearchEngine;

public class ColumnConfigurationScreen extends JFrame implements PreferredDimension
	{
		private static final long serialVersionUID = 1L;
		
		private ColumnConfigurationScreen()
			{
			}
			
		private ColumnScreen columnScreen;
		
		private static class ColumnConfigurationScreenFactoryInstance
			{
				final static ColumnConfigurationScreen instance = new ColumnConfigurationScreen();
			};
			
		public static ColumnConfigurationScreen getInstance()
			{
				return ColumnConfigurationScreenFactoryInstance.instance;
			}
			
		private JPanel				panel		= new JPanel();
		
		private JLabel				input		= new JLabel("Input");
		
		private JTextField			value		= new JTextField();
		
		private JLabel				tagLabel	= new JLabel("Tag");
		
		private JTextField			tagValue	= new JTextField();
		
		private JButton				submit		= new JButton("Submit");
		
		private JButton				cancel		= new JButton("Canel");
		
		private static final int	X1			= 10;
		private static final int	X2			= 120;
		private static final int	Y			= 30;
		private static final int	WIDTH		= 100;
		private static final int	HEIGHT		= 20;
		
		public void initiateInstance(ColumnScreen columnScreen)
			{
				setAlwaysOnTop(true);
				this.columnScreen = columnScreen;
				input.setBounds(X1, Y, WIDTH, HEIGHT);
				value.setBounds(X2, Y, WIDTH, HEIGHT);
				tagLabel.setBounds(X1, Y * 2, WIDTH, HEIGHT);
				tagValue.setBounds(X2, Y * 2, WIDTH, HEIGHT);
				submit.setBounds(X1, Y * 3, WIDTH, HEIGHT);
				submit.addActionListener(event ->
					{
						this.columnScreen.setValue(value.getText());
						this.columnScreen.getValueLabel().setText(this.columnScreen.getValue());
						String tag = (tagValue.getText() == null || tagValue.getText().trim().length() == 0) ? "" : tagValue.getText();
						this.columnScreen.setTag(tag);
						dispose();
						SearchEngine.index(columnScreen);
					});
				cancel.setBounds(X2, Y * 3, WIDTH, HEIGHT);
				cancel.addActionListener(event ->
					{
						dispose();
					});
				panel.setPreferredSize(CONFIGURATION_SCREEN);
				panel.setLayout(null);
				panel.add(input);
				panel.add(value);
				panel.add(tagLabel);
				panel.add(tagValue);
				panel.add(submit);
				panel.add(cancel);
				add(panel);
				setLocationRelativeTo(null);
				setPreferredSize(CONFIGURATION_SCREEN);
				pack();
				setVisible(true);
			}
			
	}
