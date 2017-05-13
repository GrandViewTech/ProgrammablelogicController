package org.grandviewtech.service.execution;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.grandviewtech.entity.bo.Routine;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.entity.enums.InputType;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.screen.ColumnScreen;
import org.grandviewtech.userinterface.screen.PreferenceScreen;
import org.grandviewtech.userinterface.screen.RowScreen;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;

public abstract class CompileService
	{
		private static Screen			screen			= Screen.getInstance();
		
		private static boolean			Integer2Hex		= false;
		
		private static Configuration	configuration	= new Configuration(new Version(2, 3, 23));
		
		public static void generateAsmFile() throws IOException
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
												ColumnScreenGenerator.createColumnNeighbourHood(screen.getRow(column.getRowNumber()), column);
												if (coilType.equals(CoilType.END))
													{
														loopbreakCondition = true;
														break;
													}
												else
													{
														if (coilType.equals(CoilType.LOAD))
															{
																int input = new Integer(column.getValue());
																PARALLEL_SERIES PARALLEL_SERIES = findParallelSeries(column);
																switch (column.getInputType())
																	{
																		case FLAG:
																			{
																				flag(joiner, input, PARALLEL_SERIES);
																				break;
																			}
																		case INPUT:
																			{
																				input(joiner, input, PARALLEL_SERIES);
																				break;
																			}
																		case OUTPUT:
																			{
																				output(joiner, input);
																				break;
																			}
																		case WORD:
																			{
																				word(joiner, input, PARALLEL_SERIES);
																				break;
																			}
																			
																	}
																// TODO YET TO DECIDE ON AND OR LOGIC
																
															}
														else if (coilType.equals(CoilType.OUTPUT))
															{
																int value = new Integer(column.getValue());
																output(joiner, value);
															}
														else if (coilType.equals(CoilType.ROUTINE))
															{
																boolean isJumpRequired = isJumpRequired(column);
																if (isJumpRequired)
																	{
																		joiner.add("JNC LABEL_" + column.getRowNumber() + "_" + column.getColumnNumber());
																	}
																routine(joiner, column.getRoutine());
																if (isJumpRequired)
																	{
																		joiner.add("LABEL_" + column.getRowNumber() + "_" + column.getColumnNumber() + " :");
																	}
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
			
		private static void routine(StringJoiner joiner, Routine routine) throws IOException
			{
				try
					{
						Map<String, String> dataset = new LinkedHashMap<>();
						for (Map.Entry<Integer, String> value : routine.getValues().entrySet())
							{
								String key = "INPUT" + value.getKey();
								dataset.put(key, value.getValue());
							}
						configuration.setNumberFormat("0.######");
						Template template = new Template("templateBody", new StringReader(routine.getFunctionalBlock()), configuration);
						joiner.add(FreeMarkerTemplateUtils.processTemplateIntoString(template, dataset));
					}
				catch (Exception exception)
					{
					}
			}
			
		private static void input(StringJoiner joiner, int input, PARALLEL_SERIES PARALLEL_SERIES)
			{
				// input = findMinValue(InputType.FLAG) - input;
				String[] params = findParam(input);
				joiner.add("MOV  DTPR , #INPUT0_7+" + params[0]);
				joiner.add("MOV X A,@DPTR");
				joiner.add("MOV C, ACC." + params[1]);
				seriesParallel(joiner, PARALLEL_SERIES, params[1]);
			}
			
		private static void word(StringJoiner joiner, int input, PARALLEL_SERIES PARALLEL_SERIES)
			{
				// input = findMinValue(InputType.FLAG) - input;
				String[] params = findParam(input);
				joiner.add("MOV  DTPR , #WORD0_7+" + params[0]);
				joiner.add("MOV X A,@DPTR");
				seriesParallel(joiner, PARALLEL_SERIES, params[1]);
			}
			
		private static void flag(StringJoiner joiner, int input, PARALLEL_SERIES PARALLEL_SERIES)
			{
				input = findMinValue(InputType.FLAG) - input;
				String[] params = findParam(input);
				joiner.add("MOV  DTPR , #RLY512_+" + params[0]);
				joiner.add("MOV X A,@DPTR");
				seriesParallel(joiner, PARALLEL_SERIES, params[1]);
			}
			
		private static void output(StringJoiner joiner, int input)
			{
				String[] params = findParam(input);
				joiner.add("MOV  DTPR , #OUTPUT0_7+" + params[0]);
				joiner.add("MOV X A,@DPTR");
				joiner.add("MOV ACC." + params[1] + " , C");
				joiner.add("MOVX @DPTR,A");
			}
			
		/*
		 * private static void and(StringJoiner joiner, int value) { int input = 512 - value; if (input < 0) { input = input * -1; } String param1 = intTohex(input / 8); String param2 = intTohex(input % 8); joiner.add("MOV  DTPR , #RLY512_519+" +
		 * param1); joiner.add("MOV X A,@DPTR"); joiner.add("MOV C, C." + param2); }
		 */
		
		/*
		 * private static void output(StringJoiner joiner, int value) { int input = value; String param1 = intTohex(input / 8); String param2 = intTohex(input % 7); joiner.add("MOV  DTPR , #OUTPUT0_7+" + param1); joiner.add("MOV X A,@DPTR");
		 * joiner.add("MOV C, C." + param2); joiner.add("MOVX  @DPTR,A"); }
		 */
		
		public static String[] findParam(Integer input)
			{
				if (input < 0)
					{
						input = input * -1;
					}
				return new String[]
					{ intTohex(input / 8), intTohex(input % 8) };
			}
			
		private static String intTohex(int integer)
			{
				if (Integer2Hex)
					{
						return Integer.toHexString(integer);
					}
				return "" + integer;
			}
			
		private static Integer findMinValue(InputType inputType)
			{
				String min = "min" + inputType.getInputType();
				return PreferenceScreen.get(min);
				/*
				 * String max = "max" + inputType.getInputType(); int maxsize = PreferenceScreen.get(max);
				 */
			}
			
		private static void seriesParallel(StringJoiner joiner, PARALLEL_SERIES PARALLEL_SERIES, String remainder)
			{
				switch (PARALLEL_SERIES)
					{
						case NONE:
							{
								joiner.add("MOV ACC." + remainder + " , C");
								break;
							}
						case PARALLEL:
							{
								joiner.add("ORL ACC." + remainder + " , C");
								break;
							}
						case SERIES:
							{
								joiner.add("ANL ACC." + remainder + " , C");
								break;
							}
							
					}
			}
			
		private static boolean isJumpRequired(ColumnScreen column)
			{
				// NEED TO CHECK IF PREVIOUS IS AND
				// CHECK IF JUMP IS REQUIED
				ColumnScreen previous = column.getPrevious(false);
				if (previous != null && previous.getCoilType().equals(CoilType.LOAD))
					{
						return true;
					}
				return false;
			}
			
		public static PARALLEL_SERIES findParallelSeries(ColumnScreen column)
			{
				ColumnScreen previous = column.getPrevious(false);
				if (previous != null)
					{
						if (previous.getCoilType().equals(CoilType.LOAD))
							{
								return PARALLEL_SERIES.SERIES;
							}
					}
				return PARALLEL_SERIES.NONE;
			}
			
		public enum PARALLEL_SERIES
			{
			PARALLEL, SERIES, NONE
			}
			
	}
