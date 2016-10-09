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

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.grandviewtech.constants.CustomDimension;
import org.grandviewtech.userinterface.listeners.RungListener;

public class Rung extends JPanel implements Comparable<Rung>
	{
		
		private static final long	serialVersionUID	= -9169919756235566414L;
		
		private static Font			font				= new Font("SansSerif", Font.PLAIN, 9);
		
		private JLabel				label;
		
		boolean						focusGained			= false;
		
		int							rowNumber;
		
		private String				comment;
		
		public Rung(int rowNumber)
			{
				this.rowNumber = rowNumber;
				setPreferredSize(CustomDimension.RUNG_SIZE);
				setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
				RungListener rungListener = new RungListener(this);
				addMouseListener(rungListener);
				addKeyListener(rungListener);
				addComponent(this);
				setVisible(true);
			}
			
		public void addComponent(JPanel jPanel)
			{
				
				label = new JLabel(addPaddingToRowNumber(rowNumber), SwingConstants.CENTER);
				label.setBounds(5, 120, 10, 05);
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setFont(font);
				label.setUI(new VerticalLabel(false));
				add(label);
			}
			
		private String addPaddingToRowNumber(int rowNumber)
			{
				String param = "" + rowNumber;
				int padding = 8 - param.length();
				for (int i = 0; i < padding; i++)
					{
						param += " ";
					}
				return param;
			}
			
		public int getRowNumber()
			{
				return rowNumber;
			}
			
		public void setRowNumber(int rowNumber)
			{
				this.rowNumber = rowNumber;
				label.setText(addPaddingToRowNumber(rowNumber));
				this.setToolTipText("Rung : " + rowNumber);
			}
			
		@Override
		public int compareTo(Rung comparableRung)
			{
				Integer currentRowNumber = rowNumber;
				Integer comparableRowNumber = comparableRung.getRowNumber();
				return currentRowNumber.compareTo(comparableRowNumber);
			}
			
		@Override
		public int hashCode()
			{
				return rowNumber;
			}
			
		@Override
		public boolean equals(Object comparableRung)
			{
				if (comparableRung instanceof Rung)
					{
						int currentRowNumber = rowNumber;
						int comparableRowNumber = ((Rung) comparableRung).getRowNumber();
						if (currentRowNumber == comparableRowNumber)
							{
								return true;
							}
						return false;
					}
				return false;
			}
			
		public String getComment()
			{
				return comment;
			}
			
		public void setComment(String comment)
			{
				this.comment = comment;
			}
			
	}
