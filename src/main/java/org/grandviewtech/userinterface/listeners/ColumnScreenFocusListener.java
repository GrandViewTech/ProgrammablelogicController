package org.grandviewtech.userinterface.listeners;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;

import org.grandviewtech.userinterface.misc.ActionBot;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class ColumnScreenFocusListener implements FocusListener
	{
		
		private ColumnScreen	source;
		
		private JLabel			setting;
		
		public ColumnScreenFocusListener(ColumnScreen source)
			{
				this.source = source;
				this.setting = source.getSetting();
			}
			
		@Override
		public void focusGained(FocusEvent event)
			{
				if (source.isPaintDefault() == false)
					{
						source.add(setting);
					}
				ActionBot.focusGained(source);
			}
			
		@Override
		public void focusLost(FocusEvent event)
			{
				if (source.isPaintDefault() == false)
					{
						source.remove(setting);
					}
				ActionBot.focusLost(source);
			}
			
	}
