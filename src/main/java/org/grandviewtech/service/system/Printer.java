package org.grandviewtech.service.system;

final public class Printer
	{
		static private boolean isDebugPrintingEnabled = false;
		
		public static void print(String param)
			{
				if (isDebugPrintingEnabled == true)
					{
						System.out.println(param);
					}
			}
	}
