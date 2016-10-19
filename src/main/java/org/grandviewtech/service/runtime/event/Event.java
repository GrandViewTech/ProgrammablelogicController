package org.grandviewtech.service.runtime.event;

public class Event
	{
		private EventCategory	eventCategory	= EventCategory.USER;
		
		private EventLevel		eventLevel		= EventLevel.LOW;
		
		private String			eventMessage	= "";
		
		enum EventCategory
			{
			USER, SYSTEM
			}
			
		enum EventLevel
			{
			HIGH, LOW, MEDIUM
			}
			
		public EventCategory getEventCategory()
			{
				return eventCategory;
			}
			
		public void setEventCategory(EventCategory eventCategory)
			{
				this.eventCategory = eventCategory;
			}
			
		public EventLevel getEventLevel()
			{
				return eventLevel;
			}
			
		public void setEventLevel(EventLevel eventLevel)
			{
				this.eventLevel = eventLevel;
			}
			
		public String getEventMessage()
			{
				return eventMessage;
			}
			
		public void setEventMessage(String eventMessage)
			{
				this.eventMessage = eventMessage;
			}
			
	}
