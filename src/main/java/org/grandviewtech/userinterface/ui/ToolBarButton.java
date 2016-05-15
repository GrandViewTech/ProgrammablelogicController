package org.grandviewtech.userinterface.ui;

import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JButton;

import org.grandviewtech.constants.PreferredDimension;

public class ToolBarButton extends JButton implements Serializable, PreferredDimension
	{
		
		private static final long serialVersionUID = -5924372108403206263L;
		
		public ToolBarButton()
			{
				setOpaque(false);
				setContentAreaFilled(false);
			}
			
		public ToolBarButton(Icon icon)
			{
				super(icon);
			}
			
		public ToolBarButton(String text, Icon icon)
			{
				super(text, icon);
			}
			
		public ToolBarButton(String text)
			{
				super(text);
			}
			
		/*@Override
		public Dimension getMaximumSize()
			{
				return TOOLBAR_BUTTON;
			}
			
		@Override
		public Dimension getMinimumSize()
			{
				return TOOLBAR_BUTTON;
			}*/
	}
