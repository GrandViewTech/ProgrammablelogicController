package org.grandviewtech.entity.bo;

public class SystemPreference
	{
		private CleanUpActivity cleanUpActivity = CleanUpActivity.ON_START_UP;
		
		public enum CleanUpActivity
			{
			ON_SHUTDOWN, ON_START_UP
			}
			
		public CleanUpActivity getCleanUpActivity()
			{
				return cleanUpActivity;
			}
			
		public void setCleanUpActivity(CleanUpActivity cleanUpActivity)
			{
				this.cleanUpActivity = cleanUpActivity;
			}
			
	}
