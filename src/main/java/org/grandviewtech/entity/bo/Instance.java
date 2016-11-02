package org.grandviewtech.entity.bo;

import java.io.Serializable;

public class Instance implements Serializable
	{
		private static final long	serialVersionUID	= 1136346437728355898L;
		private boolean				end					= false;
		private int					endRowNumber		= -1;
		private int					endColumnNumber		= -1;
		
		public boolean isEnd()
			{
				return end;
			}
			
		public void setEnd(boolean end)
			{
				this.end = end;
			}
			
	}
