package org.grandviewtech.userinterface.screen;

import java.awt.Component;

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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;

import org.apache.commons.io.FilenameUtils;
import org.grandviewtech.constants.CustomDimension;
import org.grandviewtech.entity.bo.Routine;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.entity.enums.Edge;
import org.grandviewtech.entity.enums.InputType;
import org.grandviewtech.entity.enums.NoNc;
import org.grandviewtech.entity.enums.Signal;
import org.grandviewtech.entity.helper.Dimension;
import org.grandviewtech.runner.Application;
import org.grandviewtech.service.system.PropertyReader;

import com.thoughtworks.xstream.XStream;

public class ColumnConfigurationScreen extends JFrame
	{
		private static org.apache.log4j.Logger	logger				= org.apache.log4j.Logger.getLogger(ColumnConfigurationScreen.class);
		final static Screen						SCREEN				= Screen.getInstance();
		private static final long				serialVersionUID	= 1L;
		private static NumberFormatter			numberFormatter		= null;
		
		static
			{
				numberFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
				numberFormatter.setValueClass(Integer.class);
				numberFormatter.setAllowsInvalid(false);
				numberFormatter.setMinimum(0);
			}
			
		private ButtonGroup				buttonGroup						= new ButtonGroup();
		
		private JRadioButton			input							= new JRadioButton("Input");
		
		private JRadioButton			flag							= new JRadioButton("Flag");
		
		private JRadioButton			word							= new JRadioButton("Word");
		
		private JRadioButton			output							= new JRadioButton("Output");
		
		private ButtonGroup				edgeButtonGroup					= new ButtonGroup();
		
		private JRadioButton			risingEdge						= new JRadioButton("Rising");
		
		private JRadioButton			fallingEdge						= new JRadioButton("Falling");
		
		private ButtonGroup				signalTypeButton;
		
		private JRadioButton			NC;
		
		private JRadioButton			NO;
		
		private JRadioButton			SET;
		
		private JRadioButton			RESET;
		
		private JPanel					panel							= new JPanel();
		
		private JLabel					edgeLabel						= new JLabel("Edge");
		
		private JLabel					ncnoLabel;
		
		private JLabel					inputValue						= new JLabel("Value");
		
		private JFormattedTextField		value							= new JFormattedTextField(numberFormatter);
		
		private JLabel					type							= new JLabel("Type");
		
		private JLabel					tagLabel						= new JLabel("Tag");
		
		private JTextField				tagValue						= new JTextField();
		
		private JButton					submit							= new JButton("Submit");
		
		private JButton					cancel							= new JButton("Canel");
		
		private JScrollPane				scrollableConfigurationScreen	= null;
		
		private String					resourcePath					= PropertyReader.getProperties("resourcePath") + File.separator + PropertyReader.getProperties("routinePath");
		
		private Map<String, JTextField>	dataset							= new HashMap<String, JTextField>();
		private static final int		X1								= 10;
		private static final int		X2								= 150;
		private static final int		Y								= 30;
		private static final int		WIDTH							= 120;
		private static final int		RADIO_WIDTH						= 80;
		private static final int		HEIGHT							= 20;
		
		public ColumnConfigurationScreen()
			{
				reset();
				scrollableConfigurationScreen = new JScrollPane(panel);
				scrollableConfigurationScreen.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrollableConfigurationScreen.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			}
			
		private void reset()
			{
				value.setText("");
				tagValue.setText("");
			}
			
		private ColumnScreen columnScreen = null;
		
		public void initiateInstance(ColumnScreen columnScreen)
			{
				try
					{
						this.columnScreen = columnScreen;
						setAlwaysOnTop(true);
						setTitle(" Row : " + columnScreen.getRowNumber() + " | Column : " + columnScreen.getColumnNumber());
						if (isLoadCoil())
							{
								loadCoilConfiguration();
							}
						else if (columnScreen.getTemp().equals(CoilType.ROUTINE))
							{
								routineCoilConfiguration();
							}
						else if (columnScreen.getTemp().equals(CoilType.OUTPUT))
							{
								outputCoilConfiguration();
							}
					}
				catch (Exception exception)
					{
						logger.error(exception.getLocalizedMessage(), exception);
					}
			}
			
		private void routineCoilConfiguration()
			{
				routineList();
				invokeFrame(CustomDimension.ROUTINE_CONFIGURATION_SCREEN);
			}
			
		private Routine selectedRoutine = null;
		
		private void routineList()
			{
				
				List<String> dataList = new ArrayList<>();
				File folder = (new File(resourcePath));
				if (!folder.exists())
					{
						folder.mkdirs();
					}
				for (File file : folder.listFiles())
					{
						if (file.isFile() && FilenameUtils.getExtension(file.getName()).contains("xml"))
							{
								String routineName = FilenameUtils.getBaseName(file.getName());
								dataList.add(routineName);
							}
					}
					
				dataList.sort(new Comparator<String>()
					{
						@Override
						public int compare(String object1, String object2)
							{
								return object1.compareTo(object2);
							}
					});
				DefaultListModel<String> defaultListModel = new DefaultListModel<String>();
				for (String data : dataList)
					{
						defaultListModel.addElement(data);
					}
				JList<String> routineList = new JList<String>(defaultListModel);
				if (dataList.size() > 0)
					{
						routineList.setSelectedIndex(0);
						selectRoutine(dataList.get(0));
					}
				routineList.addListSelectionListener(new ListSelectionListener()
					{
						@Override
						public void valueChanged(ListSelectionEvent event)
							{
								if (!event.getValueIsAdjusting())
									{
										if (routineList.getSelectedValuesList().size() > 0)
											{
												selectRoutine(routineList.getSelectedValuesList().get(0));
											}
									}
							}
					});
				routineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				// routineList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
				JScrollPane scrollPane = new JScrollPane(routineList);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setBounds(X1, Y - 5, 150, 400);
				panel.add(scrollPane);
				JButton submit = new JButton("Submit");
				submit.setBounds(X1 + 180, 410, 150, 25);
				if (dataList == null || dataList.size() == 0)
					{
						submit.setEnabled(false);
					}
				submit.addActionListener(onSubmit ->
					{
						
						JOptionPane optionPane = null;
						if (selectedRoutine != null)
							{
								for (Entry<String, JTextField> data : dataset.entrySet())
									{
										selectedRoutine.addValue(new Integer(data.getKey()), data.getValue().getText());
									}
								columnScreen.setRoutine(selectedRoutine);
								optionPane = new JOptionPane("Routine Selected Successfully", JOptionPane.INFORMATION_MESSAGE);
								JDialog dialog = optionPane.createDialog(null, "Select Routine");
								dialog.setModal(false);
								dialog.setVisible(true);
								// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html#stayup
								Timer timer = new Timer(600, timerEvent ->
									{
										dialog.setVisible(false);
										dialog.dispose();
										columnScreen.apply();
									});
								timer.start();
								dispose();
							}
						else
							{
								optionPane = new JOptionPane("Error While Selecting Routine", JOptionPane.INFORMATION_MESSAGE);
								dispose();
							}
					});
				JButton cancel = new JButton("Canel");
				cancel.setBounds(X1 + 350, 410, 150, 25);
				cancel.addActionListener(event ->
					{
						this.dispose();
					});
				panel.add(submit);
				panel.add(cancel);
			}
			
		List<Component> routineComponent = new ArrayList<>();
		
		private void selectRoutine(String selectedRoutineName)
			{
				try
					{
						if (routineComponent.size() > 0)
							{
								for (Component component : routineComponent)
									{
										panel.remove(component);
									}
							}
						Map<String, JTextField> inputFields = new LinkedHashMap<String, JTextField>();
						JLabel name = new JLabel();
						name.setText("Name      : ");
						name.setBounds(X1 + 170, Y - 5, 150, 25);
						routineComponent.add(name);
						panel.add(name);
						JLabel description = new JLabel();
						description.setText("Descrption : ");
						description.setBounds(X1 + 170, (Y - 5) + 25, 150, 25);
						routineComponent.add(description);
						panel.add(description);
						JTextArea descriptionTextArea = new JTextArea(3, 500);
						descriptionTextArea.setBounds(X1 + 250, (Y - 5) + 25, 600, 60);
						panel.add(descriptionTextArea);
						routineComponent.add(descriptionTextArea);
						name.setText("Name : " + selectedRoutineName);
						File selectedFile = new File(PropertyReader.getProperties("resourcePath") + File.separator + PropertyReader.getProperties("routinePath") + File.separator + selectedRoutineName + ".xml");
						XStream stream = new XStream();
						Object object;
						object = stream.fromXML(new FileInputStream(selectedFile));
						Routine routine = (Routine) object;
						descriptionTextArea.setText(routine.getDescription());
						descriptionTextArea.setToolTipText(descriptionTextArea.getText());
						descriptionTextArea.setEditable(false);
						int height = (Y - 5) + 70;
						int counter = 1;
						Routine current = columnScreen.getRoutine();
						for (Map.Entry<Integer, String> input : routine.getInputs().entrySet())
							{
								
								String value = input.getValue();
								JLabel label = new JLabel("Input(" + input.getKey() + ") :");
								label.setName(value);
								JTextField inputTextField = new JTextField();
								inputTextField.setName("" + input.getKey());
								if (current != null)
									{
										if (current.getValues() != null)
											{
												String val = current.getValues().get(input.getKey());
												if (val == null)
													{
														val = "";
													}
												inputTextField.setText(val);
											}
									}
								inputFields.put(inputTextField.getName(), inputTextField);
								label.setBounds(((counter % 2 != 0) ? X1 : X2 + 100) + 170, ((counter % 2 != 0) ? height = height + 25 : height), 150, 25);
								inputTextField.setBounds(((counter % 2 != 0) ? X1 : X2 + 100) + 250, height, 100, 25);
								inputTextField.addActionListener(action -> routine.addValue(new Integer(inputTextField.getName()), inputTextField.getText()));
								panel.add(label);
								routineComponent.add(label);
								panel.add(inputTextField);
								routineComponent.add(inputTextField);
								dataset.put(inputTextField.getName(), inputTextField);
								counter = counter + 1;
							}
						selectedRoutine = routine;
						panel.repaint();
					}
				catch (Exception exception)
					{
						logger.error(exception.getLocalizedMessage(), exception);
					}
					
			}
			
		private void outputCoilConfiguration()
			{
				addInputValueToScreen();
				addInputOptionsToScreen();
				addNcNoOptionToScreen();
				addTagToScreen(6);
				addSubmitToScreen(7);
				addCancelToScreen(7);
				invokeFrame(CustomDimension.OUTPUT_CONFIGURATION_SCREEN);
			}
			
		private void loadCoilConfiguration()
			{
				addInputValueToScreen();
				addInputOptionsToScreen();
				addEdgeOptionToScreen();
				addNcNoOptionToScreen();
				addTagToScreen(6);
				addSubmitToScreen(7);
				addCancelToScreen(7);
				invokeFrame(CustomDimension.LOAD_CONFIGURATION_SCREEN);
			}
			
		public void addInputValueToScreen()
			{
				inputValue.setBounds(X1, Y - 5, WIDTH, HEIGHT);
				value.setBounds(X2, Y - 5, WIDTH, HEIGHT);
				if (columnScreen.getValue() != null && columnScreen.getValue().length() > 0)
					{
						String data = columnScreen.getValue();
						if (data != null && data.trim().length() > 0)
							{
								value.setValue(new Integer(data));
							}
					}
				// separator.setBounds(X1, Y * 1 + (20),
				// CustomDimension.CONFIGURATION_SCREEN.width - (X1 * 4), 10);
				panel.add(inputValue);
				panel.add(value);
				// panel.add(separator);
			}
			
		public void addOutputOption(ColumnScreen columnScreen)
			{
				if (columnScreen.getTemp().equals(CoilType.OUTPUT))
					{
						ButtonGroup buttonGroup = new ButtonGroup();
						JRadioButton no = new JRadioButton("NO");
						no.setMnemonic(KeyEvent.VK_O);
						JRadioButton nc = new JRadioButton("NC");
						nc.setMnemonic(KeyEvent.VK_C);
						JRadioButton set = new JRadioButton("SET");
						set.setMnemonic(KeyEvent.VK_S);
						JRadioButton reset = new JRadioButton("RESET");
						reset.setMnemonic(KeyEvent.VK_R);
						buttonGroup.add(no);
						buttonGroup.add(nc);
						buttonGroup.add(set);
						buttonGroup.add(reset);
					}
			}
			
		public void addInputOptionsToScreen()
			{
				if (isLoadCoil())
					{
						addRadioButtonsToInputButtonGroup();
						type.setBounds(X1, Y * 2, RADIO_WIDTH, HEIGHT);
						input.setBounds(X2, Y * 2, RADIO_WIDTH, HEIGHT);
						// input.setSelected(true);
						word.setBounds(X2 + (RADIO_WIDTH * 1), Y * 2, RADIO_WIDTH, HEIGHT);
						flag.setBounds(X2, Y * 3, RADIO_WIDTH, HEIGHT);
						output.setBounds(X2 + (RADIO_WIDTH * 1), Y * 3, RADIO_WIDTH, HEIGHT);
						setDefaultInputOption();
						panel.add(type);
						panel.add(input);
						panel.add(flag);
						panel.add(word);
						panel.add(output);
						// panel.add(separator);
					}
				else if (columnScreen.getTemp().equals(CoilType.OUTPUT))
					{
						addRadioButtonsToOutputButtonGroup();
						type.setBounds(X1, Y * 2, RADIO_WIDTH, HEIGHT);
						// flag.setSelected(true);
						flag.setBounds(X2, Y * 2, RADIO_WIDTH, HEIGHT);
						output.setBounds(X2 + (RADIO_WIDTH * 1), Y * 2, RADIO_WIDTH, HEIGHT);
						setDefaultInputOption();
						// separator.setBounds(X1, Y * 3 + (20),
						// CustomDimension.CONFIGURATION_SCREEN.width - (X1 *
						// 4), 10);
						// setDefaultInputOption(2, columnScreen);
						panel.add(type);
						panel.add(flag);
						panel.add(output);
						// panel.add(separator);
					}
				;
			}
			
		public void addRadioButtonsToInputButtonGroup()
			{
				buttonGroup.add(input);
				buttonGroup.add(flag);
				buttonGroup.add(word);
				buttonGroup.add(output);
				addMnemonicToOptionRadioButton();
			}
			
		public void addRadioButtonsToOutputButtonGroup()
			{
				buttonGroup.add(flag);
				buttonGroup.add(output);
				addMnemonicToOptionRadioButton();
			}
			
		public void addMnemonicToOptionRadioButton()
			{
				input.setMnemonic(KeyEvent.VK_I);
				flag.setMnemonic(KeyEvent.VK_F);
				word.setMnemonic(KeyEvent.VK_W);
				output.setMnemonic(KeyEvent.VK_O);
			}
			
		public void addRadioButtonsToEdgeButtonGroup()
			{
				edgeButtonGroup.add(risingEdge);
				edgeButtonGroup.add(fallingEdge);
				addMnemonicToEdgeRadioButton();
			}
			
		public void addMnemonicToEdgeRadioButton()
			{
				risingEdge.setMnemonic(KeyEvent.VK_R);
				fallingEdge.setMnemonic(KeyEvent.VK_G);
			}
			
		public void addEdgeOptionToScreen()
			{
				addRadioButtonsToEdgeButtonGroup();
				edgeLabel.setBounds(X1, Y * 4, RADIO_WIDTH, HEIGHT);
				risingEdge.setBounds(X2, Y * 4, RADIO_WIDTH, HEIGHT);
				fallingEdge.setBounds(X2 + (RADIO_WIDTH * 1), Y * 4, RADIO_WIDTH, HEIGHT);
				// separator.setBounds(X1, Y * 4 + (20),
				// CustomDimension.CONFIGURATION_SCREEN.width - (X1 * 4), 10);
				setDefaultEdgeOption();
				panel.add(edgeLabel);
				panel.add(risingEdge);
				panel.add(fallingEdge);
				// panel.add(separator);
			}
			
		// NC
		public void addRadioButtonsToNoNcButtonGroup()
			{
				signalTypeButton = new ButtonGroup();
				CoilType coilType = columnScreen.getTemp();
				if (isLoadCoil())
					{
						NC = new JRadioButton("NC");
						NO = new JRadioButton("NO");
						risingEdge = new JRadioButton("RISING");
						fallingEdge = new JRadioButton("FALLING");
						signalTypeButton.add(risingEdge);
						signalTypeButton.add(fallingEdge);
						signalTypeButton.add(NC);
						signalTypeButton.add(NO);
					}
				else if (coilType.equals(CoilType.OUTPUT))
					{
						NC = new JRadioButton("NC");
						NO = new JRadioButton("NO");
						risingEdge = new JRadioButton("RISING");
						fallingEdge = new JRadioButton("FALLING");
						SET = new JRadioButton("SET");
						RESET = new JRadioButton("RESET");
						signalTypeButton.add(risingEdge);
						signalTypeButton.add(fallingEdge);
						signalTypeButton.add(NC);
						signalTypeButton.add(NO);
						signalTypeButton.add(SET);
						signalTypeButton.add(RESET);
					}
				addMnemonicToNoNcRadioButton();
			}
			
		public void addMnemonicToNoNcRadioButton()
			{
				CoilType coilType = columnScreen.getTemp();
				if (isLoadCoil())
					{
						NO.setMnemonic(KeyEvent.VK_N);
						NC.setMnemonic(KeyEvent.VK_C);
					}
				else if (coilType.equals(CoilType.OUTPUT))
					{
						NO.setMnemonic(KeyEvent.VK_N);
						NC.setMnemonic(KeyEvent.VK_C);
						SET.setMnemonic(KeyEvent.VK_S);
						RESET.setMnemonic(KeyEvent.VK_R);
					}
			}
			
		public void addNcNoOptionToScreen()
			{
				addRadioButtonsToNoNcButtonGroup();
				
				if (isLoadCoil())
					{
						ncnoLabel = new JLabel("NO/NC");
						ncnoLabel.setBounds(X1, Y * 5, RADIO_WIDTH, HEIGHT);
						NC.setBounds(X2, Y * 5, RADIO_WIDTH, HEIGHT);
						NO.setBounds(X2 + (RADIO_WIDTH * 1), Y * 5, RADIO_WIDTH, HEIGHT);
					}
				else if (columnScreen.getTemp().equals(CoilType.OUTPUT))
					{
						ncnoLabel = new JLabel("OPTION");
						ncnoLabel.setBounds(X1, Y * 3, RADIO_WIDTH, HEIGHT);
						NC.setBounds(X2, Y * 3, RADIO_WIDTH, HEIGHT);
						NO.setBounds(X2 + (RADIO_WIDTH * 1), Y * 3, RADIO_WIDTH, HEIGHT);
						SET.setBounds(X2, Y * 4, RADIO_WIDTH, HEIGHT);
						RESET.setBounds(X2 + (RADIO_WIDTH * 1), Y * 4, RADIO_WIDTH, HEIGHT);
						panel.add(SET);
						panel.add(RESET);
					}
				panel.add(ncnoLabel);
				panel.add(NC);
				panel.add(NO);
				setDefaultNoNcOption(columnScreen);
				
			}
			
		public void addTagToScreen(int x)
			{
				tagLabel.setBounds(X1, Y * x, WIDTH, HEIGHT);
				tagLabel.setEnabled(true);
				tagLabel.setToolTipText("Add A Tag for Current Coil");
				tagValue.setBounds(X2, Y * x, WIDTH, HEIGHT);
				String tag = columnScreen.getTag();
				if (tag != null && tag.trim().length() > 0)
					{
						tagValue.setText(tag);
					}
				// separator.setBounds(X1, Y * 6 + (20),
				// CustomDimension.CONFIGURATION_SCREEN.width - (X1 * 4), 10);
				panel.add(tagLabel);
				panel.add(tagValue);
				// panel.add(separator);
			}
			
		private void addSubmitToScreen(int x)
			{
				if (isLoadCoil())
					{
						submit.setBounds(X1, Y * x + 20, WIDTH, HEIGHT);
					}
				else if (columnScreen.getTemp().equals(CoilType.OUTPUT) || columnScreen.getTemp().equals(CoilType.ROUTINE))
					{
						submit.setBounds(X1, Y * x + 20, WIDTH, HEIGHT);
					}
				else if (columnScreen.getTemp().equals(CoilType.ROUTINE))
					{
						submit.setBounds(X1, Y * x + 20, WIDTH, HEIGHT);
					}
					
				panel.add(submit);
				submit.addActionListener((ActionEvent event) ->
					{
						
						if (input.isSelected() || flag.isSelected() || output.isSelected() || word.isSelected())
							{
								
								InputType inputType = findInputType();
								Integer dt = (Integer) value.getValue();
								String min = "min" + inputType.getInputType();
								String max = "max" + inputType.getInputType();
								int minsize = PreferenceScreen.get(min);
								int maxsize = PreferenceScreen.get(max);
								if (dt < minsize || dt > maxsize)
									{
										JOptionPane.showMessageDialog(submit, "Data Must be In Between " + minsize + " : " + maxsize);
									}
								else
									{
										boolean isRoutine = false;
										CoilType coilType = columnScreen.getTemp();
										if (coilType.equals(CoilType.ROUTINE))
											{
												isRoutine = true;
											}
										else if (isLoadCoil() || coilType.equals(CoilType.OUTPUT))
											{
												// TODO
												//setNoNcValue(columnScreen);
												//setEdgeValue(columnScreen);
											}
										if (!isRoutine)
											{
												setInputTagAndValue(columnScreen);
											}
										dispose();
										columnScreen.repaint();
										columnScreen.apply();
									}
							}
						else
							{//
								JOptionPane.showMessageDialog(submit, "Please Select Coil Type");
							}
					});
			}
			
		private void addCancelToScreen(int x)
			{
				if (isLoadCoil())
					{
						cancel.setBounds(X2, Y * x + 20, WIDTH, HEIGHT);
					}
				else if (columnScreen.getTemp().equals(CoilType.OUTPUT))
					{
						cancel.setBounds(X2, Y * x + 20, WIDTH, HEIGHT);
					}
				else if (columnScreen.getTemp().equals(CoilType.ROUTINE))
					{
						cancel.setBounds(X2, Y * x + 20, WIDTH, HEIGHT);
					}
				panel.add(cancel);
				cancel.addActionListener(event -> dispose());
			}
			
		private void invokeFrame(java.awt.Dimension screenDimension)
			{
				panel.setPreferredSize(screenDimension);
				panel.setLayout(null);
				getContentPane().add(scrollableConfigurationScreen);
				Dimension dimension = Application.calculateCenterAlignment(getPreferredSize());
				setLocation(dimension.getX(), dimension.getY());
				setPreferredSize(screenDimension);
				pack();
				setVisible(true);
			}
			
		private void setInputTagAndValue(ColumnScreen columnScreen)
			{
				InputType inputType = null;
				String optionType = "";
				if (input.isSelected())
					{
						inputType = InputType.INPUT;
						optionType = " I / ";
					}
				else if (flag.isSelected())
					{
						inputType = InputType.FLAG;
						optionType = " F / ";
					}
				else if (word.isSelected())
					{
						inputType = InputType.WORD;
						optionType = " D / ";
					}
				else if (output.isSelected())
					{
						inputType = InputType.OUTPUT;
						optionType = " O / ";
					}
				columnScreen.setInputType(inputType);
				columnScreen.setValue(value.getValue().toString());
				String valueLabel = optionType + columnScreen.getValue();
				columnScreen.getValueLabel().setText(valueLabel);
				String tag = (tagValue.getText() == null || tagValue.getText().trim().length() == 0) ? "" : tagValue.getText();
				columnScreen.setTag(tag);
			}
			
		private InputType findInputType()
			{
				if (input.isSelected())
					{
						return InputType.INPUT;
						
					}
				else if (flag.isSelected())
					{
						return InputType.FLAG;
					}
				else if (word.isSelected())
					{
						return InputType.WORD;
					}
				else if (output.isSelected())
					{
						return InputType.OUTPUT;
					}
				return null;
			}
			
		private void setCoilSignalType(ColumnScreen columnScreen)
			{
				Signal signal = null;
				if (NO.isSelected())
					{
						signal = Signal.NO;
					}
				else if (NC.isSelected())
					{
						signal = Signal.NC;
					}
					
				if (SET != null && SET.isSelected())
					{
						//	signal = Signal.SET;
					}
				else if (RESET != null && RESET.isSelected())
					{
						//	signal = Signal.RESET;
					}
				columnScreen.setSignal(signal);
			}
			
		private void setDefaultInputOption()
			{
				InputType inputType = columnScreen.getInputType();
				if (inputType != null)
					{
						switch (inputType)
							{
								case FLAG:
									{
										input.setSelected(false);
										flag.setSelected(true);
										output.setSelected(false);
										word.setSelected(false);
										break;
									}
								case INPUT:
									{
										input.setSelected(true);
										flag.setSelected(false);
										output.setSelected(false);
										word.setSelected(false);
										break;
									}
								case OUTPUT:
									{
										input.setSelected(false);
										flag.setSelected(false);
										output.setSelected(true);
										word.setSelected(false);
										break;
									}
								case WORD:
									{
										input.setSelected(false);
										flag.setSelected(false);
										output.setSelected(false);
										word.setSelected(true);
										break;
									}
								/*
								 * default: { input.setSelected(true); flag.setSelected(false); output.setSelected(false); word.setSelected(false); break; }
								 */
							}
					}
			}
			
		private void setDefaultNoNcOption(ColumnScreen columnScreen)
			{
				/*
				NoNc nonc = columnScreen.getSignal();
				if (nonc != null)
					{
						switch (nonc)
							{
								case NC:
									{
										NO.setSelected(false);
										NC.setSelected(true);
										break;
									}
								case NO:
									{
										NO.setSelected(true);
										NC.setSelected(false);
										break;
									}
								
								 * default: { NO.setSelected(true); NC.setSelected(false); break; }
								 
							}
					}
				*/}
				
		private void setDefaultEdgeOption()
			{
				/*				Edge edge = columnScreen.getEdge();
								if (edge != null)
									{
										switch (edge)
											{
												case FALLING:
													{
														risingEdge.setSelected(false);
														fallingEdge.setSelected(true);
														break;
													}
												case RISING:
													{
														risingEdge.setSelected(true);
														fallingEdge.setSelected(false);
														break;
													}
											}
									}*/
			}
			
		private boolean isLoadCoil()
			{
				CoilType coilType = columnScreen.getTemp();
				return (coilType.equals(CoilType.LOAD) || coilType.equals(CoilType.PARALLEL) || coilType.equals(CoilType.LEFT_LINK) || coilType.equals(CoilType.RIGHT_LINK));
			}
			
	}