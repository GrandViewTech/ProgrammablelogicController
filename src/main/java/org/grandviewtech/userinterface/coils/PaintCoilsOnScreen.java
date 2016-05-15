package org.grandviewtech.userinterface.coils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;

import org.grandviewtech.constants.Coils;
import org.grandviewtech.constants.PLCConstant;
import org.grandviewtech.entity.bo.Screen;

public class PaintCoilsOnScreen implements PLCConstant
	{
		final static Screen SCREEN = Screen.getInstance();
		
		public static void paint(Graphics graphics, String coil, boolean paintDefault)
			{
				if (paintDefault)
					{
						paintDefault(graphics);
					}
				else
					{
						selectRigthDragOption(graphics, coil);
					}
			}
			
		private static void selectRigthDragOption(Graphics graphics, String coil)
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
								configureOutputCoil(graphics);
								break;
							}
						case Coils.LOAD:
							{
								//Output.configureCoil(this);
								break;
							}
					}
			}
			
		private static void configureOutputCoil(Graphics graphics)
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
