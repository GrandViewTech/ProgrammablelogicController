package org.grandviewtech.userinterface.screen;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

public class Sheet extends JPanel
	{
		private static final long	serialVersionUID	= 2259392011305568009L;
		
		public GridBagConstraints	gridBagConstraints	= new GridBagConstraints();
		
		public Sheet()
			{
				GridBagLayout gridBagLayout = new GridBagLayout();
				setLayout(gridBagLayout);
			}
			
	}
