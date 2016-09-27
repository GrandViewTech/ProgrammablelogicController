package org.grandviewtech.constants;

public enum Edge
	{
	RISING("INPUT"), FALLING("FLAG");
		
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
