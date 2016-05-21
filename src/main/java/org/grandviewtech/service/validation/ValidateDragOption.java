package org.grandviewtech.service.validation;

import org.grandviewtech.constants.Coils;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ValidateDragOption
	{
		
		public static boolean validateDragOption(ColumnScreen columnScreen, String dragOption)
			{
				boolean isDragOptionAllowed = false;
				switch (dragOption)
					{
						case Coils.LINE:
							{
								isDragOptionAllowed = validateLineDrag(columnScreen);
								break;
							}
						case Coils.OUTPUT:
							{
								isDragOptionAllowed = validateOutputDrag(columnScreen);
								break;
							}
						case Coils.LOAD:
							{
								isDragOptionAllowed = validateLoadtDrag(columnScreen);
								break;
							}
						case Coils.JUMP:
							{
								isDragOptionAllowed = true;
								break;
							}
					}
				return isDragOptionAllowed;
			}
			
		private static boolean validateLoadtDrag(ColumnScreen columnScreen)
			{
				ColumnScreen previous = columnScreen.getPrevious(false);
				if (previous == null)
					{
						return true;
					}
				else
					{
						String previousOption = previous.getCoil();
						if (previousOption == null || previousOption.trim().length() == 0)
							{
								return false;
							}
						if (previousOption.equalsIgnoreCase(Coils.OUTPUT) || previousOption.equalsIgnoreCase(Coils.JUMP))
							{
								return false;
							}
						else
							{
								return true;
							}
					}
			}
			
		private static boolean validateOutputDrag(ColumnScreen columnScreen)
			{
				ColumnScreen previous = columnScreen.getPrevious(false);
				if (previous == null)
					{
						return false;
					}
				else
					{
						String previousOption = previous.getCoil();
						if (previousOption == null || previousOption.trim().length() == 0)
							{
								return false;
							}
						if (previousOption.equalsIgnoreCase(Coils.OUTPUT) || previousOption.equalsIgnoreCase(Coils.JUMP))
							{
								return false;
							}
						else
							{
								return true;
							}
					}
			}
			
		private static boolean validateLineDrag(ColumnScreen columnScreen)
			{
				ColumnScreen previous = columnScreen.getPrevious(false);
				if (previous == null)
					{
						return true;
					}
				else
					{
						String previousOption = previous.getCoil();
						if (previousOption == null || previousOption.trim().length() == 0)
							{
								return false;
							}
						if (previousOption.equalsIgnoreCase(Coils.OUTPUT) || previousOption.equalsIgnoreCase(Coils.JUMP))
							{
								return false;
							}
						else
							{
								return true;
							}
					}
			}
	}
