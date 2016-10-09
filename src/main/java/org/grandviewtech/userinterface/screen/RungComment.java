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
import org.grandviewtech.userinterface.misc.CustomToolBar;

public class RungComment extends JFrame
	{
		private static final long		serialVersionUID	= -920843301851468236L;
		
		private static int				maxLength			= 500;
		private JLabel					commentLabel		= new JLabel("Comment : ");
		
		private JButton					submit				= new JButton("Add");
		
		private JButton					cancel				= new JButton("Cancel");
		
		private JTextArea				textArea			= new JTextArea(5, 100);
		
		private JPanel					jpanel				= new JPanel();
		
		private JScrollPane				scrollPane			= new JScrollPane(textArea);
		
		private JLabel					remainingLabel		= new JLabel("" + maxLength + " characters remaining");
		
		private DefaultStyledDocument	defaultStyledDocument;
		
		public RungComment()
			{
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			}
			
		public void initiateRungComment(Rung rung)
			{
				setTitle("Add Comment For Rung : " + rung.getRowNumber());
				addComment(rung);
				addRemainingLabel();
				addSubmitToScreen(rung);
				addCancel();
				invokeFrame();
			}
			
		private void invokeFrame()
			{
				jpanel.setPreferredSize(CustomDimension.RUNG_COMMENT_SCREEN);
				jpanel.setLayout(null);
				add(jpanel);
				Dimension dimension = Application.calculateCenterAlignment(getPreferredSize());
				setLocation(dimension.getX(), dimension.getY());
				setPreferredSize(CustomDimension.RUNG_COMMENT_SCREEN);
				pack();
				setVisible(true);
			}
			
		private void addComment(Rung rung)
			{
				commentLabel.setBounds(20, 10, 100, 25);
				configureTextArea(rung);
			}
			
		private void configureTextArea(Rung rung)
			{
				jpanel.add(commentLabel);
				defaultStyledDocument = new DefaultStyledDocument();
				defaultStyledDocument.setDocumentFilter(new DocumentSizeFilter(500));
				defaultStyledDocument.addDocumentListener(new DocumentListener()
					{
						@Override
						public void changedUpdate(DocumentEvent e)
							{
								updateCount();
							}
							
						@Override
						public void insertUpdate(DocumentEvent e)
							{
								updateCount();
							}
							
						@Override
						public void removeUpdate(DocumentEvent e)
							{
								updateCount();
							}
					});
				textArea.setDocument(defaultStyledDocument);
				textArea.setWrapStyleWord(true);
				textArea.setLineWrap(true);
				
				String comment = rung.getComment();
				if (comment != null && comment.trim().length() > 0)
					{
						remainingLabel.setText((maxLength - comment.length()) + " characters remaining");
						textArea.setText(comment);
					}
				scrollPane.setBounds(150, 10, 200, 100);
				// scrollPane.add(textArea);
				// scrollPane.set
				jpanel.add(scrollPane);
			}
			
		private void addRemainingLabel()
			{
				remainingLabel.setBounds(20, 120, 325, 25);
				jpanel.add(remainingLabel);
			}
			
		private void addCancel()
			{
				cancel.setBounds(150, 150, 100, 25);
				cancel.addActionListener(event ->
					{
						dispose();
					});
				jpanel.add(cancel);
			}
			
		private void addSubmitToScreen(Rung rung)
			{
				submit.setBounds(20, 150, 100, 25);
				JFrame frame = this;
				submit.addActionListener(event ->
					{
						String comment = textArea.getText();
						rung.setComment(comment);
						JOptionPane optionPane = new JOptionPane("Comment submitted Successfully", JOptionPane.INFORMATION_MESSAGE);
						JDialog dialog = optionPane.createDialog(null, "Rung Comment");
						dialog.setModal(false);
						dialog.setVisible(true);
						// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html#stayup
						CustomToolBar.setRungComment(rung.getRowNumber(), comment);
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
			
		private void updateCount()
			{
				remainingLabel.setText((maxLength - defaultStyledDocument.getLength()) + " characters remaining");
			}
	}
