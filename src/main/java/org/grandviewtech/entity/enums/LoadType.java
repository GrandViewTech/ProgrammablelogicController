package org.grandviewtech.entity.enums;

public enum LoadType
	{
	RIGHT_LINK("RIGHT_LINK"), LEFT_LINK("LEFT_LINK");
		
		private String type;
		
		private LoadType(String type)
			{
				this.type = type;
			}
			
		public String getType()
			{
				return type;
			}
	}
