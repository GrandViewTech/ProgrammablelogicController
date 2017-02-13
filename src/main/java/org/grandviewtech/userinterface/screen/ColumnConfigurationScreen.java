package org.grandviewtech.userinterface.screen;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

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

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.grandviewtech.constants.CustomDimension;
import org.grandviewtech.entity.bo.Routine;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.entity.enums.Edge;
import org.grandviewtech.entity.enums.InputType;
import org.grandviewtech.entity.enums.NoNc;
import org.grandviewtech.entity.helper.Dimension;
import org.grandviewtech.runner.Application;
import org.grandviewtech.service.runtime.user.useractivity.Activities;
import org.grandviewtech.service.runtime.user.useractivity.Activity;
import org.grandviewtech.service.searching.SearchEngine;
import org.grandviewtech.service.system.PropertyReader;
import org.grandviewtech.service.system.RoutineFileReader;

import com.thoughtworks.xstream.XStream;

public class ColumnConfigurationScreen extends JFrame
	{
		private static org.apache.log4j.Logger	logger							= org.apache.log4j.Logger.getLogger(ColumnConfigurationScreen.class);
		private static Activities				activities						= Activities.getInstance();
		final static Screen						SCREEN							= Screen.getInstance();
		private static final long				serialVersionUID				= 1L;
		
		private ButtonGroup						buttonGroup						= new ButtonGroup();
		
		private JRadioButton					input							= new JRadioButton("Input");
		
		private JRadioButton					flag							= new JRadioButton("Flag");
		
		private JRadioButton					word							= new JRadioButton("Word");
		
		private JRadioButton					output							= new JRadioButton("Output");
		
		private ButtonGroup						edgeButtonGroup					= new ButtonGroup();
		
		private JRadioButton					risingEdge						= new JRadioButton("Rising");
		
		private JRadioButton					fallingEdge						= new JRadioButton("Falling");
		
		private ButtonGroup						nc_noButtonGroup				= new ButtonGroup();
		
		private JRadioButton					NC								= new JRadioButton("NC");
		
		private JRadioButton					NO								= new JRadioButton("NO");
		
		private JPanel							panel							= new JPanel();
		
		private JLabel							edgeLabel						= new JLabel("Edge");
		
		private JLabel							ncnoLabel						= new JLabel("NC/NO");
		
		private JLabel							inputValue						= new JLabel("Value");
		
		private JTextField						value							= new JTextField();
		
		private JLabel							type							= new JLabel("Type");
		
		private JLabel							tagLabel						= new JLabel("Tag");
		
		private JTextField						tagValue						= new JTextField();
		
		private JButton							submit							= new JButton("Submit");
		
		private JButton							cancel							= new JButton("Canel");
		
		private JComboBox<String>				category						= new JComboBox<String>();
		
		private JComboBox<String>				routine							= new JComboBox<String>();
		
		private JScrollPane						scrollableConfigurationScreen	= null;
		
		private String							resourcePath					= PropertyReader.getProperties("resourcePath") + File.separator + PropertyReader.getProperties("routinePath");
		
		private Map<String, JTextField>			dataset							= new HashMap<String, JTextField>();
		private String							fileName						= "";
		private static final int				X1								= 10;
		private static final int				X2								= 150;
		private static final int				Y								= 30;
		private static final int				WIDTH							= 120;
		private static final int				RADIO_WIDTH						= 80;
		private static final int				HEIGHT							= 20;
		private static final int				CATEGORY_WIDTH					= (WIDTH * 2);
		
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
			
		public void initiateInstance(ColumnScreen columnScreen)
			{
				try
					{
						setAlwaysOnTop(true);
						setTitle(" Row : " + columnScreen.getRowNumber() + " | Column : " + columnScreen.getColumnNumber());
						if (columnScreen.getTemp().equals(CoilType.LOAD))
							{
								loadCoilConfiguration(columnScreen);
							}
						else if (columnScreen.getTemp().equals(CoilType.ROUTINE))
							{
								routineCoilConfiguration(columnScreen);
							}
						else if (columnScreen.getTemp().equals(CoilType.OUTPUT))
							{
								outputCoilConfiguration(columnScreen);
							}
					}
				catch (Exception exception)
					{
						logger.error(exception.getLocalizedMessage(), exception);
					}
			}
			
		private void routineCoilConfiguration(ColumnScreen columnScreen)
			{
				//loadCategory();
				//loadSubCategory();
				//addSubmitToScreen(7, columnScreen);
				//addCancelToScreen(7, columnScreen);
				routineList();
				invokeFrame(CustomDimension.ROUTINE_CONFIGURATION_SCREEN);
			}
			
		private void routineList()
			{
				Map<String, JTextField> inputFields = new LinkedHashMap<String, JTextField>();
				JLabel name = new JLabel();
				name.setText("Name      : ");
				name.setBounds(X1 + 170, Y - 5, 150, 25);
				panel.add(name);
				JLabel description = new JLabel();
				description.setText("Descrption : ");
				description.setBounds(X1 + 170, (Y - 5) + 25, 150, 25);
				panel.add(description);
				JTextArea descriptionTextArea = new JTextArea(3, 500);
				descriptionTextArea.setBounds(X1 + 250, (Y - 5) + 25, 600, 60);
				panel.add(descriptionTextArea);
				List<String> dataList = new ArrayList<>();
				for (File file : (new File(resourcePath)).listFiles())
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
				routineList.addListSelectionListener(new ListSelectionListener()
					{
						@Override
						public void valueChanged(ListSelectionEvent e)
							{
								if (!e.getValueIsAdjusting())
									{
										try
											{
												final List<String> selectedValuesList = routineList.getSelectedValuesList();
												if (selectedValuesList.size() > 0)
													{
														String selectedRoutineName = selectedValuesList.get(0);
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
														for (Map.Entry<Integer, String> input : routine.getInputs().entrySet())
															{
																String value = input.getValue();
																JLabel label = new JLabel("Input(" + input.getKey() + ") :");
																label.setName(value);
																JTextField inputTextField = new JTextField();
																inputTextField.setName(value);
																inputFields.put(inputTextField.getName(), inputTextField);
																label.setBounds(((counter % 2 != 0) ? X1 : X2 + 100) + 170, ((counter % 2 != 0) ? height = height + 25 : height), 150, 25);
																inputTextField.setBounds(((counter % 2 != 0) ? X1 : X2 + 100) + 250, height, 100, 25);
																inputTextField.addActionListener(action ->
					                                                {
						                                                routine.addValue(input.getKey(), inputTextField.getText());
					                                                });
																panel.add(label);
																panel.add(inputTextField);
																counter = counter + 1;
															}
														panel.repaint();
													}
											}
										catch (FileNotFoundException fileNotFoundException)
											{
												logger.error(fileNotFoundException.getLocalizedMessage(), fileNotFoundException);
											}
									}
							}
					});
				routineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				//routineList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
				JScrollPane scrollPane = new JScrollPane(routineList);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setBounds(X1, Y - 5, 150, 400);
				panel.add(scrollPane);
			}
			
		private void loadCategory()
			{
				category.removeAllItems();
				for (File file : (new File(resourcePath)).listFiles())
					{
						if (file.isFile() && FilenameUtils.getExtension(file.getName()).contains("xml"))
							{
								String name = FilenameUtils.getBaseName(file.getName());
								category.addItem(name);
							}
					}
				JLabel label = new JLabel("Category : ");
				label.setBounds(X1, Y - 5, WIDTH, HEIGHT);
				category.setBounds(X2 - 10, Y - 5, CATEGORY_WIDTH, HEIGHT);
				panel.add(label);
				panel.add(category);
				addChangeListernerForCategory();
			}
			
		private void addChangeListernerForRoutine()
			{
				routine.addActionListener(event ->
					{
						Object object = routine.getSelectedItem();
						String selectedRoutine = (object == null) ? "" : (((String) object).trim().length() == 0) ? "" : ((String) object).trim();
						activities.addActivity(new Activity("Routine : " + selectedRoutine + " Selected", Activity.Category.USER));
						routine.setToolTipText(selectedRoutine);
						if (selectedRoutine.trim().length() > 0)
							{
								String filePath = resourcePath + File.separator + (String) category.getSelectedItem() + File.separator + selectedRoutine;
								Object[] routineContent = RoutineFileReader.getRoutineCode(filePath);
								int x = 3;
								for (int i = 1; i <= (Integer) routineContent[1]; i++)
									{
										String keyword = "D" + i;
										JLabel dataLabel = new JLabel("D" + i);
										JTextField dataField = new JTextField();
										dataLabel.setBounds(X1, Y * (x + i), WIDTH, HEIGHT);
										dataField.setBounds(X2, Y * (x + i), WIDTH, HEIGHT);
										dataset.put(keyword, dataField);
										panel.add(dataLabel);
										panel.add(dataField);
									}
								revalidate();
								repaint();
							}
					});
			}
			
		private void addChangeListernerForCategory()
			{
				category.addActionListener(event ->
					{
						routine.removeAllItems();
						String categoryFileName = (String) category.getSelectedItem();
						loadSubCategory(categoryFileName);
					});
			}
			
		private void loadSubCategory(String categoryFileName)
			{
				File file = new File(resourcePath + File.separator + categoryFileName);
				String[] folder = file.list();
				if (folder != null)
					{
						for (String folderName : folder)
							{
								String fileName = StringUtils.split(folderName, ".")[0];
								routine.addItem(fileName);
							}
					}
			}
			
		private void loadSubCategory()
			{
				int x = 2;
				JLabel label = new JLabel("Routine : ");
				label.setBounds(X1, (Y - 5) * x, WIDTH, HEIGHT);
				routine.setBounds(X2 - 10, (Y - 5) * x, CATEGORY_WIDTH, HEIGHT);
				/*
				 * JLabel selectedSubCategory = new JLabel("");
				 * selectedSubCategory.setBounds(X1, (Y - 5) * (x + 2), WIDTH *
				 * 3, HEIGHT);
				 */
				panel.add(label);
				panel.add(routine);
				// panel.add(selectedSubCategory);
				addChangeListernerForRoutine();
			}
			
		private void outputCoilConfiguration(ColumnScreen columnScreen)
			{
				addInputValueToScreen(columnScreen);
				addInputOptionsToScreen(columnScreen);
				addTagToScreen(4, columnScreen);
				addSubmitToScreen(5, columnScreen);
				addCancelToScreen(5, columnScreen);
				invokeFrame(CustomDimension.OUTPUT_CONFIGURATION_SCREEN);
			}
			
		private void loadCoilConfiguration(ColumnScreen columnScreen)
			{
				addInputValueToScreen(columnScreen);
				addInputOptionsToScreen(columnScreen);
				addEdgeOptionToScreen(columnScreen);
				addNcNoOptionToScreen(columnScreen);
				addTagToScreen(6, columnScreen);
				addSubmitToScreen(7, columnScreen);
				addCancelToScreen(7, columnScreen);
				invokeFrame(CustomDimension.LOAD_CONFIGURATION_SCREEN);
			}
			
		public void addInputValueToScreen(ColumnScreen columnScreen)
			{
				inputValue.setBounds(X1, Y - 5, WIDTH, HEIGHT);
				value.setBounds(X2, Y - 5, WIDTH, HEIGHT);
				if (columnScreen.getValue() != null && columnScreen.getValue().length() > 0)
					{
						value.setText(columnScreen.getValue());
					}
				JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
				// separator.setBounds(X1, Y * 1 + (20),
				// CustomDimension.CONFIGURATION_SCREEN.width - (X1 * 4), 10);
				panel.add(inputValue);
				panel.add(value);
				// panel.add(separator);
			}
			
		public void addInputOptionsToScreen(ColumnScreen columnScreen)
			{
				
				if (columnScreen.getTemp().equals(CoilType.LOAD))
					{
						addRadioButtonsToInputButtonGroup();
						type.setBounds(X1, Y * 2, RADIO_WIDTH, HEIGHT);
						input.setBounds(X2, Y * 2, RADIO_WIDTH, HEIGHT);
						// input.setSelected(true);
						word.setBounds(X2 + (RADIO_WIDTH * 1), Y * 2, RADIO_WIDTH, HEIGHT);
						flag.setBounds(X2, Y * 3, RADIO_WIDTH, HEIGHT);
						output.setBounds(X2 + (RADIO_WIDTH * 1), Y * 3, RADIO_WIDTH, HEIGHT);
						JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
						// separator.setBounds(X1, Y * 3 + (20),
						// CustomDimension.CONFIGURATION_SCREEN.width - (X1 *
						// 4), 10);
						// setDefaultInputOption(1, columnScreen);
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
						JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
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
			
		public void addEdgeOptionToScreen(ColumnScreen columnScreen)
			{
				addRadioButtonsToEdgeButtonGroup();
				edgeLabel.setBounds(X1, Y * 4, RADIO_WIDTH, HEIGHT);
				risingEdge.setBounds(X2, Y * 4, RADIO_WIDTH, HEIGHT);
				fallingEdge.setBounds(X2 + (RADIO_WIDTH * 1), Y * 4, RADIO_WIDTH, HEIGHT);
				JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
				// separator.setBounds(X1, Y * 4 + (20),
				// CustomDimension.CONFIGURATION_SCREEN.width - (X1 * 4), 10);
				// setDefaultEdgeOption(columnScreen);
				panel.add(edgeLabel);
				panel.add(risingEdge);
				panel.add(fallingEdge);
				// panel.add(separator);
			}
			
		// NC
		public void addRadioButtonsToNoNcButtonGroup()
			{
				nc_noButtonGroup.add(NC);
				nc_noButtonGroup.add(NO);
				addMnemonicToNoNcRadioButton();
			}
			
		public void addMnemonicToNoNcRadioButton()
			{
				NO.setMnemonic(KeyEvent.VK_N);
				NC.setMnemonic(KeyEvent.VK_C);
			}
			
		public void addNcNoOptionToScreen(ColumnScreen columnScreen)
			{
				addRadioButtonsToNoNcButtonGroup();
				ncnoLabel.setBounds(X1, Y * 5, RADIO_WIDTH, HEIGHT);
				NC.setBounds(X2, Y * 5, RADIO_WIDTH, HEIGHT);
				NO.setBounds(X2 + (RADIO_WIDTH * 1), Y * 5, RADIO_WIDTH, HEIGHT);
				JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
				// separator.setBounds(X1, Y * 5 + (20),
				// CustomDimension.CONFIGURATION_SCREEN.width - (X1 * 4), 10);
				// setDefaultNoNcOption(columnScreen);
				panel.add(ncnoLabel);
				panel.add(NC);
				panel.add(NO);
				// panel.add(separator);
			}
			
		public void addTagToScreen(int x, ColumnScreen columnScreen)
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
				JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
				// separator.setBounds(X1, Y * 6 + (20),
				// CustomDimension.CONFIGURATION_SCREEN.width - (X1 * 4), 10);
				panel.add(tagLabel);
				panel.add(tagValue);
				// panel.add(separator);
			}
			
		private void addSubmitToScreen(int x, ColumnScreen columnScreen)
			{
				if (columnScreen.getTemp().equals(CoilType.LOAD))
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
				submit.addActionListener(event ->
					{
						
						if (input.isSelected() || flag.isSelected() || output.isSelected() || word.isSelected())
							{
								
								boolean isRoutine = false;
								if (columnScreen.getTemp().equals(CoilType.LOAD))
									{
										setNoNcValue(columnScreen);
										setEdgeValue(columnScreen);
									}
								else if (columnScreen.getTemp().equals(CoilType.ROUTINE))
									{
										isRoutine = true;
									}
								if (!isRoutine)
									{
										setInputTagAndValue(columnScreen);
										SearchEngine.index(columnScreen);
									}
								dispose();
								columnScreen.repaint();
								columnScreen.apply();
							}
						else
							{//
								JOptionPane.showMessageDialog(submit, "Please Select Coil Type");
							}
					});
			}
			
		private void addCancelToScreen(int x, ColumnScreen columnScreen)
			{
				if (columnScreen.getTemp().equals(CoilType.LOAD))
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
				cancel.addActionListener(event ->
					{
						dispose();
					});
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
				columnScreen.setValue(value.getText());
				String valueLabel = optionType + columnScreen.getValue();
				columnScreen.getValueLabel().setText(valueLabel);
				String tag = (tagValue.getText() == null || tagValue.getText().trim().length() == 0) ? "" : tagValue.getText();
				columnScreen.setTag(tag);
			}
			
		private void setNoNcValue(ColumnScreen columnScreen)
			{
				NoNc nonc = null;
				if (NO.isSelected())
					{
						nonc = NoNc.NO;
					}
				else if (NC.isSelected())
					{
						nonc = NoNc.NC;
					}
				columnScreen.setNonc(nonc);
			}
			
		private void setEdgeValue(ColumnScreen columnScreen)
			{
				Edge edge = null;
				if (risingEdge.isSelected())
					{
						edge = Edge.RISING;
					}
				else if (fallingEdge.isSelected())
					{
						edge = Edge.FALLING;
					}
				columnScreen.setEdge(edge);
			}
			
		private void setDefaultInputOption(int i, ColumnScreen columnScreen)
			{
				InputType inputType = columnScreen.getInputType();
				if (inputType != null)
					{
						/*
						 * if (i == 1) { input.setSelected(true);
						 * flag.setSelected(false); } else {
						 * input.setSelected(false); flag.setSelected(true); }
						 * 
						 * output.setSelected(false); word.setSelected(false); }
						 * else {
						 */
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
								default:
									{
										input.setSelected(true);
										flag.setSelected(false);
										output.setSelected(false);
										word.setSelected(false);
										break;
									}
							}
					}
			}
			
		private void setDefaultNoNcOption(ColumnScreen columnScreen)
			{
				NoNc nonc = columnScreen.getNonc();
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
								default:
									{
										NO.setSelected(true);
										NC.setSelected(false);
										break;
									}
							}
					}
			}
			
		private void setDefaultEdgeOption(ColumnScreen columnScreen)
			{
				Edge edge = columnScreen.getEdge();
				if (edge != null)
					
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
							default:
								{
									risingEdge.setSelected(true);
									fallingEdge.setSelected(false);
									break;
								}
						}
			}
	}
