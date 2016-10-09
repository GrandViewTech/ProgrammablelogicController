package org.grandviewtech.entity.enums;

public enum Edge
	{
	RISING("INPUT"), FALLING("FLAG"), DEFAULT("DEFAULT");
		
		private String edge;
		
		private Edge(String edge)
			{
				this.edge = edge;
			}
			
		public String getEdge()
			{
				return edge;
			}
	}
