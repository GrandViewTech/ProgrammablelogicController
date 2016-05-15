package org.grandviewtech.userinterface.ui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.Border;

public class ToolBarLabel extends JLabel
	{
		private static final long	serialVersionUID	= 1L;
		
		Border						paddingBorder		= BorderFactory.createEmptyBorder(2, 5, 5, 2);
		Border						border				= BorderFactory.createLineBorder(Color.white);
		
		public ToolBarLabel(Icon icon)
			{
				super();
				setBorder(BorderFactory.createCompoundBorder(border, paddingBorder));
				setIcon(icon);
			}
	}
