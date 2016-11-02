package org.grandviewtech.entity.bo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Response implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		private boolean				error				= false;
		private List<String>		messages			= new LinkedList<String>();
		
		public boolean isError()
			{
				if (messages.size() > 0)
					{
						this.error = true;
					}
				return error;
			}
			
		public void setError(boolean error)
			{
				this.error = error;
			}
			
		public List<String> getMessages()
			{
				return messages;
			}
			
		public void addMessage(String message)
			{
				this.messages.add(message);
			}
			
	}
