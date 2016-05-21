package org.grandviewtech.userinterface.screen;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.grandviewtech.constants.PreferredDimension;
import org.grandviewtech.userinterface.listeners.RungListener;

public class Rung extends JPanel implements PreferredDimension
	{
		
		private static final long	serialVersionUID	= -9169919756235566414L;
		
		private static Font			font				= new Font("SansSerif", Font.PLAIN, 9);
		
		private JLabel				label;
		
		boolean						focusGained			= false;
		
		int							rowNumber;
		
		public Rung(int rowNumber)
			{
				this.rowNumber = rowNumber;
				setPreferredSize(RUNG_SIZE);
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
				label.setText(addPaddingToRowNumber(this.rowNumber));
			}
			
	}
