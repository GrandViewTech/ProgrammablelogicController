package org.grandviewtech.userinterface.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.helper.RowGenerator;

public class NewRowListener implements ActionListener
	{
		Screen		screen	= Screen.getInstance();
		private int	currentRungNumber;
		private int	newRungNumber;
		
		public NewRowListener(int currentRungNumber, int newRungNumber)
			{
				this.currentRungNumber = currentRungNumber;
				this.newRungNumber = newRungNumber;
			}
			
		
		private void action()
		{
			if (newRungNumber > currentRungNumber)
				{
					RowGenerator.regenerateRow(newRungNumber);
				}
		}


		@Override
		public void actionPerformed(ActionEvent e)
			{
				action();
			}
		
		
			
	}
