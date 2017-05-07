package org.grandviewtech.entity.bo;

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Routine implements Serializable
	{
		private static final long		serialVersionUID	= 7598562241144944212L;
		private String					description;
		private String					name;
		private String					functionalBlock;
		private Map<Integer, String>	inputs				= new LinkedHashMap<>();
		private Map<Integer, String>	values				= new LinkedHashMap<>();
		private String					result;
		
		private List<Component>			components			= new ArrayList<>();
		
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
				this.name = name.trim();
			}
			
		public Map<Integer, String> getInputs()
			{
				if (this.inputs == null)
					{
						this.inputs = new LinkedHashMap<>();
					}
				return inputs;
			}
			
		public void setInputs(Map<Integer, String> inputs)
			{
				if (this.inputs == null)
					{
						this.inputs = new LinkedHashMap<>();
					}
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
			
		public String getFunctionalBlock()
			{
				return functionalBlock;
			}
			
		public void setFunctionalBlock(String functionalBlock)
			{
				this.functionalBlock = functionalBlock;
			}
			
		public Routine(String name, String description, String functionalBlock, Map<Integer, String> inputs, String result)
			{
				super();
				this.description = description;
				this.name = name;
				this.functionalBlock = functionalBlock;
				this.inputs = inputs;
				this.result = result;
			}
			
		public Map<Integer, String> getValues()
			{
				if(values==null)
					{
						values=new LinkedHashMap<>();
					}
				return values;
			}
			
		public void addValue(Integer key, String value)
			{
				System.out.println("Key : " + key + " | Value : " + value);
				if (this.values == null)
					{
						this.values = new LinkedHashMap<>();
					}
				this.values.put(key, value);
			}
			
		public List<Component> getComponents()
			{
				if (this.components == null)
					{
						this.components = new ArrayList<>();
					}
				return components;
			}
			
		public void setComponent(Component component)
			{
				if (this.components == null)
					{
						this.components = new ArrayList<>();
					}
				this.components.add(component);
			}
			
		public Routine update(Routine routine)
			{
				this.description = routine.getDescription();
				this.name = routine.getName();
				this.functionalBlock = routine.getFunctionalBlock();
				this.inputs = routine.getInputs();
				this.result = routine.getResult();
				return this;
			}
			
	}
