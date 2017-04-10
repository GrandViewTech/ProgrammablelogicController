package com.grandviewtech.application.entity.component.screen;

import java.awt.Container;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.grandviewtech.application.entity.bo.Section;
import com.grandviewtech.application.entity.bo.Session;
import com.grandviewtech.application.entity.bo.SessionHolder;

public class CompilerScreen
	{
		private Session		session;
		private Set<String>	usedCoiled	= new LinkedHashSet<String>();
										
		public CompilerScreen(Session session)
			{
				this.session = session;
			}
			
		public void compile()
			{
				if (session != null)
					{
						List<Section> sections = session.getSectionList();
						Collections.sort(sections, new Comparator<Section>()
							{
								@Override
								public int compare(Section section1, Section section2)
									{
										int id1 = section1.getCoilId();
										int id2 = section2.getCoilId();
										if (id1 < id2)
											{
												return -1;
											}
										else if (id1 == id2)
											{
												return 0;
											}
										else
											{
												return 1;
											}
									}
							});
						Iterator<Section> iterator = sections.iterator();
						StringBuffer stringBuffer = new StringBuffer();
						while (iterator.hasNext())
							{
								Section section = iterator.next();
								String coildId = "" + section.getCoilId();
								if ((usedCoiled.contains(coildId) == false) && (section.isBlank() == false))
									{
										switch (section.getSelectedCoil())
											{
												case LOAD:
													{
														ADD(stringBuffer, section.getValue());
														break;
													}
												case ROUTINE:
													{
														ROUTINE(stringBuffer, section.getRoutineName());
													}
												default:
													{
														break;
													}
											}
									}
							}
						initializeCompilerEditor(stringBuffer);
						writeASMFile(session.getSessionId() + ".asm", stringBuffer);
					}
			}
			
		public void ROUTINE(StringBuffer stringBuffer, String routineName)
			{
				if ((routineName == null) || (routineName.trim().length() == 0))
					{
						return;
					}
				File file = new File(SessionHolder.getRoutineFilePath(routineName));
				switch (SessionHolder.getRoutineExtension(routineName).toUpperCase())
					{
						case "DOC":
						case "DOCX":
							{
								try
									{
										//										InputStream inputStream = new FileInputStream(file);
										//										//XwpfDocument xwpfDocument = new XWPFDocument(inputStream);
										//										XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(xwpfDocument);
										//										System.out.println(xwpfWordExtractor.getText());
										//										if (xwpfWordExtractor != null)
										//											{
										//												xwpfWordExtractor.close();
										//											}
									}
								catch (Exception exception)
									{
										exception.printStackTrace();
									}
								break;
							}
						case "TXT":
							{
								break;
							}
					}
			}
			
		public void ADD(StringBuffer stringBuffer, int value)
			{
				int input = 512 - value;
				String param1 = intTohex(input / 8);
				String param2 = intTohex(input % 8);
				stringBuffer.append("MOV  DTPR , #(RLY512_519)+" + param1 + "\n");
				stringBuffer.append("MOV X A,@DPTR \n");
				stringBuffer.append("MOV C, ACC." + param2 + "\n");
			}
			
		private String intTohex(int integer)
			{
				return Integer.toHexString(integer);
			}
			
		private void writeASMFile(String fileName, StringBuffer stringBuffer)
			{
				
				FileWriter writer;
				try
					{
						writer = new FileWriter(new File(fileName));
						writer.write(stringBuffer.toString());
						writer.flush();
						writer.close();
					}
				catch (IOException e)
					{
						e.printStackTrace();
					}
					
			}
			
		private void initializeCompilerEditor(StringBuffer stringBuffer)
			{
				System.out.println("Hello Editor");
				JFrame frame = new JFrame("Code Editor");
				JScrollPane scrollPane = new JScrollPane();
				JEditorPane editorPane = new JEditorPane();
				editorPane.setText(stringBuffer.toString());
				scrollPane.setViewportView(editorPane);
				Container container = frame.getContentPane();
				javax.swing.GroupLayout layout = new javax.swing.GroupLayout(container);
				container.setLayout(layout);
				layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 501, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(20, Short.MAX_VALUE)));
				layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE).addContainerGap()));
				frame.pack();
				frame.setVisible(true);
				frame.validate();
				frame.repaint();
			}
			
	}
