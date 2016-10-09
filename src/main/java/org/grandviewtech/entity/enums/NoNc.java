package org.grandviewtech.entity.enums;

public enum NoNc
	{
	NO("NO"), NC("NC"), DEFAULT("DEFAULT");
		
		private String type;
		
		private NoNc(String type)
			{
				this.type = type;
			}
			
		public String getType()
			{
				return type;
			}
	}
