package org.grandviewtech.userinterface.listeners;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.grandviewtech.userinterface.misc.ActionBot;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ColumnScreenFocusListener implements FocusListener
	{
		
		private ColumnScreen source;
		
		public ColumnScreenFocusListener(ColumnScreen source)
			{
				this.source = source;
			}
			
		@Override
		public void focusGained(FocusEvent event)
			{
				ActionBot.focusGained(source);
			}
			
		@Override
		public void focusLost(FocusEvent event)
			{
				ActionBot.focusLost(source);
			}
			
	}
