package org.grandviewtech.userinterface.ui;

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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.grandviewtech.constants.ApplicationConstant;

public class Ruler extends JScrollPane
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
						
						@Override
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
							
						@Override
						public Dimension getPreferredSize()
							{
								return new Dimension(ApplicationConstant.SECTION_WIDTH, 10);
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
						
						@Override
						public void paintComponent(Graphics graphics)
							{
								super.paintComponent(graphics);
								for (int i = 0; i <= ApplicationConstant.MAX_CELL; i++)
									{
										int x;
										if (i == 0)
											{
												x = ApplicationConstant.SECTION_WIDTH * i + ApplicationConstant.SECTION_WIDTH / 2 + 30;
											}
										else
											{
												x = ApplicationConstant.SECTION_WIDTH * i + ApplicationConstant.SECTION_WIDTH / 2 + 20;
											}
											
										graphics.drawLine(x, 0, x, 3);
										graphics.drawString("" + (i + 1), x - 5, 16);
									}
							}
							
						@Override
						public Dimension getPreferredSize()
							{
								return new Dimension(ApplicationConstant.SECTION_WIDTH, 20);
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
