package org.grandviewtech.service.system;

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

final public class Printer
	{
		
		private static org.apache.log4j.Logger	logger					= org.apache.log4j.Logger.getLogger(Printer.class);
		
		static private boolean					isDebugPrintingEnabled	= true;
		
		public static void print(String param)
			{
				if (isDebugPrintingEnabled == true)
					{
						if (param != null && param != "")
							logger.info(param);
					}
			}
	}
