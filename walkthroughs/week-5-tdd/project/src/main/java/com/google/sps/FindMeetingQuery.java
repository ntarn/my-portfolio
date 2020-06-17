// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long duration = request.getDuration();
    Collection<String> attendees = request.getAttendees();
    Collection<TimeRange> ret = new ArrayList<>();
  
    TreeSet<TimeRange> schedule = new TreeSet<>(TimeRange.ORDER_BY_START);
    for (Event event : events) {
      Set<String> eventAttendees = event.getAttendees();
      for (String eventAttendee : eventAttendees) {
        if (attendees.contains(eventAttendee)) {
          schedule.add(event.getWhen());
          break;
        }
      }
    }
    TimeRange previousEvent = TimeRange.fromStartEnd(0, 0, false);
    for(TimeRange tr : schedule){
    
      // Case 1: |---| |---|
      if(!previousEvent.overlaps(tr)){
        TimeRange open = TimeRange.fromStartEnd(previousEvent.end(), tr.start(), false);
        if (open.duration()>=duration){
          ret.add(open);
        }
      }
      else if(previousEvent.start()<=tr.start() && previousEvent.end()>=tr.end()){
        // Case 3: |---------|
        //            |---|
        
        tr = previousEvent;// Make sure previousEvent stays the same.
        break;
      }

      
      previousEvent =tr;
    }

    System.out.println("ntarn debug: last TimeRange open");
    TimeRange open = TimeRange.fromStartEnd(previousEvent.end(), 1440, false);
    if(open.duration()>=duration){
      ret.add(open);
    }

  
    return ret;
  }
}
