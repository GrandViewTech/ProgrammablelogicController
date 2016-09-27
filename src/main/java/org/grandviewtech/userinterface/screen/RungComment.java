package org.grandviewtech.userinterface.screen;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;

import org.grandviewtech.constants.PreferredDimension;
import org.grandviewtech.entity.helper.Dimension;
import org.grandviewtech.runner.Application;
import org.grandviewtech.userinterface.listeners.DocumentSizeFilter;
import org.grandviewtech.userinterface.misc.CustomToolBar;

public class RungComment extends JFrame implements PreferredDimension
	{
		private static final long		serialVersionUID	= -920843301851468236L;
		
		private static int				maxLength			= 500;
		private JLabel					commentLabel		= new JLabel("Comment : ");
		
		private JButton					submit				= new JButton("Add");
		
		private JButton					cancel				= new JButton("Cancel");
		
		private JTextArea				textArea			= new JTextArea(5, 100);
		
		private JPanel					jpanel				= new JPanel();
		
		private JLabel					remainingLabel		= new JLabel("" + maxLength + " characters remaining");
		
		private DefaultStyledDocument	defaultStyledDocument;
		
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
				jpanel.setPreferredSize(RUNG_COMMENT_SCREEN);
				jpanel.setLayout(null);
				add(jpanel);
				Dimension dimension = Application.calculateCenterAlignment(getPreferredSize());
				setLocation(dimension.getX(), dimension.getY());
				setPreferredSize(RUNG_COMMENT_SCREEN);
				pack();
				setVisible(true);
			}
			
		private void addComment(Rung rung)
			{
				String comment = rung.getComment();
				if ( comment != null && comment.trim().length() > 0 )
					{
						remainingLabel.setText((maxLength - textArea.getText().length()) + " characters remaining");
						textArea.setText(comment);
					}
				commentLabel.setBounds(20, 10, 100, 25);
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
				textArea.setBounds(150, 10, 200, 100);
				jpanel.add(textArea);
			}
			
		private void addRemainingLabel()
			{
				remainingLabel.setBounds(20, 120, 325, 25);
				jpanel.add(remainingLabel);
			}
			
		private void addCancel()
			{
				cancel.setBounds(150, 150, 100, 25);
				cancel.addActionListener(event -> {
					dispose();
				});
				jpanel.add(cancel);
			}
			
		private void addSubmitToScreen(Rung rung)
			{
				submit.setBounds(20, 150, 100, 25);
				JFrame frame = this;
				submit.addActionListener(event -> {
					String comment = textArea.getText();
					rung.setComment(comment);
					JOptionPane.showMessageDialog(frame, "Comment Successfully Added");
					CustomToolBar.setRungComment(rung.getRowNumber(), comment);
					frame.dispose();
				});
				jpanel.add(submit);
			}
			
		private void updateCount()
			{
				remainingLabel.setText((maxLength - defaultStyledDocument.getLength()) + " characters remaining");
			}
	}
