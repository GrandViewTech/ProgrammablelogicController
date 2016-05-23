package org.grandviewtech.userinterface.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.grandviewtech.constants.Icons;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.screen.ColumnConfigurationScreen;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class SettingsMouseClickListener implements MouseListener, Icons
	{
		final static Screen		SCREEN	= Screen.getInstance();
		private ColumnScreen	columnScreen;
		//private JLabel			setting;
		
		public SettingsMouseClickListener(ColumnScreen columnScreen)
			{
				this.columnScreen = columnScreen;
			}
			
		//private JPopupMenu popUpMenu = new JPopupMenu();
		
		@Override
		public void mouseClicked(MouseEvent mouseEvent)
			{
				ColumnConfigurationScreen columnConfigurationScreen =new ColumnConfigurationScreen();
				columnConfigurationScreen.initiateInstance(columnScreen);
				columnConfigurationScreen.requestFocusInWindow();
			}
			
		@Override
		public void mousePressed(MouseEvent mouseEvent)
			{
			}
			
		@Override
		public void mouseReleased(MouseEvent mouseEvent)
			{
			}
			
		@Override
		public void mouseEntered(MouseEvent mouseEvent)
			{
			}
			
		@Override
		public void mouseExited(MouseEvent mouseEvent)
			{
			}
			
		/*private void addPopUpMenu()
			{
				edit();
				delete();
				popUpMenu.show(setting, setting.getX() - 10, setting.getY() + 5);
			}
		
		private void edit()
			{
				JMenuItem menuItem = new JMenuItem("Edit");
				menuItem.setIcon(EDIT);
				menuItem.addActionListener(event ->
					{
						ColumnConfigurationScreen columnConfigurationScreen = ColumnConfigurationScreen.getInstance();
						columnConfigurationScreen.requestFocusInWindow();
						columnConfigurationScreen.initiateInstance(columnScreen);
					});
				popUpMenu.add(menuItem);
			}
			
		private void delete()
			{
				JMenuItem menuItem = new JMenuItem("delete");
				menuItem.setIcon(DELETE);
				popUpMenu.add(menuItem);
			}*/
	}
