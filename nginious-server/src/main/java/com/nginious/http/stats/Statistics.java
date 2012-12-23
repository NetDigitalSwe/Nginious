/**
 * Copyright 2012 NetDigital Sweden AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.nginious.http.stats;

import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * Maintains statistics from start time to the current time in segments of one minute each.
 */
abstract class Statistics<T extends StatisticsEntry> {
	
	protected long startTimeMillis;
	
	protected int endTimeDeltaMinutes;
	
	protected long currentHourBaseTimeMillis;	
	
	protected T[] curHour;
	
	protected T[][] curDate;
	
	protected CopyOnWriteArrayList<T[][]> dates;
	
	Statistics() {
		this(System.currentTimeMillis());
	}
	
	Statistics(long startTimeMillis) {
		super();
		this.startTimeMillis = startTimeMillis;
		this.endTimeDeltaMinutes = 0;
		this.currentHourBaseTimeMillis = this.startTimeMillis - (this.startTimeMillis % 3600000L);
		this.dates = new CopyOnWriteArrayList<T[][]>();
		setupHours(this.currentHourBaseTimeMillis, this.startTimeMillis);
		this.startTimeMillis -= this.startTimeMillis % 60000L;
		this.currentHourBaseTimeMillis -= 3600000L;
		
	}
	
	/**
	 * Returns the start time in milliseconds rounded to the nearest earlier minute for this statistics.
	 * 
	 * @return the start time in milliseconds rounded to the nearest earlier minute
	 */
	public Date getStartTime() {
		return new Date(this.startTimeMillis);
	}
	
	/**
	 * Returns the end time in milliseconds rounded to to closes higher minute for the last
	 * statistics entry.
	 * 
	 * @return the end time in milliseconds rounded to the nearest higher minute
	 */
	public Date getEndTime() {
		long endTimeMillis = this.startTimeMillis + (this.endTimeDeltaMinutes * 60000L);
		return new Date(endTimeMillis);
	}
	
	/**
	 * Returns all statistics entries for each minute within the specified start time
	 * to end time period. Empty statistics entries within the time period are also
	 * returned.
	 *  
	 * @param startTime the start time in milliseconds
	 * @param endTime the end time in milliseconds
	 * @return all minute statistics entries within the time period
	 */
	public T[] getEntries(Date startTime, Date endTime) {
		long startRangeTimeMillis = this.startTimeMillis;
		long endRangeTimeMillis = this.startTimeMillis + (this.endTimeDeltaMinutes * 60000L);
		
		long startTimeMillis = startRangeTimeMillis;
		long endTimeMillis = endRangeTimeMillis;
		
		if(startTime != null) {
			startTimeMillis = startTime.getTime();
		}
		
		if(endTime != null) {
			endTimeMillis = endTime.getTime();
		}
		
		if(startTimeMillis > endTimeMillis) {
			return createArray(0);
		}
		
		if(endTimeMillis < startRangeTimeMillis) {
			return createArray(0);
		}
		
		if(startTimeMillis < startRangeTimeMillis) {
			startTimeMillis = startRangeTimeMillis;
		}
		
		if(endTimeMillis > endRangeTimeMillis) {
			endTimeMillis = endRangeTimeMillis;
		}
		
		int numEntries = (int)((endTimeMillis - startTimeMillis) / 60000L) + 1;
		
		if((endTimeMillis - startTimeMillis) % 60000L != 0) {
			numEntries++;
		}
		
		int startDateIndex = (int)((startTimeMillis - startRangeTimeMillis) / 86400000L);
		int endDateIndex = (int)((endTimeMillis - this.startTimeMillis) / 86400000L);
		int startHourIndex = (int)((startTimeMillis % 86400000L) / 3600000L);
		int endHourIndex = (int)((endTimeMillis % 86400000L) / 3600000L);
		int startMinuteIndex = (int)((startTimeMillis % 3600000L) / 60000L);
		int endMinuteIndex = (int)((endTimeMillis % 3600000L) / 60000L);
		T[] outEntries = createArray(numEntries);
		int outEntriesPos = 0;
		
		for(int i = startDateIndex; i <= endDateIndex; i++) {
			int startHour = 0;
			int endHour = 23;
			
			if(i == startDateIndex) {
				startHour = startHourIndex;
			}
			
			if(i == endDateIndex) {
				endHour = endHourIndex;
			}
			
			T[][] dateEntries = dates.get(i);
			
			for(int j = startHour; j <= endHour; j++) {
				int startMin = 0;
				int endMin = 59;
				
				if(i == startDateIndex && j == startHour) {
					startMin = startMinuteIndex;
				}
				
				if(i == endDateIndex && j == endHour) {
					endMin = endMinuteIndex;
				}
				
				for(int k = startMin; k <= endMin; k++) {
					outEntries[outEntriesPos++] = dateEntries[j][k];
				}
			}
		}
		
		return outEntries;
	}
	
	/*
	 * Returns the current statistics entry. If a statistics entry for the current minute
	 * period does not exist entries for the next hour are created and stored.
	 */
	protected T getEntry() {
		long curTimeMillis = System.currentTimeMillis();
		int slot = (int)((curTimeMillis - this.currentHourBaseTimeMillis) / 60000L);
		
		if(slot >= 60) {
			setupHours(this.currentHourBaseTimeMillis, curTimeMillis);
			this.currentHourBaseTimeMillis -= 3600000L;
			slot = (int)((curTimeMillis - this.currentHourBaseTimeMillis) / 60000L);
		}
		
		this.endTimeDeltaMinutes = (int)((curTimeMillis - this.startTimeMillis) / 60000L);
		return curHour[slot];
	}
	
	/*
	 * Creates statistics entries for the hour starting at the specified hour
	 * base time.
	 */
	private void setupHours(long hourBaseTimeMillis, long curTimeMillis) {
		synchronized(this) {
			// Another thread already updated while we where waiting for the lock
			if(hourBaseTimeMillis != this.currentHourBaseTimeMillis) {
				return;
			}
			
			while(hourBaseTimeMillis < curTimeMillis) {
				long minuteMillis = hourBaseTimeMillis;
				T[] entries = createArray(60);
				
				for(int i = 0; i < entries.length; i++) {
					entries[i] = createEntry(minuteMillis);
					minuteMillis += 60000L;
				}
				
				if(this.curHour != null) {
					for(int i = 0; i < curHour.length; i++) {
						curHour[i].setCurrent(false);
					}
				}
					
				this.curHour = entries;
				int hourSlot = (int)((hourBaseTimeMillis % 86400000L) / 3600000L);
				hourBaseTimeMillis += 3600000L;
				
				if(hourSlot == 0 || this.curDate == null) {
					this.curDate = create2Array(24);
					dates.add(this.curDate);
					
					if(dates.size() > 100) {
						dates.remove(0);
						this.startTimeMillis += 86400000L;
					}
				}
				
				curDate[hourSlot] = entries;
			}
			
			this.currentHourBaseTimeMillis = hourBaseTimeMillis;
		}
	}
	
	/*
	 * Creates statistics entry for the minute starting at the specified minute in
	 * milliseconds.
	 */
	protected abstract T createEntry(long minuteMillis);
	
	protected abstract T[] createArray(int size);
	
	protected abstract T[][] create2Array(int size);
}
