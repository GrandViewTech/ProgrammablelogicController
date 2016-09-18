package org.grandviewtech.entity.helper;

import java.io.Serializable;

public class Dimension implements Serializable
	{
		
		private static final long	serialVersionUID	= 4857691843212675040L;
		
		private Double				screenHeight		= 0.0;
		
		private Double				screenWidth			= 0.0;
		
		private Double				height				= 0.0;
		
		private Double				width				= 0.0;
		
		public Integer getX()
			{
				int screenBy2 = screenWidth.intValue() / 2;
				int compnentBy2 = width.intValue() / 2;
				int finalWidth = screenBy2 - compnentBy2;
				return finalWidth;
			}
			
		public Integer getY()
			{
				int screenBy2 = screenHeight.intValue() / 2;
				int compnentBy2 = height.intValue() / 2;
				int finalHeight = screenBy2 - compnentBy2;
				return finalHeight;
				
			}
			
		public Double getHeight()
			{
				return height;
			}
			
		public void setHeight(Double height)
			{
				this.height = height;
			}
			
		public Double getWidth()
			{
				return width;
			}
			
		public void setWidth(Double width)
			{
				this.width = width;
			}
			
		public Integer getIntegerHeight()
			{
				return height.intValue();
			}
			
		public Integer getIntegerWidth()
			{
				return width.intValue();
			}
			
		public Double getScreenHeight()
			{
				return screenHeight;
			}
			
		public void setScreenHeight(Double screenHeight)
			{
				this.screenHeight = screenHeight;
			}
			
		public Double getScreenWidth()
			{
				return screenWidth;
			}
			
		public void setScreenWidth(Double screenWidth)
			{
				this.screenWidth = screenWidth;
			}
			
	}
