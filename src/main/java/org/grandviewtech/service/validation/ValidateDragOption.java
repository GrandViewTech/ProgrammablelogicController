package org.grandviewtech.service.validation;

import org.grandviewtech.constants.ApplicationConstant;

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

import org.grandviewtech.entity.bo.Response;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;

public class ValidateDragOption
	{
		private static Screen SCREEN = Screen.getInstance();
		
		public static Response validateDragOption(ColumnScreen columnScreen, CoilType dragOption)
			{
				Response response = new Response();
				response = validateEndCase(response, dragOption, columnScreen);
				if (!response.isError())
					{
						switch (dragOption)
							{
								case LINE:
									{
										response = validateLine(response, columnScreen);
										break;
									}
								case OUTPUT:
									{
										response = validateOutputDrag(response, CoilType.OUTPUT, columnScreen);
										break;
									}
								case LOAD:
									{
										response = validateLoadtDrag(response, CoilType.LOAD, columnScreen);
										break;
									}
								case JUMP:
									{
										
										response = validateCoil(response, CoilType.JUMP, columnScreen);
										break;
									}
								case END:
									{
										break;
									}
								case ROUTINE:
									{
										break;
									}
								case DEFAULT:
									{
										break;
									}
								case LEFT_LINK:
									{
										response = validateLeftLink(response, columnScreen);
										break;
									}
								case PARALLEL:
									{
										response = validateParallelLink(response, columnScreen);
										break;
									}
								case RIGHT_LINK:
									{
										response = validateRightLink(response, columnScreen);
										break;
									}
							}
					}
				return response;
			}
			
		private static Response validateLoadtDrag(Response response, CoilType dragOption, ColumnScreen columnScreen)
			{
				return validateCoil(response, dragOption, columnScreen);
			}
			
		private static Response validateOutputDrag(Response response, CoilType dragOption, ColumnScreen columnScreen)
			{
				return validateCoil(response, dragOption, columnScreen);
			}
			
		private static Response validateEndCase(Response response, CoilType dragOption, ColumnScreen columnScreen)
			{
				if (columnScreen.getRowNumber() == 1 && columnScreen.getColumnNumber() == 1)
					{
						CoilType coil = dragOption;
						if (!coil.equals(CoilType.LOAD))
							{
								response.setError(true);
								response.addMessage("cannot " + dragOption.getCoilType().toUpperCase() + " in Row 1 , column 1");
							}
					}
				else if (columnScreen.getRowNumber() >= SCREEN.getEndRowNumber() && columnScreen.getColumnNumber() > SCREEN.getEndColumnNumber())
					{
						response.setError(true);
						response.addMessage("Cannot Place Coil After End of File | Row : " + SCREEN.getEndRowNumber() + " | Column : " + SCREEN.getEndColumnNumber());
					}
				return response;
			}
			
		private static Response validatePreviousOption(Response response, ColumnScreen previous, ColumnScreen columnScreen)
			{
				CoilType previousOption = previous.getCoilType();
				if (previousOption == null)
					{
						response.setError(true);
						response.addMessage("cannot place coil in between , previous option is undefined");
					}
				else
					{
						if (previousOption.equals(CoilType.OUTPUT))
							{
								response.setError(true);
								response.addMessage("Cannot place coil after OUTPUT coil");
							}
						else if (previousOption.equals(CoilType.JUMP))
							{
								response.setError(true);
								response.addMessage("Cannot place coil after JUMP coil");
							}
					}
				return response;
			}
			
		private static Response validateCoil(Response response, CoilType coilType, ColumnScreen columnScreen)
			{
				ColumnScreen previous = columnScreen.getPrevious(false);
				if (previous != null)
					{
						CoilType previousOption = previous.getCoilType();
						if (previousOption == null)
							{
								if (coilType == null)
									{
										response.setError(true);
										response.addMessage("cannot place coil in between , previous option is undefined");
									}
								else if (!coilType.equals(CoilType.LOAD) && !coilType.equals(CoilType.END))
									{
										response.setError(true);
										response.addMessage("cannot place coil in between , previous option is undefined");
									}
								else
									{
										if (coilType.equals(CoilType.LEFT_LINK) || coilType.equals(CoilType.RIGHT_LINK) || coilType.equals(CoilType.PARALLEL))
											{
												
											}
									}
							}
						else
							{
								response = validatePreviousOption(response, previous, columnScreen);
							}
					}
				return response;
			}
			
		private static Response validateLeftLink(Response response, ColumnScreen columnScreen)
			{
				int rowNumber = columnScreen.getRowNumber();
				RowScreen rowScreen = SCREEN.getRow(rowNumber - 1);
				if (rowScreen == null)
					{
						response.setError(true);
						response.addMessage("No Parent Rung Available To Create Left Link");
						
					}
				else
					{
						CoilType parentCoilType = (rowScreen.getColumnScreens(columnScreen.getColumnNumber()).getCoilType());
						if (parentCoilType == null || parentCoilType.equals(CoilType.DEFAULT))
							{
								response.setError(true);
								response.addMessage("Cannot Place Left Linked , CoilType of Parent Rung is Undefined");
							}
						else if (parentCoilType.equals(CoilType.OUTPUT) || parentCoilType.equals(CoilType.END) || parentCoilType.equals(CoilType.ROUTINE))
							{
								response.setError(true);
								response.addMessage("Cannot Place Left Link below " + parentCoilType.getCoilType());
							}
					}
				return response;
			}
			
		private static Response validateRightLink(Response response, ColumnScreen columnScreen)
			{
				int rowNumber = columnScreen.getRowNumber();
				RowScreen rowScreen = SCREEN.getRow(rowNumber - 1);
				if (rowScreen == null)
					{
						response.setError(true);
						response.addMessage("No Parent Row Available To Create Left Link");
					}
				return response;
			}
			
		private static Response validateParallelLink(Response response, ColumnScreen columnScreen)
			{
				int rowNumber = columnScreen.getRowNumber();
				RowScreen rowScreen = SCREEN.getRow(rowNumber - 1);
				if (rowScreen == null)
					{
						response.setError(true);
						response.addMessage("No Parent Row Available To Create Left Link");
					}
				return response;
			}
			
		private static Response validateLine(Response response, ColumnScreen columnScreen)
			{
				int columnNumber = columnScreen.getColumnNumber();
				if (columnNumber == 1 || columnNumber == ApplicationConstant.MAX_CELL)
					{
						response.setError(true);
						response.addMessage("Line Cannot be Place in ColumnNumber : " + columnNumber);
					}
				return response;
			}
	}
