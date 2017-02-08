package org.grandviewtech.entity.bo;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Routine implements Serializable
	{
		private static final long	serialVersionUID	= 7598562241144944212L;
		public String				description;
		public String				name;
		public Map<Integer, String>	inputs				= new LinkedHashMap<>();
		public String				result;
		public String getDescription()
			{
				return description;
			}
		public void setDescription(String description)
			{
				this.description = description;
			}
		public String getName()
			{
				return name;
			}
		public void setName(String name)
			{
				this.name = name;
			}
		public Map<Integer, String> getInputs()
			{
				return inputs;
			}
		public void setInputs(Map<Integer, String> inputs)
			{
				this.inputs = inputs;
			}
		public String getResult()
			{
				return result;
			}
		public void setResult(String result)
			{
				this.result = result;
			}
		
		
		
	}
