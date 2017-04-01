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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.grandviewtech.runner.Application;

public class AdvancedSearchScreen extends JFrame
	{
		private JPanel				panel				= new JPanel();
		private JLabel				valueLabel			= new JLabel("1. Value");
		private JTextField			value				= new JTextField("");
		private JLabel				tagLabel			= new JLabel("2. Tag");
		private JTextField			tag					= new JTextField("");
		private JLabel				type				= new JLabel("3. Type");
		private JRadioButton		input				= new JRadioButton("input");
		private JRadioButton		flag				= new JRadioButton("flag");
		private JRadioButton		word				= new JRadioButton("word");
		private JRadioButton		output				= new JRadioButton("output");
		private JButton				go					= new JButton("Go");
		private JButton				cancel				= new JButton("Cancel");
		private JLabel				text				= new JLabel("No Text Found");
		private JButton				next				= new JButton("Next");
		private JButton				previous			= new JButton("Previous");
		
		private static final long	serialVersionUID	= 1L;
		
		public AdvancedSearchScreen()
			{
				panel.setPreferredSize(new Dimension(400, 130));
				panel.setLayout(null);
				ButtonGroup buttonGroup = new ButtonGroup();
				buttonGroup.add(input);
				buttonGroup.add(flag);
				buttonGroup.add(word);
				buttonGroup.add(output);
				valueLabel.setBounds(20, 10, 60, 20);
				value.setBounds(80, 10, 50, 20);
				tagLabel.setBounds(180, 10, 60, 20);
				tag.setBounds(240, 10, 50, 20);
				int radioButtonSize = 80;
				type.setBounds(20, 40, radioButtonSize, 20);
				input.setBounds(80, 40, radioButtonSize, 20);
				flag.setBounds(140, 40, radioButtonSize, 20);
				word.setBounds(200, 40, radioButtonSize, 20);
				output.setBounds(260, 40, radioButtonSize, 20);
				go.setBounds(50, 70, radioButtonSize, 20);
				cancel.setBounds(240, 70, radioButtonSize, 20);
				text.setBounds(130, 100, 200, 20);
				next.setBounds(20, 100, radioButtonSize, 20);
				previous.setBounds(260, 100, radioButtonSize, 20);
				panel.add(valueLabel);
				panel.add(value);
				panel.add(tagLabel);
				panel.add(tag);
				panel.add(type);
				panel.add(input);
				panel.add(flag);
				panel.add(word);
				panel.add(output);
				panel.add(go);
				panel.add(cancel);
				JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
				separator.setBounds(10, 90, 380, 10);
				add(separator, BorderLayout.LINE_START);
				panel.add(text);
				panel.add(next);
				panel.add(previous);
				setVisible(true);
				add(panel);
				//invoke();
				addListeners();
			}
			
		public void reset()
			{
				
			}
			
		public void invoke()
			{
				setVisible(true);
				setAlwaysOnTop(true);
				org.grandviewtech.entity.helper.Dimension dimension = Application.calculateCenterAlignment(getPreferredSize());
				setLocation(dimension.getX(), dimension.getY());
				pack();
				repaint();
			}
			
		private void addListeners()
			{
				addCancelListener();
			}
			
		private void addCancelListener()
			{
				cancel.addActionListener(event -> dispose());
			}
			
	}
