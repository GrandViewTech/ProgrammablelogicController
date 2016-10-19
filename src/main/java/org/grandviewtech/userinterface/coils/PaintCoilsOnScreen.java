package org.grandviewtech.userinterface.coils;

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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;

import org.grandviewtech.constants.ApplicationConstant;
import org.grandviewtech.constants.CustomFont;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.entity.enums.NoNc;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;

public class PaintCoilsOnScreen
	{
		final static Screen SCREEN = Screen.getInstance();
		
		public static void paint(ColumnScreen columnScreen, Graphics graphics)
			{
				CoilType coilType = columnScreen.getCoilType();
				boolean paintDefault = columnScreen.isPaintDefault();
				if (paintDefault)
					{
						paintDefault(graphics);
					}
				else
					{
						selectRigthDragOption(columnScreen, graphics, coilType);
					}
			}
			
		private static void selectRigthDragOption(ColumnScreen columnScreen, Graphics graphics, CoilType coilType)
			{
				switch (coilType)
					{
						case LINE:
							{
								configureLineCoil(graphics);
								break;
							}
						case OUTPUT:
							{
								configureOutputCoil(columnScreen, graphics);
								// configureNamedCoil(columnScreen, graphics);
								break;
							}
						case LOAD:
							{
								configureLoadCoil(columnScreen, graphics);
								break;
							}
						case JUMP:
							{
								// configureJumpCoil(columnScreen, graphics);
								configureNamedCoil(columnScreen, graphics, coilType.getCoilType());
								break;
							}
						case END:
							{
								configureNamedCoil(columnScreen, graphics, coilType.getCoilType());
								break;
							}
						case ROUTINE:
							{
								break;
							}
					}
			}
			
		/*		private static void configureJumpCoil(ColumnScreen columnScreen, Graphics graphics)
					{
						int offsetY = 8;
						graphics.setColor(Color.BLACK);
						graphics.fillRect(0, ApplicationConstant.SECTION_HEIGHT / 2, (ApplicationConstant.SECTION_WIDTH / 2) - 20, 1);
						((Graphics2D) graphics).draw(new QuadCurve2D.Double((ApplicationConstant.SECTION_WIDTH / 2) - 15, (ApplicationConstant.SECTION_HEIGHT / 2) - offsetY, (ApplicationConstant.SECTION_WIDTH / 2) - 25, (ApplicationConstant.SECTION_HEIGHT / 2), (ApplicationConstant.SECTION_WIDTH / 2) - 15, (ApplicationConstant.SECTION_HEIGHT / 2) + offsetY));
						((Graphics2D) graphics).draw(new QuadCurve2D.Double((ApplicationConstant.SECTION_WIDTH / 2) + 15, (ApplicationConstant.SECTION_HEIGHT / 2) - offsetY, (ApplicationConstant.SECTION_WIDTH / 2) + 25, (ApplicationConstant.SECTION_HEIGHT / 2), (ApplicationConstant.SECTION_WIDTH / 2) + 15, (ApplicationConstant.SECTION_HEIGHT / 2) + offsetY));
						graphics.fillRect((ApplicationConstant.SECTION_WIDTH / 2) + 20, ApplicationConstant.SECTION_HEIGHT / 2, ApplicationConstant.SECTION_WIDTH, 1);
						columnScreen.getValueLabel().setText("JUMP");
						columnScreen.getValueLabel().setBounds((ApplicationConstant.SECTION_WIDTH / 2) - 12, (ApplicationConstant.SECTION_HEIGHT / 2) - 5, 60, 10);
						columnScreen.getValueLabel().setFont(CustomFont.font1);
					}
		*/
		
		private static void configureLoadCoil(ColumnScreen columnScreen, Graphics graphics)
			{
				int offset = 5;
				graphics.drawLine(0, ApplicationConstant.SECTION_HEIGHT / 2, (ApplicationConstant.SECTION_WIDTH / 2) - offset, ApplicationConstant.SECTION_HEIGHT / 2);
				graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) - offset, (ApplicationConstant.SECTION_HEIGHT / 2) - offset, (ApplicationConstant.SECTION_WIDTH / 2) - offset, (ApplicationConstant.SECTION_HEIGHT / 2) + offset);
				graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) + offset, (ApplicationConstant.SECTION_HEIGHT / 2) - offset, (ApplicationConstant.SECTION_WIDTH / 2) + offset, (ApplicationConstant.SECTION_HEIGHT / 2) + offset);
				graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) + offset, ApplicationConstant.SECTION_HEIGHT / 2, (ApplicationConstant.SECTION_WIDTH), ApplicationConstant.SECTION_HEIGHT / 2);
				if (columnScreen.getNonc().getType().equalsIgnoreCase(NoNc.NC.getType()))
					{
						graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) - offset * 2, (ApplicationConstant.SECTION_HEIGHT / 2) + (offset * 2), (ApplicationConstant.SECTION_WIDTH / 2) + offset * 2, (ApplicationConstant.SECTION_HEIGHT / 2) - (offset * 2));
					}
			}
			
		private static void configureOutputCoil(ColumnScreen columnScreen, Graphics graphics)
			{
				int currentColumnNumber = columnScreen.getColumnNumber();
				if (currentColumnNumber < ApplicationConstant.MAX_CELL)
					{
						int currentRowNumber = columnScreen.getRowNumber();
						for (int i$ = currentColumnNumber; i$ <= ApplicationConstant.MAX_CELL; i$++)
							{
								RowScreen rowScreen = SCREEN.getRow(currentRowNumber);
								if (rowScreen != null)
									{
										ColumnScreen tempScreen = rowScreen.getColumnScreens(i$);
										if (i$ < ApplicationConstant.MAX_CELL)
											{
												tempScreen.setCoilType(CoilType.LINE);
											}
										else if (i$ == ApplicationConstant.MAX_CELL)
											{
												tempScreen.setCoilType(CoilType.OUTPUT);
											}
										tempScreen.repaint();
									}
							}
					}
				else
					{
						paintOutPutCoil(columnScreen, graphics);
					}
					
			}
			
		private static void configureLineCoil(Graphics graphics)
			{
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, ApplicationConstant.SECTION_HEIGHT / 2, ApplicationConstant.SECTION_WIDTH, 1);
			}
			
		private static void paintDefault(Graphics graphics)
			{
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.fillRect(0, ApplicationConstant.SECTION_HEIGHT / 2, ApplicationConstant.SECTION_WIDTH, 1);
			}
			
		private static void configureNamedCoil(ColumnScreen columnScreen, Graphics graphics, String name)
			{
				if (name == null || name.trim().length() == 0)
					{
						return;
					}
				int offsetY = 8;
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, ApplicationConstant.SECTION_HEIGHT / 2, (ApplicationConstant.SECTION_WIDTH / 2) - 20, 1);
				((Graphics2D) graphics).draw(new QuadCurve2D.Double((ApplicationConstant.SECTION_WIDTH / 2) - 15, (ApplicationConstant.SECTION_HEIGHT / 2) - offsetY, (ApplicationConstant.SECTION_WIDTH / 2) - 25, (ApplicationConstant.SECTION_HEIGHT / 2), (ApplicationConstant.SECTION_WIDTH / 2) - 15, (ApplicationConstant.SECTION_HEIGHT / 2) + offsetY));
				((Graphics2D) graphics).draw(new QuadCurve2D.Double((ApplicationConstant.SECTION_WIDTH / 2) + 15, (ApplicationConstant.SECTION_HEIGHT / 2) - offsetY, (ApplicationConstant.SECTION_WIDTH / 2) + 25, (ApplicationConstant.SECTION_HEIGHT / 2), (ApplicationConstant.SECTION_WIDTH / 2) + 15, (ApplicationConstant.SECTION_HEIGHT / 2) + offsetY));
				graphics.fillRect((ApplicationConstant.SECTION_WIDTH / 2) + 20, ApplicationConstant.SECTION_HEIGHT / 2, ApplicationConstant.SECTION_WIDTH, 1);
				columnScreen.getValueLabel().setText(name.toUpperCase());
				columnScreen.getValueLabel().setBounds((ApplicationConstant.SECTION_WIDTH / 2) - 12, (ApplicationConstant.SECTION_HEIGHT / 2) - 5, 60, 10);
				columnScreen.getValueLabel().setFont(CustomFont.font1);
			}
			
		private static void paintOutPutCoil(ColumnScreen columnScreen, Graphics graphics)
			{
				int offsetY = 8;
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, ApplicationConstant.SECTION_HEIGHT / 2, (ApplicationConstant.SECTION_WIDTH / 2) - 10, 1);
				((Graphics2D) graphics).draw(new QuadCurve2D.Double((ApplicationConstant.SECTION_WIDTH / 2) - 5, (ApplicationConstant.SECTION_HEIGHT / 2) - offsetY, (ApplicationConstant.SECTION_WIDTH / 2) - 15, (ApplicationConstant.SECTION_HEIGHT / 2), (ApplicationConstant.SECTION_WIDTH / 2) - 5, (ApplicationConstant.SECTION_HEIGHT / 2) + offsetY));
				((Graphics2D) graphics).draw(new QuadCurve2D.Double((ApplicationConstant.SECTION_WIDTH / 2) + 5, (ApplicationConstant.SECTION_HEIGHT / 2) - offsetY, (ApplicationConstant.SECTION_WIDTH / 2) + 15, (ApplicationConstant.SECTION_HEIGHT / 2), (ApplicationConstant.SECTION_WIDTH / 2) + 5, (ApplicationConstant.SECTION_HEIGHT / 2) + offsetY));
				
				// graphics.fillRect((ApplicationConstant.SECTION_WIDTH / 2) + 10, ApplicationConstant.SECTION_HEIGHT /
				// 2, SECTION_WIDTH, 1);
			}
	}
