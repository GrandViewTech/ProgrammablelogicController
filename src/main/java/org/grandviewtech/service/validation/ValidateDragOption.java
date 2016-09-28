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

import org.grandviewtech.constants.Coils;
import org.grandviewtech.entity.bo.Response;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ValidateDragOption
	{
		static Screen screen = Screen.getInstance();
		
		public static Response validateDragOption(ColumnScreen columnScreen, String dragOption)
			{
				Response response = new Response();
				response = validateEndCase(response, dragOption, columnScreen);
				if ( !response.isError() )
					{
						switch (dragOption)
							{
								case Coils.LINE:
									{
										response = validateLineDrag(response, Coils.LINE, columnScreen);
										break;
									}
								case Coils.OUTPUT:
									{
										response = validateOutputDrag(response, Coils.OUTPUT, columnScreen);
										break;
									}
								case Coils.LOAD:
									{
										response = validateLoadtDrag(response, Coils.LOAD, columnScreen);
										break;
									}
								case Coils.JUMP:
									{
										
										response = validateCoil(response, Coils.JUMP, columnScreen);
										break;
									}
							}
					}
				return response;
			}
			
		private static Response validateLoadtDrag(Response response, String dragOption, ColumnScreen columnScreen)
			{
				return validateCoil(response, dragOption, columnScreen);
			}
			
		private static Response validateOutputDrag(Response response, String dragOption, ColumnScreen columnScreen)
			{
				return validateCoil(response, dragOption, columnScreen);
			}
			
		private static Response validateLineDrag(Response response, String dragOption, ColumnScreen columnScreen)
			{
				return validateCoil(response, dragOption, columnScreen);
			}
			
		private static Response validateEndCase(Response response, String dragOption, ColumnScreen columnScreen)
			{
				if ( columnScreen.getRowNumber() == 1 && columnScreen.getColumnNumber() == 1 )
					{
						String coil = dragOption;
						if ( !coil.equalsIgnoreCase(Coils.LOAD) )
							{
								response.setError(true);
								response.addMessage("cannot " + dragOption.toUpperCase() + " in Row 1 , column 1");
							}
					}
				else if ( columnScreen.getRowNumber() > screen.getEndRowNumber() || columnScreen.getColumnNumber() > screen.getEndColumnNumber() )
					{
						response.setError(true);
						response.addMessage("Cannot Place Coil After End of File | Row : " + screen.getEndRowNumber() + " | Column : " + screen.getEndColumnNumber());
					}
				return response;
			}
			
		private static Response validatePreviousOption(Response response, ColumnScreen previous, ColumnScreen columnScreen)
			{
				String previousOption = previous.getCoil();
				if ( previousOption == null || previousOption.trim().length() == 0 )
					{
						response.addMessage("cannot place coil in between , previous option is undefined");
					}
				else
					{
						if ( previousOption.equalsIgnoreCase(Coils.OUTPUT) )
							{
								response.addMessage("Cannot place coil after OUTPUT coil");
							}
						else if ( previousOption.equalsIgnoreCase(Coils.JUMP) )
							{
								response.addMessage("Cannot place coil after JUMP coil");
							}
					}
				return response;
			}
			
		private static Response validateCoil(Response response, String coil, ColumnScreen columnScreen)
			{
				ColumnScreen previous = columnScreen.getPrevious(false);
				if ( previous != null )
					{
						String previousOption = previous.getCoil();
						if ( previousOption == null || previousOption.trim().length() == 0 )
							{
								if ( coil == null || coil.trim().length() == 0 )
									{
										response.addMessage("cannot place coil in between , previous option is undefined");
									}
								else if ( !coil.equalsIgnoreCase(Coils.LOAD) && !coil.equalsIgnoreCase(Coils.END) )
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
