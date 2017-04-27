package org.grandviewtech.service.execution;

import java.util.StringJoiner;

import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.RowScreen;

public abstract class CompileService
	{
		private static Screen	screen		= Screen.getInstance();
		
		private static boolean	Integer2Hex	= false;
		
		public static void generateAsmFile()
			{
				StringJoiner joiner = new StringJoiner("\n");
				joiner.add("----- ASM CODE ------");
				boolean loopbreakCondition = false;
				for (RowScreen row : screen.getRows())
					{
						
						for (ColumnScreen column : row.getAllColumnScreens())
							{
								if (!column.isBlank())
									{
										CoilType coilType = column.getCoilType();
										if (coilType != null)
											{
												if (coilType.equals(CoilType.END))
													{
														loopbreakCondition = true;
														break;
													}
												else
													{
														if (coilType.equals(CoilType.LOAD))
															{
																int value = new Integer(column.getValue());
																and(joiner, value);
															}
														else if (coilType.equals(CoilType.OUTPUT))
															{
																int value = new Integer(column.getValue());
																output(joiner, value);
															}
													}
											}
									}
									
							}
						if (loopbreakCondition)
							{
								break;
							}
					}
				joiner.add("----- ASM CODE ------");
				System.out.println(joiner.toString());
			}
			
		private static void and(StringJoiner joiner, int value)
			{
				int input = 512 - value;
				if (input < 0)
					{
						input = input * -1;
					}
				String param1 = intTohex(input / 8);
				String param2 = intTohex(input % 8);
				joiner.add("MOV  DTPR , #RLY512_519+" + param1);
				joiner.add("MOV X A,@DPTR");
				joiner.add("MOV C, ACC." + param2);
			}
			
		private static void output(StringJoiner joiner, int value)
			{
				int input = value;
				String param1 = intTohex(input / 8);
				String param2 = intTohex(input % 8);
				joiner.add("MOV  DTPR , #OUTPUT0_7+" + param1);
				joiner.add("MOV X A,@DPTR");
				joiner.add("MOV C, ACC." + param2);
			}
			
		private static void or(StringJoiner joiner, int value)
			{
				int input = 512 - value;
				String param1 = intTohex(input / 8);
				String param2 = intTohex(input % 8);
				joiner.add("MOV  DTPR , #(RLY512_519)+" + param1);
				joiner.add("MOV X A,@DPTR");
				joiner.add("MOV C, ACC." + param2);
			}
			
		private static String intTohex(int integer)
			{
				if (Integer2Hex)
					{
						return Integer.toHexString(integer);
					}
				return "" + integer;
			}
	}
