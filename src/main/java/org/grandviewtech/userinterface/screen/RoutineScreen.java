package org.grandviewtech.userinterface.screen;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;

import org.grandviewtech.constants.CustomDimension;
import org.grandviewtech.entity.helper.Dimension;
import org.grandviewtech.runner.Application;
import org.grandviewtech.userinterface.listeners.DocumentSizeFilter;

public class RoutineScreen extends JFrame
	{
		private static final long	serialVersionUID	= -7808536714907991917L;
		
		private static int			maxLength			= 500;
		
		private static int			maxNameLength		= 80;
		
		private JLabel				functionLabel		= new JLabel("Function : ");
		
		private JLabel				descriptionLabel	= new JLabel("Descrition : ");
		
		private JLabel				nameLabel			= new JLabel("name : ");
		
		private JLabel				nameCounterLabel	= new JLabel(maxNameLength + " characters remaining");
		
		private JTextArea			nameTextField		= new JTextArea(2, 100);
		
		private JButton				submit				= new JButton("Add");
		
		private JButton				cancel				= new JButton("Cancel");
		
		private JTextArea			functionTextArea	= new JTextArea(20, 1000);
		
		private JTextArea			descriptionTextArea	= new JTextArea(3, 500);
		
		private JPanel				jpanel				= new JPanel();
		
		private JLabel				remainingLabel		= new JLabel("" + maxLength + " characters remaining");
		
		public void init()
			{
				setTitle("Add Routine");
				int x = 20;
				int y = 20;
				addName(x, y);
				//addDescription();
				addFunction(x, y = y + 100);
				addSubmitToScreen(x, y = y + 400);
				addCancel(x + 150, y);
				invokeFrame();
			}
			
		private void invokeFrame()
			{
				jpanel.setPreferredSize(CustomDimension.ROUTINE_COMMENT_SCREEN);
				jpanel.setLayout(null);
				add(jpanel);
				Dimension dimension = Application.calculateCenterAlignment(getPreferredSize());
				setLocation(dimension.getX(), dimension.getY());
				setPreferredSize(CustomDimension.ROUTINE_COMMENT_SCREEN);
				pack();
				setVisible(true);
			}
			
		private void addName(int x, int y)
			{
				nameLabel.setBounds(x, y, 100, 25);
				// JTextField Configuration
				//nameTextField.setPreferredSize(new java.awt.Dimension(100, 25));
				nameTextField.setBounds(x + 80, y, 600, 20);
				configure(nameTextField, nameCounterLabel, maxNameLength, true);
				// Name Counter Label
				//nameCounterLabel.setBounds(x + 80, y + scrollPane.getHeight(), 325, 25);
				jpanel.add(nameLabel);
				jpanel.add(nameTextField);
				jpanel.add(nameCounterLabel);
			}
			
		private void addDescription(int x, int y)
			{
				JScrollPane scrollPane = new JScrollPane(descriptionTextArea);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				descriptionLabel.setBounds(x, y, 100, 25);
				scrollPane.setBounds(x, y, 600, 20);
				jpanel.add(descriptionLabel);
				jpanel.add(scrollPane);
			}
			
		private void addFunction(int x, int y)
			{
				JScrollPane scrollPane = new JScrollPane(functionTextArea);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				functionLabel.setBounds(x, y, 100, 25);
				jpanel.add(functionLabel);
				// function block
				scrollPane.setBounds(x + 80, y, 600, 200);
				jpanel.add(scrollPane);
				// Remaining Block
				remainingLabel.setBounds(x, y + scrollPane.getHeight(), 325, 25);
				jpanel.add(remainingLabel);
				//	configureTextArea();
				configure(functionTextArea, remainingLabel, maxLength, false);
			}
			
		private void addCancel(int x, int y)
			{
				cancel.setBounds(x, y, 100, 25);
				cancel.addActionListener(event ->
					{
						dispose();
					});
				jpanel.add(cancel);
			}
			
		private void addSubmitToScreen(int x, int y)
			{
				submit.setBounds(x, y, 100, 25);
				JFrame frame = this;
				submit.addActionListener(event ->
					{
						String comment = functionTextArea.getText();
						//rung.setComment(comment);
						JOptionPane optionPane = new JOptionPane("Comment submitted Successfully", JOptionPane.INFORMATION_MESSAGE);
						JDialog dialog = optionPane.createDialog(null, "Rung Comment");
						dialog.setModal(false);
						dialog.setVisible(true);
						// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html#stayup
						//CustomToolBar.setRungComment(rung.getRowNumber(), comment);
						Timer timer = new Timer(600, timerEvent ->
							{
								dialog.setVisible(false);
								dialog.dispose();
							});
						timer.start();
						frame.dispose();
					});
				jpanel.add(submit);
			}
			
		private void configure(JTextArea component, JLabel countLabel, int maxLength, boolean remaining)
			{
				DefaultStyledDocument defaultStyledDocument = new DefaultStyledDocument();
				defaultStyledDocument.setDocumentFilter(new DocumentSizeFilter(maxLength));
				defaultStyledDocument.addDocumentListener(new DocumentListener()
					{
						@Override
						public void changedUpdate(DocumentEvent e)
							{
								count(countLabel, maxLength, defaultStyledDocument.getLength(), remaining);
							}
							
						@Override
						public void insertUpdate(DocumentEvent e)
							{
								count(countLabel, maxLength, defaultStyledDocument.getLength(), remaining);
							}
							
						@Override
						public void removeUpdate(DocumentEvent e)
							{
								count(countLabel, maxLength, defaultStyledDocument.getLength(), remaining);
							}
					});
				component.setDocument(defaultStyledDocument);
				component.setWrapStyleWord(true);
				component.setLineWrap(true);
				
				String comment = component.getText();
				if (comment != null && comment.trim().length() > 0)
					{
						count(countLabel, maxLength, comment.length(), remaining);
						component.setText(comment);
					}
			}
			
		private void count(JLabel countLabel, int maxLength, int textLength, boolean remaining)
			{
				String count = (remaining) ? (maxLength - textLength) + " characters remaining." : textLength + " characters.";
				countLabel.setText(count);
			}
	}
