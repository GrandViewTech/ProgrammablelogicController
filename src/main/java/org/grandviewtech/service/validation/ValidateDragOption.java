package org.grandviewtech.service.validation;

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

public class ValidateDragOption
	{
		static Screen screen = Screen.getInstance();
		
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
										response = validateLineDrag(response, CoilType.LINE, columnScreen);
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
			
		private static Response validateLineDrag(Response response, CoilType dragOption, ColumnScreen columnScreen)
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
				else if (columnScreen.getRowNumber() > screen.getEndRowNumber() || columnScreen.getColumnNumber() > screen.getEndColumnNumber())
					{
						response.setError(true);
						response.addMessage("Cannot Place Coil After End of File | Row : " + screen.getEndRowNumber() + " | Column : " + screen.getEndColumnNumber());
					}
				return response;
			}
			
		private static Response validatePreviousOption(Response response, ColumnScreen previous, ColumnScreen columnScreen)
			{
				CoilType previousOption = previous.getCoilType();
				if (previousOption == null)
					{
						response.addMessage("cannot place coil in between , previous option is undefined");
					}
				else
					{
						if (previousOption.equals(CoilType.OUTPUT))
							{
								response.addMessage("Cannot place coil after OUTPUT coil");
							}
						else if (previousOption.equals(CoilType.JUMP))
							{
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
										response.addMessage("cannot place coil in between , previous option is undefined");
									}
								else if (!coilType.equals(CoilType.LOAD) && !coilType.equals(CoilType.END))
									{
										response.addMessage("cannot place coil in between , previous option is undefined");
									}
							}
						else
							{
								response = validatePreviousOption(response, previous, columnScreen);
							}
					}
				return response;
			}
	}
