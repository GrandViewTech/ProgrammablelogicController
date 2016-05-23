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

import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.grandviewtech.constants.PreferredDimension;
import org.grandviewtech.service.searching.SearchEngine;

public class ColumnConfigurationScreen extends JFrame implements PreferredDimension
	{
		private static final long serialVersionUID = 1L;
		
		public ColumnConfigurationScreen()
			{
				reset();
			}
			
		private ColumnScreen columnScreen;
		
		private void reset()
			{
				value.setText("");
				tagValue.setText("");
			}
			
		private ButtonGroup			buttonGroup	= new ButtonGroup();
		
		private JRadioButton		input		= new JRadioButton("Input", true);
		
		private JRadioButton		flag		= new JRadioButton("Flag");
		
		private JRadioButton		word		= new JRadioButton("Word");
		
		private JRadioButton		output		= new JRadioButton("Output");
		
		private JPanel				panel		= new JPanel();
		
		private JLabel				inputValue	= new JLabel("Input");
		
		private JTextField			value		= new JTextField();
		
		private JLabel				type		= new JLabel("Type");
		
		private JLabel				tagLabel	= new JLabel("Tag");
		
		private JTextField			tagValue	= new JTextField();
		
		private JButton				submit		= new JButton("Submit");
		
		private JButton				cancel		= new JButton("Canel");
		
		private static final int	X1			= 10;
		private static final int	X2			= 150;
		private static final int	Y			= 30;
		private static final int	WIDTH		= 120;
		private static final int	RADIO_WIDTH	= 80;
		private static final int	HEIGHT		= 20;
		
		public void setValues()
			{
				
			}
			
		public void initiateInstance(ColumnScreen columnScreen)
			{
				setTitle("Configure Coil | Row : " + columnScreen.getRowNumber() + " Column Number : " + columnScreen.getColumnNumber());
				input.setMnemonic(KeyEvent.VK_I);
				flag.setMnemonic(KeyEvent.VK_F);
				word.setMnemonic(KeyEvent.VK_W);
				output.setMnemonic(KeyEvent.VK_O);
				
				buttonGroup.add(input);
				buttonGroup.add(flag);
				buttonGroup.add(word);
				buttonGroup.add(output);
				
				setAlwaysOnTop(true);
				this.columnScreen = columnScreen;
				inputValue.setBounds(X1, Y - 5, WIDTH, HEIGHT);
				value.setBounds(X2, Y - 5, WIDTH, HEIGHT);
				//
				type.setBounds(X1, Y * 2, RADIO_WIDTH, HEIGHT);
				input.setBounds(X2, Y * 2, RADIO_WIDTH, HEIGHT);
				flag.setBounds(X2 + (RADIO_WIDTH * 1) , Y * 2, RADIO_WIDTH, HEIGHT);
				word.setBounds(X2, Y * 3, RADIO_WIDTH, HEIGHT);
				output.setBounds(X2 + (RADIO_WIDTH * 1) , Y * 3, RADIO_WIDTH, HEIGHT);
				//
				tagLabel.setBounds(X1, Y * 4, WIDTH, HEIGHT);
				tagLabel.setEnabled(true);
				tagLabel.setToolTipText("Add A Tag for Current Coil");
				tagValue.setBounds(X2, Y * 4, WIDTH, HEIGHT);
				submit.setBounds(X1, Y * 5, WIDTH, HEIGHT);
				submit.addActionListener(event ->
					{
						this.columnScreen.setValue(value.getText());
						this.columnScreen.getValueLabel().setText(this.columnScreen.getValue());
						String tag = (tagValue.getText() == null || tagValue.getText().trim().length() == 0) ? "" : tagValue.getText();
						this.columnScreen.setTag(tag);
						dispose();
						SearchEngine.index(columnScreen);
					});
				cancel.setBounds(X2, Y * 5, WIDTH, HEIGHT);
				cancel.addActionListener(event ->
					{
						dispose();
					});
				panel.setPreferredSize(CONFIGURATION_SCREEN);
				panel.setLayout(null);
				panel.add(inputValue);
				panel.add(value);
				panel.add(type);
				panel.add(input);
				panel.add(flag);
				panel.add(word);
				panel.add(output);
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
