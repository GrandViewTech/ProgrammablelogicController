package org.grandviewtech.entity.bo;

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Routine implements Serializable
	{
		private static final long	serialVersionUID	= 7598562241144944212L;
		private String				description;
		private String				name;
		private String				functionalBlock;
		private List<RoutineInput>	routineInputs		= new ArrayList<>();
		private String				result;
		
		private List<Component>		components			= new ArrayList<>();
		
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
			
		public List<RoutineInput> getRoutineInputs()
			{
				return routineInputs;
			}
			
		public void setRoutineInputs(List<RoutineInput> routineInputs)
			{
				this.routineInputs = routineInputs;
			}
			
		public Routine(String name, String description, String functionalBlock, List<RoutineInput> routineInputs, String result)
			{
				super();
				this.description = description;
				this.name = name;
				this.functionalBlock = functionalBlock;
				this.routineInputs = routineInputs;
				this.result = result;
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
				this.routineInputs = routine.getRoutineInputs();
				this.result = routine.getResult();
				return this;
			}
			
	}
