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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;
import java.util.Map;

import javax.swing.JLabel;

import org.grandviewtech.constants.ApplicationConstant;
import org.grandviewtech.constants.CustomFont;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.entity.bo.Routine;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.entity.enums.Edge;
import org.grandviewtech.entity.enums.InputType;
import org.grandviewtech.entity.enums.LoadType;
import org.grandviewtech.entity.enums.NoNc;
import org.grandviewtech.entity.enums.Signal;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;

public class PaintCoilsOnScreen
	{/*
		final static Screen SCREEN = Screen.getInstance();
		
		public static void paintDragOption(ColumnScreen columnScreen, Graphics graphics)
			{
				CoilType coilType = columnScreen.getCoilType();
				coilType = (coilType == null) ? CoilType.DEFAULT : coilType;
				if (columnScreen.getCoilType() != null && (columnScreen.getCoilType().getCoilType().equals(CoilType.DEFAULT.getCoilType()) == false))
					{
						paint(columnScreen, graphics, columnScreen.getCoilType());
					}
				if (columnScreen.getChildType() != null && (columnScreen.getChildType().getCoilType().equals(CoilType.DEFAULT.getCoilType()) == false))
					{
						ColumnScreenGenerator.createColumnNeighbourHood(ClipBoard.SCREEN.getRow(columnScreen.getRowNumber()), columnScreen);
						paint(columnScreen, graphics, columnScreen.getChildType());
					}
			}
			
		private static void paintLoadCoil(ColumnScreen columnScreen, Graphics graphics)
			{
				int offset = 6;
				graphics.drawLine(0, ApplicationConstant.SECTION_HEIGHT / 2, (ApplicationConstant.SECTION_WIDTH / 2) - offset, ApplicationConstant.SECTION_HEIGHT / 2);
				graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) - offset, (ApplicationConstant.SECTION_HEIGHT / 2) - offset, (ApplicationConstant.SECTION_WIDTH / 2) - offset, (ApplicationConstant.SECTION_HEIGHT / 2) + offset);
				graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) + offset, (ApplicationConstant.SECTION_HEIGHT / 2) - offset, (ApplicationConstant.SECTION_WIDTH / 2) + offset, (ApplicationConstant.SECTION_HEIGHT / 2) + offset);
				graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) + offset, ApplicationConstant.SECTION_HEIGHT / 2, (ApplicationConstant.SECTION_WIDTH), ApplicationConstant.SECTION_HEIGHT / 2);
			}
			
		public static void paintSignalCoil(ColumnScreen component, Graphics graphics)
			{
				Signal signal = component.getSignal();
				
				int offset = 6;
				graphics.drawLine(0, ApplicationConstant.SECTION_HEIGHT / 2, (ApplicationConstant.SECTION_WIDTH / 2) - offset, ApplicationConstant.SECTION_HEIGHT / 2);
				graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) - offset, (ApplicationConstant.SECTION_HEIGHT / 2) - offset, (ApplicationConstant.SECTION_WIDTH / 2) - offset, (ApplicationConstant.SECTION_HEIGHT / 2) + offset);
				graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) + offset, (ApplicationConstant.SECTION_HEIGHT / 2) - offset, (ApplicationConstant.SECTION_WIDTH / 2) + offset, (ApplicationConstant.SECTION_HEIGHT / 2) + offset);
				graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) + offset, ApplicationConstant.SECTION_HEIGHT / 2, (ApplicationConstant.SECTION_WIDTH), ApplicationConstant.SECTION_HEIGHT / 2);
				if (signal != null)
					{
						int lengthOffset = 4;
						int heightOffset = offset;
						switch (signal)
							{
								case FALLING:
									{
										// Left Line
										graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) - lengthOffset, (ApplicationConstant.SECTION_HEIGHT / 2), (ApplicationConstant.SECTION_WIDTH / 2), (ApplicationConstant.SECTION_HEIGHT / 2) + (heightOffset));
										// Center Line
										graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2), (ApplicationConstant.SECTION_HEIGHT / 2) - heightOffset, (ApplicationConstant.SECTION_WIDTH / 2), (ApplicationConstant.SECTION_HEIGHT / 2) + heightOffset);
										// Right Line
										graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2), (ApplicationConstant.SECTION_HEIGHT / 2) + (heightOffset), (ApplicationConstant.SECTION_WIDTH / 2) + lengthOffset, (ApplicationConstant.SECTION_HEIGHT / 2));
										break;
									}
								case RISING:
									{
										// Left Line
										graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) - lengthOffset, (ApplicationConstant.SECTION_HEIGHT / 2), (ApplicationConstant.SECTION_WIDTH / 2), (ApplicationConstant.SECTION_HEIGHT / 2) - (heightOffset));
										// Center Line
										graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2), (ApplicationConstant.SECTION_HEIGHT / 2) - heightOffset, (ApplicationConstant.SECTION_WIDTH / 2), (ApplicationConstant.SECTION_HEIGHT / 2) + heightOffset);
										// Right Line
										graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2), (ApplicationConstant.SECTION_HEIGHT / 2) - (heightOffset), (ApplicationConstant.SECTION_WIDTH / 2) + lengthOffset, (ApplicationConstant.SECTION_HEIGHT / 2));
										break;
									}
								case NC:
									{
										break;
									}
								case NO:
									{
										graphics.drawLine((ApplicationConstant.SECTION_WIDTH / 2) - offset * 2, (ApplicationConstant.SECTION_HEIGHT / 2) + (offset * 2), (ApplicationConstant.SECTION_WIDTH / 2) + offset * 2, (ApplicationConstant.SECTION_HEIGHT / 2) - (offset * 2));
										break;
									}
								case NONE:
									{
										break;
									}
									
							}
					}
					
			}
			
		public static void paintRoutineCoil(ColumnScreen component, Graphics graphics)
			{
				int offset = 1;
				Routine routine = (component).getRoutine();
				graphics.drawLine(0, 0, ApplicationConstant.SECTION_WIDTH - offset, 0);
				graphics.drawLine(0, 0, 0, ApplicationConstant.SECTION_HEIGHT - offset);
				graphics.drawLine(ApplicationConstant.SECTION_WIDTH - offset, 0, ApplicationConstant.SECTION_WIDTH - offset, ApplicationConstant.SECTION_HEIGHT - offset);
				graphics.drawLine(0, ApplicationConstant.SECTION_HEIGHT - offset, (ApplicationConstant.SECTION_WIDTH - offset), ApplicationConstant.SECTION_HEIGHT - offset);
				int counter = 1;
				int height = 10;
				if (routine.getInputs() != null)
					{
						for (Map.Entry<Integer, String> input : routine.getInputs().entrySet())
							{
								String value = routine.getValues().get(input.getKey());
								JLabel label = new JLabel("I(" + input.getKey() + ") : " + value);
								label.setName(value);
								label.setBounds(5, ((counter % 2 == 0) ? height = height + 20 : height), 100, 50);
								component.add(label);
								routine.setComponent(label);
								counter = counter + 1;
							}
					}
					
			}
			
		private static void paintOutputCoil(Component component, Graphics graphics)
			{
				
				if (component instanceof ColumnScreen)
					{
						ColumnScreen columnScreen = (ColumnScreen) component;
						NoNc nonc = columnScreen.getNonc();
						InputType inputType = columnScreen.getInputType();
						String value = columnScreen.getValue();
						String valueLabel = columnScreen.getValueLabel().getText();
						int currentColumnNumber = columnScreen.getColumnNumber();
						String tag = columnScreen.getTag();
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
														tempScreen.reset();
														tempScreen.setCoilType(CoilType.LINE);
														tempScreen.apply();
													}
												else if (i$ == ApplicationConstant.MAX_CELL)
													{
														tempScreen.reset();
														tempScreen.setCoilType(CoilType.OUTPUT);
														tempScreen.setInputType(inputType);
														tempScreen.setValue(value);
														tempScreen.setNonc(nonc);
														tempScreen.getValueLabel().setText(valueLabel);
														tempScreen.setTag(tag);
														//tempScreen.apply();
													}
												//tempScreen.repaint();
											}
									}
							}
						else if (currentColumnNumber == ApplicationConstant.MAX_CELL)
							{
								paintOutPutCoil(columnScreen, graphics);
								//columnScreen.revalidate();
								columnScreen.apply();
							}
							
					}
					
			}
			
		private static void paintLineCoil(Graphics graphics)
			{
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, ApplicationConstant.SECTION_HEIGHT / 2, ApplicationConstant.SECTION_WIDTH, 1);
			}
			
		public static void paintDefault(ColumnScreen columnScreen, Graphics graphics)
			{
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.fillRect(0, ApplicationConstant.SECTION_HEIGHT / 2, ApplicationConstant.SECTION_WIDTH, 1);
			}
			
		private static void paintNamedCoil(Component component, Graphics graphics, String name)
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
				// graphics.fillRect((ApplicationConstant.SECTION_WIDTH / 2) +
				// 20, ApplicationConstant.SECTION_HEIGHT / 2,
				// ApplicationConstant.SECTION_WIDTH, 1);
				
				ColumnScreen columnScreen = (ColumnScreen) component;
				columnScreen.getValueLabel().setText(name.toUpperCase());
				columnScreen.getValueLabel().setBounds((ApplicationConstant.SECTION_WIDTH / 2) - 12, (ApplicationConstant.SECTION_HEIGHT / 2) - 5, 60, 10);
				columnScreen.getValueLabel().setFont(CustomFont.font1);
				
			}
			
		private static void paintOutPutCoil(Component component, Graphics graphics)
			{
				//o
				int offsetY = 8;
				int offsetX = 10;
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, ApplicationConstant.SECTION_HEIGHT / 2, (ApplicationConstant.SECTION_WIDTH / 2) - 10, 1);
				((Graphics2D) graphics).draw(new QuadCurve2D.Double((ApplicationConstant.SECTION_WIDTH / 2) - offsetX, (ApplicationConstant.SECTION_HEIGHT / 2) - offsetY, (ApplicationConstant.SECTION_WIDTH / 2) - 15, (ApplicationConstant.SECTION_HEIGHT / 2), (ApplicationConstant.SECTION_WIDTH / 2) - offsetX, (ApplicationConstant.SECTION_HEIGHT / 2) + offsetY));
				((Graphics2D) graphics).draw(new QuadCurve2D.Double((ApplicationConstant.SECTION_WIDTH / 2) + offsetX, (ApplicationConstant.SECTION_HEIGHT / 2) - offsetY, (ApplicationConstant.SECTION_WIDTH / 2) + 15, (ApplicationConstant.SECTION_HEIGHT / 2), (ApplicationConstant.SECTION_WIDTH / 2) + offsetX, (ApplicationConstant.SECTION_HEIGHT / 2) + offsetY));
				String name = "";
				ColumnScreen columnScreen = (ColumnScreen) component;
				if (columnScreen.getNonc() != null)
					{
						boolean addLabel = false;
						switch (columnScreen.getNonc())
							{
								case NC:
									{
										addLabel = true;
										name = "NC";
										break;
									}
								case NO:
									{
										name = "";
										break;
									}
								case DEFAULT:
									{
										name = "";
										break;
									}
								case SET:
									{
										addLabel = true;
										
										name = "S";
										break;
									}
								case RESET:
									{
										addLabel = true;
										name = "R";
										break;
									}
							}
						if (addLabel)
							{
								JLabel label = new JLabel(name);
								label.setFont(CustomFont.font10);
								label.setBounds((ApplicationConstant.SECTION_WIDTH / 2) - offsetX + 2, (ApplicationConstant.SECTION_HEIGHT / 2) - 5, 60, 10);
								columnScreen.add(label);
							}
					}
			}
			
		private static void paintLoadTypeCoil(boolean isParent, LoadType loadType, ColumnScreen child, Graphics graphics)
			{
				RowScreen rowScreen = SCREEN.getRow(child.getRowNumber() - 1);
				if (rowScreen != null)
					{
						ColumnScreen parent = (isParent == false) ? rowScreen.getColumnScreens(child.getColumnNumber()) : null;
						
						switch (loadType)
							{
								case LEFT_LINK:
									{
										if (parent != null)
											{
												parent.getGraphics().drawLine(0, ApplicationConstant.SECTION_HEIGHT / 2, 0, ApplicationConstant.SECTION_HEIGHT);
											}
										graphics.drawLine(0, 0, 0, ApplicationConstant.SECTION_HEIGHT / 2);
										
										 * if (parent != null) {
										 * child.setAbove(parent);
										 * parent.setParent(true);
										 * parent.setChildType(CoilType.
										 * LEFT_LINK); }
										 
										break;
									}
								case RIGHT_LINK:
									{
										if (parent != null)
											{
												parent.getGraphics().drawLine(ApplicationConstant.SECTION_WIDTH - 1, ApplicationConstant.SECTION_HEIGHT / 2, ApplicationConstant.SECTION_WIDTH - 1, ApplicationConstant.SECTION_HEIGHT);
											}
										graphics.drawLine(ApplicationConstant.SECTION_WIDTH - 1, 0, ApplicationConstant.SECTION_WIDTH - 1, ApplicationConstant.SECTION_HEIGHT / 2);
										
										 * if (parent != null) {
										 * child.setAbove(parent);
										 * parent.setParent(true);
										 * parent.setChildType(CoilType.
										 * RIGHT_LINK); parent.repaint(); }
										 
										break;
									}
									
							}
						// if (parent != null)
						// {
						// parent.revalidate();
						// parent.repaint();
						// }
						// child.revalidate();
						// child.repaint();
					}
			}
			
		private static void paint(ColumnScreen current, Graphics graphics, CoilType coilType)
			{
				switch (coilType)
					{
						case LINE:
							{
								
								paintLineCoil(graphics);
								while (current.hasNext())
									{
										ColumnScreen neighbor = current.getNext(false);
										if (neighbor.isBlank())
											{
												Graphics neighborGraphics = neighbor.getGraphics();
												paintLineCoil(neighborGraphics);
												neighbor.setCoilType(CoilType.LINE);
											}
										else
											{
												break;
											}
										ColumnScreenGenerator.createColumnNeighbourHood(ClipBoard.SCREEN.getRow(neighbor.getRowNumber()), neighbor);
										current = neighbor;
									}
								break;
							}
						case OUTPUT:
							{
								paintOutputCoil(current, graphics);
								break;
							}
						case LOAD:
							{
								paintLoadCoil(current, graphics);
								break;
							}
						case LABEL:
							{
								
								paintNamedCoil(current, graphics, coilType.getCoilType());
								break;
							}
						case JUMP:
							{
								paintNamedCoil(current, graphics, coilType.getCoilType());
								break;
							}
						case END:
							{
								paintNamedCoil(current, graphics, coilType.getCoilType());
								break;
							}
						case ROUTINE:
							{
								paintRoutineCoil(current, graphics);
								break;
							}
						case DEFAULT:
							{
								break;
							}
						case LEFT_LINK:
							{
								
								 * ColumnScreen parent =
								 * current.getAbove(false); if (parent != null)
								 * { parent.setChildType(CoilType.LEFT_LINK); }
								 
								current.setCoilType(coilType);
								paintLoadCoil(current, graphics);
								paintLoadTypeCoil(current.isParent(), LoadType.LEFT_LINK, current, graphics);
								break;
							}
						case PARALLEL:
							{
								
								 * ColumnScreen parent =
								 * current.getAbove(false); if (parent != null)
								 * { parent.setChildType(CoilType.PARALLEL); }
								 
								current.setCoilType(coilType);
								paintLoadCoil(current, graphics);
								paintLoadTypeCoil(current.isParent(), LoadType.LEFT_LINK, current, graphics);
								paintLoadTypeCoil(current.isParent(), LoadType.RIGHT_LINK, current, graphics);
								break;
							}
						case RIGHT_LINK:
							{
								
								 * ColumnScreen parent =
								 * current.getAbove(false); if (parent != null)
								 * { parent.setChildType(CoilType.RIGHT_LINK); }
								 
								current.setCoilType(coilType);
								paintLoadCoil(current, graphics);
								paintLoadTypeCoil(current.isParent(), LoadType.RIGHT_LINK, current, graphics);
								break;
							}
					}
					
			}
	*/}
