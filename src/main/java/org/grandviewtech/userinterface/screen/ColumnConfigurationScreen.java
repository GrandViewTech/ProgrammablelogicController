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
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.grandviewtech.constants.PreferredDimension;
import org.grandviewtech.entity.helper.Dimension;
import org.grandviewtech.runner.Application;
import org.grandviewtech.service.searching.SearchEngine;
import org.grandviewtech.service.system.Printer;

public class ColumnConfigurationScreen extends JFrame implements PreferredDimension
	{
		private static org.apache.log4j.Logger	logger				= org.apache.log4j.Logger.getLogger(ColumnConfigurationScreen.class);
		
		private static final long				serialVersionUID	= 1L;
		
		public ColumnConfigurationScreen()
			{
				reset();
				scrollableConfigurationScreen = new JScrollPane(panel);
				scrollableConfigurationScreen.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrollableConfigurationScreen.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			}
			
		private ColumnScreen columnScreen;
		
		private void reset()
			{
				value.setText("");
				tagValue.setText("");
			}
			
		private ButtonGroup			buttonGroup						= new ButtonGroup();
		
		private JRadioButton		input							= new JRadioButton("Input", true);
		
		private JRadioButton		flag							= new JRadioButton("Flag");
		
		private JRadioButton		word							= new JRadioButton("Word");
		
		private JRadioButton		output							= new JRadioButton("Output");
		
		private ButtonGroup			edgeButtonGroup					= new ButtonGroup();
		
		private JRadioButton		risingEdge						= new JRadioButton("Rising", true);
		
		private JRadioButton		fallingEdge						= new JRadioButton("Falling");
		
		private ButtonGroup			nc_noButtonGroup				= new ButtonGroup();
		
		private JRadioButton		NC								= new JRadioButton("NC", true);
		
		private JRadioButton		NO								= new JRadioButton("NO");
		
		private JPanel				panel							= new JPanel();
		
		private JLabel				edgeLabel						= new JLabel("Edge");
		
		private JLabel				ncnoLabel						= new JLabel("NC/NO");
		
		private JLabel				inputValue						= new JLabel("Input");
		
		private JTextField			value							= new JTextField();
		
		private JLabel				type							= new JLabel("Type");
		
		private JLabel				tagLabel						= new JLabel("Tag");
		
		private JTextField			tagValue						= new JTextField();
		
		private JButton				submit							= new JButton("Submit");
		
		private JButton				cancel							= new JButton("Canel");
		
		private JScrollPane			scrollableConfigurationScreen	= null;
		
		private static final int	X1								= 10;
		private static final int	X2								= 150;
		private static final int	Y								= 30;
		private static final int	WIDTH							= 120;
		private static final int	RADIO_WIDTH						= 80;
		private static final int	HEIGHT							= 20;
		
		public void setValues()
			{
				
			}
			
		public void initiateInstance(ColumnScreen columnScreen)
			{
				try
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
						JSeparator jvs1 = new JSeparator(SwingConstants.HORIZONTAL);
						jvs1.setBounds(X1, Y * 1 + (20), CONFIGURATION_SCREEN.width - (X1 * 4), 10);
						//
						type.setBounds(X1, Y * 2, RADIO_WIDTH, HEIGHT);
						input.setBounds(X2, Y * 2, RADIO_WIDTH, HEIGHT);
						JSeparator jvs2 = new JSeparator(SwingConstants.HORIZONTAL);
						jvs2.setBounds(X1, Y * 2 + (20), CONFIGURATION_SCREEN.width - (X1 * 4), 10);
						flag.setBounds(X2 + (RADIO_WIDTH * 1), Y * 2, RADIO_WIDTH, HEIGHT);
						word.setBounds(X2, Y * 3, RADIO_WIDTH, HEIGHT);
						output.setBounds(X2 + (RADIO_WIDTH * 1), Y * 3, RADIO_WIDTH, HEIGHT);
						JSeparator jvs3 = new JSeparator(SwingConstants.HORIZONTAL);
						jvs3.setBounds(X1, Y * 3 + (20), CONFIGURATION_SCREEN.width - (X1 * 4), 10);
						// Edge
						edgeButtonGroup.add(risingEdge);
						edgeButtonGroup.add(fallingEdge);
						risingEdge.setMnemonic(KeyEvent.VK_R);
						fallingEdge.setMnemonic(KeyEvent.VK_G);
						edgeLabel.setBounds(X1, Y * 4, RADIO_WIDTH, HEIGHT);
						risingEdge.setBounds(X2, Y * 4, RADIO_WIDTH, HEIGHT);
						fallingEdge.setBounds(X2 + (RADIO_WIDTH * 1), Y * 4, RADIO_WIDTH, HEIGHT);
						JSeparator jvs4 = new JSeparator(SwingConstants.HORIZONTAL);
						jvs4.setBounds(X1, Y * 4 + (20), CONFIGURATION_SCREEN.width - (X1 * 4), 10);
						// NO/NC
						nc_noButtonGroup.add(NC);
						nc_noButtonGroup.add(NO);
						NO.setMnemonic(KeyEvent.VK_N);
						NC.setMnemonic(KeyEvent.VK_C);
						ncnoLabel.setBounds(X1, Y * 5, RADIO_WIDTH, HEIGHT);
						NC.setBounds(X2, Y * 5, RADIO_WIDTH, HEIGHT);
						NO.setBounds(X2 + (RADIO_WIDTH * 1), Y * 5, RADIO_WIDTH, HEIGHT);
						JSeparator jvs5 = new JSeparator(SwingConstants.HORIZONTAL);
						jvs5.setBounds(X1, Y * 5 + (20), CONFIGURATION_SCREEN.width - (X1 * 4), 10);
						tagLabel.setBounds(X1, Y * 6, WIDTH, HEIGHT);
						tagLabel.setEnabled(true);
						tagLabel.setToolTipText("Add A Tag for Current Coil");
						JSeparator jvs6 = new JSeparator(SwingConstants.HORIZONTAL);
						jvs6.setBounds(X1, Y * 6 + (20), CONFIGURATION_SCREEN.width - (X1 * 4), 10);
						tagValue.setBounds(X2, Y * 6, WIDTH, HEIGHT);
						submit.setBounds(X1, Y * 7 + 20, WIDTH, HEIGHT);
						submit.addActionListener(event ->
							{
								String optionType = "";
								if (input.isSelected())
									{
										optionType = "I/";
									}
								else if (flag.isSelected())
									{
										optionType = "F/";
									}
								else if (word.isSelected())
									{
										optionType = "D/";
									}
								else if (output.isSelected())
									{
										optionType = "O/";
									}
								this.columnScreen.setValue(value.getText());
								String valueLabel = optionType + this.columnScreen.getValue();
								Printer.print("valueLabel " + valueLabel);
								this.columnScreen.getValueLabel().setText(valueLabel);
								String tag = (tagValue.getText() == null || tagValue.getText().trim().length() == 0) ? "" : tagValue.getText();
								this.columnScreen.setTag(tag);
								dispose();
								SearchEngine.index(columnScreen);
							});
						cancel.setBounds(X2, Y * 7 + 20, WIDTH, HEIGHT);
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
						panel.add(jvs1);
						panel.add(jvs2);
						panel.add(jvs3);
						panel.add(jvs4);
						panel.add(jvs5);
						panel.add(jvs6);
						panel.add(edgeLabel);
						panel.add(risingEdge);
						panel.add(fallingEdge);
						panel.add(ncnoLabel);
						panel.add(NC);
						panel.add(NO);
						panel.add(tagLabel);
						panel.add(tagValue);
						panel.add(submit);
						panel.add(cancel);
						getContentPane().add(scrollableConfigurationScreen);
						Dimension dimension = Application.calculateCenterAlignment(getPreferredSize());
						//logger.info("setting location relative to  X: " + dimension.getX() + " | Y :" + dimension.getY());
						setLocation(dimension.getX(), dimension.getY());
						setPreferredSize(CONFIGURATION_SCREEN);
						pack();
						setVisible(true);
					}
				catch (Exception exception)
					{
						logger.error(exception.getLocalizedMessage(), exception);
					}
			}
			
	}
