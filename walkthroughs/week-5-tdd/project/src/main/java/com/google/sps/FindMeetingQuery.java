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

    Collection<TimeRange> openMandatoryTimeRanges = new ArrayList<>();
    Collection<TimeRange> openOptionalTimeRanges = new ArrayList<>();
    Collection<TimeRange> openAllAttendeesTimeRanges = new ArrayList<>();

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
    for (TimeRange timerange : schedule) {
      // Test Case 1: |---| |---|
      if (!previousEvent.overlaps(timerange)) {
        TimeRange openTimeRange = TimeRange.fromStartEnd(previousEvent.end(), timerange.start(), false);
        if (openTimeRange.duration() >= duration) {
          openMandatoryTimeRanges.add(openTimeRange);
        }
      } else if (previousEvent.start() <= timerange.start() && previousEvent.end() >= timerange.end()) {
        // Test Case 2: |---------|
        //                 |---|
        // Make sure previousEvent stays the same.
        timerange = previousEvent;
        break;
      }
      previousEvent = timerange;
    }

    // Add last TimeRange open considering only mandatory attendees.
    TimeRange lastOpen = TimeRange.fromStartEnd(previousEvent.end(), 1440, false);
    if (lastOpen.duration() >= duration) {
      openMandatoryTimeRanges.add(lastOpen);
    }

    // Find open slots within optional attendees.
    TimeRange previousOptionalEvent = TimeRange.fromStartEnd(0, 0, false);
    for (TimeRange timerange : optionalSchedule) {
      // Test Case 1: |---| |---|
      if (!previousOptionalEvent.overlaps(timerange)) {
        TimeRange openTimeRange = TimeRange.fromStartEnd(previousOptionalEvent.end(), timerange.start(), false);
        if (openTimeRange.duration() >= duration) {
          openOptionalTimeRanges.add(openTimeRange);
        }
      } else if (previousOptionalEvent.start() <= timerange.start() && previousOptionalEvent.end() >= timerange.end()) {
        // Test Case 2: |---------|
        //                 |---|
        // Make sure previousEvent stays the same.
        timerange = previousOptionalEvent;
        break;
      }

      previousOptionalEvent = timerange;
    }

    // Add last TimeRange open considering only optional attendees.
    TimeRange openLastOptional = TimeRange.fromStartEnd(previousOptionalEvent.end(), 1440, false);
    if (openLastOptional.duration() >= duration) {
      openOptionalTimeRanges.add(openLastOptional);
    }
    if (!attendees.isEmpty() && openMandatoryTimeRanges.isEmpty()) {
      return openMandatoryTimeRanges;
    } else if (attendees.isEmpty()) {
      return openOptionalTimeRanges;
    }

    boolean optionalCanAttend = false;
    // Compare open mandatory TimeRanges with open optional TimeRanges.
    for (TimeRange openMandatoryTimeRange : openMandatoryTimeRanges) {
      for (TimeRange openOptionalTimeRange : openOptionalTimeRanges) {
        // Compare Case 1: |---|
        //                   |---|
        if (openMandatoryTimeRange.overlaps(openOptionalTimeRange)) {
          TimeRange overlapTimeRange = TimeRange.fromStartEnd(0, 0, false);
          if (openMandatoryTimeRange.start() <= openOptionalTimeRange.start()
              && openMandatoryTimeRange.end() <= openOptionalTimeRange.end()) {
            overlapTimeRange = TimeRange.fromStartEnd(openOptionalTimeRange.start(), openMandatoryTimeRange.end(),
                false);
          } else if (openOptionalTimeRange.start() <= openMandatoryTimeRange.start()
              && openOptionalTimeRange.end() <= openMandatoryTimeRange.end()) {
            overlapTimeRange = TimeRange.fromStartEnd(openMandatoryTimeRange.start(), openOptionalTimeRange.end(),
                false);
          } else if (openMandatoryTimeRange.start() <= openOptionalTimeRange.start()
              && openMandatoryTimeRange.end() >= openOptionalTimeRange.end()) {
            // Compare Case 2: |---------|
            //                    |---|
            overlapTimeRange = TimeRange.fromStartEnd(openOptionalTimeRange.start(), openOptionalTimeRange.end(),
                false);
          } else if (openOptionalTimeRange.start() <= openMandatoryTimeRange.start()
              && openOptionalTimeRange.end() >= openMandatoryTimeRange.end()) {
            overlapTimeRange = TimeRange.fromStartEnd(openMandatoryTimeRange.start(), openMandatoryTimeRange.end(),
                false);
          }

          if (overlapTimeRange.duration() >= duration) {
            openAllAttendeesTimeRanges.add(overlapTimeRange);
            optionalCanAttend = true;
          }
          break;
        }
      }
    }

    // Check to see if there are overlapping open time ranges for mandatory and optional attendees. Return
    // the correct collection of timeranges, depending on the overlap.
    if (!openAllAttendeesTimeRanges.isEmpty() && optionalCanAttend) {
      return openAllAttendeesTimeRanges;
    } else {
      return openMandatoryTimeRanges;
    }
  }
}