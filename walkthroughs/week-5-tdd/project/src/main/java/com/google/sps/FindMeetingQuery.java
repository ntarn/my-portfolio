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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.events.Event;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // subtract end of first event and start of second to see if it is long as the
    // duration of request
    long duration = request.getDuration();
    Collection<String> attendees = request.getAttendees();
    String firstAttendee = "";

    HashMap<String, TreeSet<TimeRange>> indSchedules = new HashMap<>();

    for (Event event : events) {
      Set<String> eventAttendees = event.getAttendees();
      for (String eventAttendee : eventAttendees) {
        if (attendees.contains(eventAttendee)) {
          // if the person who needs to be in the meeting is in this meeting add it to
          // their schedule
          if (firstAttendee.isEmpty()) {
            firstAttendee = eventAttendee;
          }
          TreeSet<TimeRange> schedule = indSchedules.get(eventAttendee);
          TimeRange addEvent = event.getWhen();
          if (schedule != null) {
            schedule.add(addEvent);
            indSchedules.put(eventAttendee, schedule);
          } else {
            schedule = new TreeSet<>(TimeRange.ORDER_BY_START);
            schedule.add(addEvent);
            indSchedules.put(eventAttendee, schedule);
          }
        }
      }
    }
    TimeRange previousEvent;
    for (TimeRange tr : indSchedules.get(firstAttendee)){
      if (previous == null){
        previousEvent = tr;
        continue;
      }
      else{
        TimeRange open = new TimeRange(previousEvent.end(), tr.start(), false);
        if(open.duration()>=duration){
          for(String attendee: attendees){
            
          }
        }
      }
      previousEvent = tr;
    }

      throw new UnsupportedOperationException("TODO: Implement this method.");
  }
}
