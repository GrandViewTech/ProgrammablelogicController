package org.grandviewtech.userinterface.coils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;

import org.grandviewtech.constants.Coils;
import org.grandviewtech.constants.Fonts;
import org.grandviewtech.constants.PLCConstant;
import org.grandviewtech.entity.bo.Screen;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class PaintCoilsOnScreen implements PLCConstant, Fonts
	{
		final static Screen SCREEN = Screen.getInstance();
		
		public static void paint(ColumnScreen columnScreen, Graphics graphics)
			{
				String coil = columnScreen.getCoil();
				boolean paintDefault = columnScreen.isPaintDefault();
				if (paintDefault)
					{
						paintDefault(graphics);
					}
				else
					{
						selectRigthDragOption(columnScreen, graphics, coil);
					}
			}
			
		private static void selectRigthDragOption(ColumnScreen columnScreen, Graphics graphics, String coil)
			{
				switch (coil)
					{
						case Coils.LINE:
							{
								configureLineCoil(graphics);
								break;
							}
						case Coils.OUTPUT:
							{
								configureOutputCoil(columnScreen, graphics);
								break;
							}
						case Coils.LOAD:
							{
								configureLoadCoil(columnScreen, graphics);
								break;
							}
						case Coils.JUMP:
							{
								configureJumpCoil(columnScreen, graphics);
								break;
							}
					}
			}
			
		private static void configureJumpCoil(ColumnScreen columnScreen, Graphics graphics)
			{
				int offsetY = 8;
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, SECTION_HEIGHT / 2, (SECTION_WIDTH / 2) - 20, 1);
				((Graphics2D) graphics).draw(new QuadCurve2D.Double((SECTION_WIDTH / 2) - 15, (SECTION_HEIGHT / 2) - offsetY, (SECTION_WIDTH / 2) - 25, (SECTION_HEIGHT / 2), (SECTION_WIDTH / 2) - 15, (SECTION_HEIGHT / 2) + offsetY));
				((Graphics2D) graphics).draw(new QuadCurve2D.Double((SECTION_WIDTH / 2) + 15, (SECTION_HEIGHT / 2) - offsetY, (SECTION_WIDTH / 2) + 25, (SECTION_HEIGHT / 2), (SECTION_WIDTH / 2) + 15, (SECTION_HEIGHT / 2) + offsetY));
				graphics.fillRect((SECTION_WIDTH / 2) + 20, SECTION_HEIGHT / 2, SECTION_WIDTH, 1);
				columnScreen.getValueLabel().setText("JUMP");
				columnScreen.getValueLabel().setBounds((SECTION_WIDTH / 2) - 12, (SECTION_HEIGHT / 2) - 5, 60, 10);
				columnScreen.getValueLabel().setFont(font1);
			}
			
		private static void configureLoadCoil(ColumnScreen columnScreen, Graphics graphics)
			{
				int offset = 5;
				graphics.drawLine(0, SECTION_HEIGHT / 2, (SECTION_WIDTH / 2) - offset, SECTION_HEIGHT / 2);
				graphics.drawLine((SECTION_WIDTH / 2) - offset, (SECTION_HEIGHT / 2) - offset, (SECTION_WIDTH / 2) - offset, (SECTION_HEIGHT / 2) + offset);
				graphics.drawLine((SECTION_WIDTH / 2) + offset, (SECTION_HEIGHT / 2) - offset, (SECTION_WIDTH / 2) + offset, (SECTION_HEIGHT / 2) + offset);
				graphics.drawLine((SECTION_WIDTH / 2) + offset, SECTION_HEIGHT / 2, (SECTION_WIDTH), SECTION_HEIGHT / 2);
			}
			
		private static void configureOutputCoil(ColumnScreen columnScreen, Graphics graphics)
			{
				int offsetY = 8;
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, SECTION_HEIGHT / 2, (SECTION_WIDTH / 2) - 10, 1);
				((Graphics2D) graphics).draw(new QuadCurve2D.Double((SECTION_WIDTH / 2) - 5, (SECTION_HEIGHT / 2) - offsetY, (SECTION_WIDTH / 2) - 15, (SECTION_HEIGHT / 2), (SECTION_WIDTH / 2) - 5, (SECTION_HEIGHT / 2) + offsetY));
				((Graphics2D) graphics).draw(new QuadCurve2D.Double((SECTION_WIDTH / 2) + 5, (SECTION_HEIGHT / 2) - offsetY, (SECTION_WIDTH / 2) + 15, (SECTION_HEIGHT / 2), (SECTION_WIDTH / 2) + 5, (SECTION_HEIGHT / 2) + offsetY));
				graphics.fillRect((SECTION_WIDTH / 2) + 10, SECTION_HEIGHT / 2, SECTION_WIDTH, 1);
			}
			
		private static void configureLineCoil(Graphics graphics)
			{
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, SECTION_HEIGHT / 2, SECTION_WIDTH, 1);
			}
			
		private static void paintDefault(Graphics graphics)
			{
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.fillRect(0, SECTION_HEIGHT / 2, SECTION_WIDTH, 1);
			}
			
	}
