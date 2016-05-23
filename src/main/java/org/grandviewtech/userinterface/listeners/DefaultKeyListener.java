package org.grandviewtech.userinterface.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.grandviewtech.constants.Borders;
import org.grandviewtech.entity.bo.ClipBoard;

public class DefaultKeyListener implements KeyListener, Borders
	{
		@Override
		public void keyTyped(KeyEvent keyEvent)
			{
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(true);
					}
				else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					{
						actionOnEscape();
					}
			}
			
		@Override
		public void keyPressed(KeyEvent keyEvent)
			{
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(true);
					}
				else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					{
						actionOnEscape();
					}
			}
			
		@Override
		public void keyReleased(KeyEvent keyEvent)
			{
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL)
					{
						ClipBoard.setControlKeyActive(false);
					}
			}
			
		private void actionOnEscape()
			{
				ClipBoard.cutLabel.setBorder(null);
				ClipBoard.copyLabel.setBorder(null);
				ClipBoard.pasteLabel.setBorder(null);
				ClipBoard.cutLabel.setBorder(PADDED_DEFAULT);
				ClipBoard.copyLabel.setBorder(PADDED_DEFAULT);
				ClipBoard.pasteLabel.setBorder(PADDED_DEFAULT);
				ClipBoard.resetClipBoardColumnSelection();
			}
	}
