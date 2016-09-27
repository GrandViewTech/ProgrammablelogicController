package org.grandviewtech.constants;

public enum NoNc
	{
	NO("NO"), NC("NC");
		
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
