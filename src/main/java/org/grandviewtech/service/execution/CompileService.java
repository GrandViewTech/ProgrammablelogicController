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
import org.grandviewtech.entity.enums.NoNc;
import org.grandviewtech.runner.Run;
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
														String label = createLabel(column.getRowNumber(), column.getColumnNumber());
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
																				OUTPUT_TYPE outputType = findOutputType(column.getNonc());
																				output(joiner, input, outputType, label);
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
																OUTPUT_TYPE outputType = findOutputType(column.getNonc());
																output(joiner, value, outputType, label);
															}
														else if (coilType.equals(CoilType.ROUTINE))
															{
																boolean isJumpRequired = isJumpRequired(column);
																if (isJumpRequired)
																	{
																		joiner.add("JNC " + label);
																	}
																routine(joiner, column.getRoutine());
																if (isJumpRequired)
																	{
																		joiner.add(label + " :");
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
			
		private static void output(StringJoiner joiner, int input, OUTPUT_TYPE outputType, String label)
			{
				String[] params = findParam(input);
				switch (outputType)
					{
						
						case RESET:
							{
								joiner.add("JNC "+label);
								joiner.add("MOV  DTPR , #OUTPUT0_7+" + params[0]);
								joiner.add("MOV X A,@DPTR");
								joiner.add("MOV ACC." + params[1] + " , C");
								joiner.add("MOVX @DPTR,A");
								joiner.add(label+" :");
								break;
							}
						case SET:
							{
								joiner.add("JNC "+label);
								joiner.add("MOV  DTPR , #OUTPUT0_7+" + params[0]);
								joiner.add("MOV X A,@DPTR");
								joiner.add("MOV ACC." + params[1] + " , C");
								joiner.add("MOVX @DPTR,A");
								joiner.add("SETB "); // NO UNDERSTANDING OF RLY
								joiner.add(label+" :");
								break;
							}
						case NONE:
						default:
							{
								joiner.add("MOV  DTPR , #OUTPUT0_7+" + params[0]);
								joiner.add("MOV X A,@DPTR");
								joiner.add("MOV ACC." + params[1] + " , C");
								joiner.add("MOVX @DPTR,A");
								break;
							}
					}
					
			}
			
		public static void setReset()
			{
				
			}
			
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
			
		private static void nonc(StringJoiner joiner, NoNc noNc, String remainder)
			{
				
			}
			
		private static void seriesParallel(StringJoiner joiner, PARALLEL_SERIES PARALLEL_SERIES, String remainder)
			{
				switch (PARALLEL_SERIES)
					{
						case NONE:
							{
								joiner.add("MOV C, ACC." + remainder);
								break;
							}
						case PARALLEL:
							{
								joiner.add("ORL C, ACC." + remainder);
								break;
							}
						case SERIES:
							{
								joiner.add("ANL C, ACC." + remainder);
								break;
							}
							
					}
			}
			
		private static boolean isJumpRequired(ColumnScreen column)
			{
				// TO CHECK IF PREVIOUS IS AND
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
			
		private static String createLabel(int rowNumber, int columnNumber)
			{
				return "JNC LABEL_" + rowNumber + "_" + columnNumber;
			}
			
		public static OUTPUT_TYPE findOutputType(NoNc nonc)
			{
				if (nonc != null)
					{
						switch (nonc)
							{
								
								case RESET:
									{
										return OUTPUT_TYPE.RESET;
									}
								case SET:
									{
										return OUTPUT_TYPE.SET;
									}
								case DEFAULT:
								case NC:
								case NO:
								default:
									return OUTPUT_TYPE.NONE;
							}
					}
				return OUTPUT_TYPE.NONE;
			}
			
		public enum OUTPUT_TYPE
			{
			SET, RESET, NONE;
			}
			
		public enum PARALLEL_SERIES
			{
			PARALLEL, SERIES, NONE
			}
			
	}
