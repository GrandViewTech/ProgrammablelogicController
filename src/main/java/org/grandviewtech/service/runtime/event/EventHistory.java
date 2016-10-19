package org.grandviewtech.service.runtime.event;

import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.log4j.Logger;

public class EventHistory
	{
		private static Logger	logger		= Logger.getLogger(EventHistory.class);
		
		private Queue<Event>	eventQueue	= new PriorityQueue<Event>();
		
		public EventHistory prgetInstance()
			{
				return EventHistoryInitializer.getEventHistory();
			}
			
		private EventHistory()
			{
			}
			
		private static class EventHistoryInitializer
			{
				private static EventHistory eventHistory;
				
				public static EventHistory getEventHistory()
					{
						if (eventHistory == null)
							{
								eventHistory = new EventHistory();
							}
						return eventHistory;
					}
					
			}
			
		public void addEvent(Event event)
			{
				logger.debug(event.toString());
				this.eventQueue.add(event);
			}
			
	}
