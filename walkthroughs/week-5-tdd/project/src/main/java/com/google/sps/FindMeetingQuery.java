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
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    
    Collection<TimeRange> retWithoutOptional = new ArrayList<>();
    Collection<TimeRange> openOptional = new ArrayList<>();
    Collection<TimeRange> retWithOptional = new ArrayList<>();

    TreeSet<TimeRange> schedule = new TreeSet<>(TimeRange.ORDER_BY_START);
    TreeSet<TimeRange> optionalSchedule = new TreeSet<>(TimeRange.ORDER_BY_START);
    for (Event event : events) {
      Set<String> eventAttendees = event.getAttendees();
      for (String eventAttendee : eventAttendees) {
        if (attendees.contains(eventAttendee)) {
          schedule.add(event.getWhen());
        }
        if (optionalAttendees.contains(eventAttendee)) {
          optionalSchedule.add(event.getWhen());
        }
      }
    }

    // Find open slots within mandatory attendees.
    TimeRange previousEvent = TimeRange.fromStartEnd(0, 0, false);
    for (TimeRange tr : schedule) {

      // Case 1: |---| |---|
      if (!previousEvent.overlaps(tr)) {
        TimeRange open = TimeRange.fromStartEnd(previousEvent.end(), tr.start(), false);
        if (open.duration() >= duration) {
          retWithoutOptional.add(open);
        }
      } else if (previousEvent.start() <= tr.start() && previousEvent.end() >= tr.end()) {
        // Case 3: |---------|
        //            |---|

        tr = previousEvent;// Make sure previousEvent stays the same.
        break;
      }

      previousEvent = tr;
    }

    // Add last TimeRange open considering only mandatory attendees.
    TimeRange open = TimeRange.fromStartEnd(previousEvent.end(), 1440, false);
    if (open.duration() >= duration) {
      retWithoutOptional.add(open);
    }

    // Find open slots within optional attendees.
    TimeRange previousOptionalEvent = TimeRange.fromStartEnd(0, 0, false);
    for (TimeRange tr : optionalSchedule) {

      // Case 1: |---| |---|
      if (!previousOptionalEvent.overlaps(tr)) {
        TimeRange open = TimeRange.fromStartEnd(previousOptionalEvent.end(), tr.start(), false);
        if (open.duration() >= duration) {
          openOptional.add(open);
        }
      } else if (previousOptionalEvent.start() <= tr.start() && previousOPtionalEvent.end() >= tr.end()) {
        // Case 3: |---------|
        //            |---|

        tr = previousOptionalEvent;// Make sure previousEvent stays the same.
        break;
      }

      previousOptionalEvent = tr;
    }

    // Add last TimeRange open considering only optional attendees.
    TimeRange openLastOptional = TimeRange.fromStartEnd(previousOptionalEvent.end(), 1440, false);
    if (openLastOptional.duration() >= duration) {
      openOptional.add(open);
    }

    boolean optionalCanAttend = false;
    // Compare open mandatory TimeRanges with open optional TimeRanges.
    for (TimeRange mtr: retWithOptional){
      for(TimeRange otr: retWithoutOptional){
        // Case 2a: |---|
        //           |---|
        if (mtr.overlaps(otr)) {
          TimeRange openBoth;
          if (mtr.start()<=otr.start() && mtr.end() <= otr.end()){
            openBoth = new TimeRange.fromStartEnd(otr.start(), mtr.end(), true);  
    
          }
          else if (otr.start()<=mtr.start() && otr.end() <= mtr.end()){
            openBoth = new TimeRange.fromStartEnd(mtr.start(), otr.end(), true); 

          }
          else if (mtr.start() <= otr.start() && mtr.end() >= otr.end()) {
            openBoth = new TimeRange.fromStartEnd(otr.start(), otr.end(), true); 

          }
          else if (otr.start() <= mtr.start() && otr.end() >= mtr.end()) {
            openBoth = new TimeRange.fromStartEnd(mtr.start(), mtr.end(), true); 
            
          }
          
          if (openBoth.duration() >= duration) {
            retWithOptional.add(openBoth);
            optionalCanAttend = true;
          }
          break;
          // Case 3: |---------|
          //            |---|
  

      }
    }
    return ret;
  }
}
