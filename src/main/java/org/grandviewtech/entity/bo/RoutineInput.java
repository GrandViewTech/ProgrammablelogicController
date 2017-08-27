package org.grandviewtech.entity.bo;

public class RoutineInput
	{
		private int		sequence;
		private String	type;
		private String	label;
		private String	name;
		private String	value;
		
		public String getName()
			{
				return name;
			}
			
		public void setName(String name)
			{
				this.name = name;
			}
			
		public int getSequence()
			{
				return sequence;
			}
			
		public void setSequence(int sequence)
			{
				this.sequence = sequence;
			}
			
		public String getType()
			{
				return type;
			}
			
		public void setType(String type)
			{
				this.type = type;
			}
			
		public String getLabel()
			{
				if (label != null)
					{
						label = label.trim();
					}
				return label;
			}
			
		public void setLabel(String label)
			{
				this.label = label;
			}
			
		public String getValue()
			{
				if (value == null)
					{
						value = "";
					}
				return value;
			}
			
		public void setValue(String value)
			{
				this.value = value;
			}
			
	}
