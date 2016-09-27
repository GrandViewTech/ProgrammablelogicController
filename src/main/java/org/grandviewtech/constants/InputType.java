package org.grandviewtech.constants;

public enum InputType
	{
	INPUT("INPUT"), FLAG("FLAG"), WORD("WORD"), OUTPUT("OUTPUT");
		
		private String inputType;
		
		private InputType(String inputType)
			{
				this.inputType = inputType;
			}
			
		public String getInputType()
			{
				return inputType;
			}
	}
