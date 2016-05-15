package org.grandviewtech.userinterface.helper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Line2D;

import javax.swing.border.AbstractBorder;

import org.grandviewtech.constants.PLCConstant;


public class CustomBorder extends AbstractBorder implements PLCConstant

{

	/**
	 * <br>
	 *
	 * @Author : puneetsharma <br>
	 * @createdDate : 22-Feb-2016 <br>
	 * @createdTime : 12:21:30 am <br>
	 * @Description :
	 */
	private static final long	serialVersionUID	= 2652904055398847506L;
	private Color				borderColour		= Color.red;
	private int					gap;

	public CustomBorder()
		{
			gap = 0;
		}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
		{
			super.paintBorder(c, g, x, y, width, height);
			Graphics2D g2d = null;
			if (g instanceof Graphics2D)
				{
					g2d = (Graphics2D) g;
					g2d.setColor(borderColour);
					float xc = width - 1;
					g2d.draw(new Line2D.Float(xc, (PLCConstant.SECTION_HEIGHT / 2) - PLCConstant.CUSTOM_BORDER_HEIGTH_OFFSET, xc, (PLCConstant.SECTION_HEIGHT / 2) + PLCConstant.CUSTOM_BORDER_HEIGTH_OFFSET));
				}
		}

	@Override
	public Insets getBorderInsets(Component c)
		{
			return (getBorderInsets(c, new Insets(gap, gap, gap, gap)));
		}

	@Override
	public Insets getBorderInsets(Component c, Insets insets)
		{
			insets.left = insets.top = insets.right = insets.bottom = gap;
			return insets;
		}

	@Override
	public boolean isBorderOpaque()
		{
			return true;
		}

}
