package org.grandviewtech.service.system;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import org.grandviewtech.constants.CustomIcon;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.entity.enums.CoilType;
import org.grandviewtech.userinterface.screen.Sheet;

public class DragDropCursor
	{
		final private static Screen	SCREEN	= Screen.getInstance();
		private static Sheet		sheet	= SCREEN.getSheet();
		
		public static void setCursor4DragOption(String dragDrop)
			{
				
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				int x = sheet.getX();
				int y = sheet.getY();
				Point point = new Point(x, y);
				Image image = getCursorImage4DragAndDrop(dragDrop);
				if (image != null)
					{
						Cursor custom = toolkit.createCustomCursor(image, point, "sheet");
						sheet.setCursor(custom);
					}
				else
					{
						sheet.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
					
			}
			
		public static void setCursor4DropOption()
			{
				sheet.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			
		private static Image getCursorImage4DragAndDrop(String dragDrop)
			{
				CoilType coilType = CoilType.valueOf(dragDrop);
				if (coilType == null)
					{
						return null;
					}
				switch (coilType)
					{
						case DEFAULT:
							{
								return null;
							}
						case DELETE:
							{
								return CustomIcon.COIL_DELETE.getImage();
							}
						case END:
							{
								return CustomIcon.COIL_END.getImage();
							}
						case JUMP:
							{
								return CustomIcon.COIL_JUMP.getImage();
							}
						case LABEL:
							{
								return CustomIcon.COIL_LABEL.getImage();
							}
						case LEFT_LINK:
							{
								return CustomIcon.COIL_LEFT_LINK.getImage();
							}
						case LINE:
							{
								return CustomIcon.COIL_LINE.getImage();
							}
						case LOAD:
							{
								return CustomIcon.COIL_LOAD.getImage();
							}
						case OUTPUT:
							{
								return CustomIcon.COIL_OUTPUT.getImage();
							}
						case PARALLEL:
							{
								return CustomIcon.COIL_PARALLEL.getImage();
							}
						case RIGHT_LINK:
							{
								return CustomIcon.COIL_RIGHT_LINK.getImage();
							}
						case ROUTINE:
							{
								return CustomIcon.COIL_ROUTINE.getImage();
							}
						default:
							{
								return null;
							}
						
					}
			}
	}
