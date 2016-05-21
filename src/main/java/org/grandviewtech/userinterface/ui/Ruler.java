package org.grandviewtech.userinterface.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.grandviewtech.constants.PLCConstant;

public class Ruler extends JScrollPane implements PLCConstant
	{
		private static final long	serialVersionUID	= 5438596997335696102L;
		
		private JLabel[]			corners				= new JLabel[4];
		
		public void configureCorners()
			{
				for (int i = 0; i < 4; i++)
					{
						corners[i] = new JLabel();
						corners[i].setBackground(Color.white);
						corners[i].setOpaque(true);
					}
			}
			
		public JLabel configureRowHeader()
			{
				JLabel rowheader = new JLabel("")
					{
						private static final long serialVersionUID = 2103953553735173735L;
						
						public void paintComponent(Graphics graphics)
							{
								super.paintComponent(graphics);
								Rectangle rect = graphics.getClipBounds();
								for (int i = 50 - (rect.y % 50); i < rect.height; i += 50)
									{
										graphics.drawLine(0, rect.y + i, 3, rect.y + i);
										graphics.drawString("" + (rect.y + i), 6, rect.y + i + 3);
									}
							}
							
						public Dimension getPreferredSize()
							{
								return new Dimension(SECTION_WIDTH, 10);
							}
							
					};
				//rowheader.setBackground(Color.white);
				rowheader.setOpaque(true);
				return rowheader;
			}
			
		public JLabel configureColumnHeader()
			{
				JLabel columnheader = new JLabel()
					{
						private static final long serialVersionUID = -2103953553735173735L;
						
						public void paintComponent(Graphics graphics)
							{
								super.paintComponent(graphics);
								for (int i = 0; i <= MAX_CELL; i++)
									{
										int x;
										if (i == 0)
											{
												x = SECTION_WIDTH * i + SECTION_WIDTH / 2 + 30;
											}
										else
											{
												x = SECTION_WIDTH * i + SECTION_WIDTH / 2 + 20;
											}
											
										graphics.drawLine(x, 0, x, 3);
										graphics.drawString("" + (i + 1), x - 5, 16);
									}
							}
							
						public Dimension getPreferredSize()
							{
								return new Dimension(SECTION_WIDTH, 20);
							}
					};
				columnheader.setBackground(Color.white);
				columnheader.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
				columnheader.setOpaque(true);
				return columnheader;
			}
			
		public void initializeScrollPane()
			{
				configureCorners();
				//setRowHeaderView(configureRowHeader());
				setColumnHeaderView(configureColumnHeader());
				setCorner(JScrollPane.LOWER_LEFT_CORNER, corners[0]);
				setCorner(JScrollPane.LOWER_RIGHT_CORNER, corners[1]);
				setCorner(JScrollPane.UPPER_LEFT_CORNER, corners[2]);
				setCorner(JScrollPane.UPPER_RIGHT_CORNER, corners[3]);
				
			}
			
		public Ruler()
			{
				super();
			}
			
		public Ruler(Component view, int vsbPolicy, int hsbPolicy)
			{
				super(view, vsbPolicy, hsbPolicy);
			}
			
		public Ruler(Component view)
			{
				super(view);
			}
			
		public Ruler(int vsbPolicy, int hsbPolicy)
			{
				super(vsbPolicy, hsbPolicy);
			}
			
	}