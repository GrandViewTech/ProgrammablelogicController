package org.grandviewtech.userinterface.screen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import org.apache.log4j.Logger;
import org.grandviewtech.constants.CustomDimension;
import org.grandviewtech.entity.helper.Dimension;
import org.grandviewtech.runner.Application;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class PreferenceScreen extends JFrame
	{
		private static org.apache.log4j.Logger	logger				= org.apache.log4j.Logger.getLogger(Application.class);
		
		private static final long				serialVersionUID	= -8037817505353170621L;
		final private static Logger				LOGGER				= Logger.getLogger(PreferenceScreen.class);
		final JPanel							panel				= new JPanel();
		
		private static NumberFormatter			numberFormatter		= null;
		
		public static Map<String, Integer>		preferences			= new LinkedHashMap<>();
		
		static
			{
				numberFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
				numberFormatter.setValueClass(Integer.class);
				numberFormatter.setAllowsInvalid(false);
				numberFormatter.setMinimum(0);
				try
					{
						// Reading Properties
						String file = "resources" + File.separator + "input" + File.separator + "preferences.properties";
						Resource resource = new FileSystemResource(file);
						if (resource.getFile().exists())
							{
								Properties properties = new Properties();
								properties.load(resource.getInputStream());
								for (Object object : properties.keySet())
									{
										String key = (String) object;
										Object value = properties.get(key);
										if (value != null)
											{
												put(key, new Integer((String) value));
											}
									}
							}
						else
							{
								File folder = new File("resources" + File.separator + "input");
								if (!folder.exists())
									{
										folder.mkdirs();
									}
								Properties properties = new Properties();
								resource.getFile().createNewFile();
								OutputStream outputStream = new FileOutputStream(file);
								// set the properties value
								properties.setProperty("minInput", "0");
								properties.setProperty("maxInput", "127");
								
								properties.setProperty("minOutput", "0");
								properties.setProperty("maxOutput", "127");
								
								properties.setProperty("minFlag", "0");
								properties.setProperty("maxFlag", "127");
								
								properties.setProperty("minWord", "0");
								properties.setProperty("maxWord", "4444");
								
								properties.store(outputStream, "Default Properites");
								outputStream.close();
								// properties.load(resource.getInputStream());
								for (Object object : properties.keySet())
									{
										String key = (String) object;
										Object value = properties.get(key);
										if (value != null)
											{
												put(key, new Integer((String) value));
											}
									}
							}
					}
				catch (IOException e)
					{
						logger.error(e.getLocalizedMessage(), e);
					}
			}
			
		// MIN & MAX
		private JLabel				min			= new JLabel("MIN :");
		private JLabel				max			= new JLabel("MAX :");
		// INPUT
		private JLabel				input		= new JLabel("INPUT :");
		private JFormattedTextField	minInput	= new JFormattedTextField(numberFormatter);
		private JFormattedTextField	maxInput	= new JFormattedTextField(numberFormatter);
		// OUTPUT
		private JLabel				output		= new JLabel("OUTPUT :");
		private JFormattedTextField	minOutput	= new JFormattedTextField(numberFormatter);
		private JFormattedTextField	maxOutput	= new JFormattedTextField(numberFormatter);
		// WORD
		private JLabel				word		= new JLabel("WORD :");
		private JFormattedTextField	minWord		= new JFormattedTextField(numberFormatter);
		private JFormattedTextField	maxWord		= new JFormattedTextField(numberFormatter);
		// FLAG
		private JLabel				flag		= new JLabel("FLAG :");
		private JFormattedTextField	minflag		= new JFormattedTextField(numberFormatter);
		private JFormattedTextField	maxflag		= new JFormattedTextField(numberFormatter);
		
		private JButton				submit		= new JButton("SUBMIT");
		private JButton				cancel		= new JButton("CANCEL");
		
		public void init()
			{
				LOGGER.info("INITIALIZING FRAME");
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				setTitle("Preference");
				panel.setPreferredSize(new java.awt.Dimension(400, 200));
				panel.setLayout(null);
				int height = 40;
				addMinMax();
				addInputPreference(height * 1);
				addWordPreference(height * 2);
				addFlagPreference(height * 3);
				addOutPutPreference(height * 4);
				addSubmitAndCancel(height * 5);
				populateData();
				invokeFrame();
			}
			
		private void addSubmitAndCancel(int height)
			{
				submit.setBounds(120, height, 80, 25);
				add(submit);
				submit.addActionListener(event ->
					{
						put("minInput", minInput.getValue());
						put("maxInput", maxInput.getValue());
						put("minFlag", minflag.getValue());
						put("maxFlag", maxflag.getValue());
						put("minWord", minWord.getValue());
						put("maxWord", maxWord.getValue());
						put("minOutput", minOutput.getValue());
						put("maxOutput", maxOutput.getValue());
						try
							{
								String file = "resources" + File.separator + "input" + File.separator + "preferences.properties";
								Resource resource = new FileSystemResource(file);
								Properties properties = new Properties();
								resource.getFile().createNewFile();
								OutputStream outputStream = new FileOutputStream(file);
								
								for (Map.Entry<String, Integer> entry : preferences.entrySet())
									{
										String key = entry.getKey();
										Integer value = entry.getValue();
										properties.setProperty(key, value.toString());
									}
								properties.store(outputStream, "Default Properites");
								outputStream.close();
								this.dispose();
							}
						catch (Exception exception)
							{
								logger.error(exception.getLocalizedMessage(), exception);
							}
					});
				cancel.setBounds(250, height, 80, 25);
				add(cancel);
				cancel.addActionListener(event ->
					{
						this.dispose();
					});
			}
			
		public static Integer get(String key)
			{
				key = key.trim().toLowerCase();
				return preferences.get(key);
			}
			
		public static void put(String key, Integer value)
			{
				key = key.trim().toLowerCase();
				preferences.put(key, value);
			}
			
		public static void put(String key, Object value)
			{
				key = key.trim().toLowerCase();
				if (value != null)
					{
						preferences.put(key, (Integer) value);
					}
			}
			
		private void populateData()
			{
				minInput.setValue(get("minInput"));
				maxInput.setValue(get("maxInput"));
				minOutput.setValue(get("minOutput"));
				maxOutput.setValue(get("maxOutput"));
				minflag.setValue(get("minFlag"));
				maxflag.setValue(get("maxFlag"));
				minWord.setValue(get("minWord"));
				maxWord.setValue(get("maxWord"));
			}
			
		public void addMinMax()
			{
				min.setBounds(140, 10, 80, 25);
				add(min);
				max.setBounds(260, 10, 80, 25);
				add(max);
			}
			
		public void addInputPreference(int height)
			{
				input.setBounds(10, height, 110, 25);
				add(input);
				minInput.setBounds(120, height, 80, 25);
				add(minInput);
				maxInput.setBounds(250, height, 80, 25);
				add(maxInput);
			}
			
		public void addWordPreference(int height)
			{
				
				word.setBounds(10, height, 110, 25);
				add(word);
				minWord.setBounds(120, height, 80, 25);
				add(minWord);
				maxWord.setBounds(250, height, 80, 25);
				add(maxWord);
			}
			
		public void addOutPutPreference(int height)
			{
				output.setBounds(10, height, 110, 25);
				add(output);
				minOutput.setBounds(120, height, 80, 25);
				add(minOutput);
				maxOutput.setBounds(250, height, 80, 25);
				add(maxOutput);
			}
			
		public void addFlagPreference(int height)
			{
				flag.setBounds(10, height, 110, 25);
				add(flag);
				minflag.setBounds(120, height, 80, 25);
				add(minflag);
				maxflag.setBounds(250, height, 80, 25);
				add(maxflag);
			}
			
		private void invokeFrame()
			{
				// add(panel);
				setLayout(null);
				Dimension dimension = Application.calculateCenterAlignment(getPreferredSize());
				setLocation(dimension.getX(), dimension.getY());
				setPreferredSize(CustomDimension.PREFERENCE_SCREEN);
				pack();
				setVisible(true);
			}
	}
