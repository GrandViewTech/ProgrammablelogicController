package org.grandviewtech.service.runtime.user.useractivity;

public class Activity
	{
		private String		message;
		
		private Category	category;
		
		public String getMessage()
			{
				return message;
			}
			
		public void setMessage(String message)
			{
				this.message = message;
			}
			
		public Category getCategory()
			{
				if (category == null)
					{
						category = Category.DEFAULT;
					}
				return category;
			}
			
		public void setCategory(Category category)
			{
				this.category = category;
			}
			
		public enum Category
			{
			DEFAULT, USER, SYSTEM
			}
			
		public Activity(String message, Category category)
			{
				super();
				this.message = message;
				this.category = category;
			}
			
		@Override
		public String toString()
			{
				return "Activity [" + (message != null ? "message=" + message + ", " : "") + (category != null ? "category=" + category : "") + "]";
			}
			
	}
