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

package com.google.sps.servlets;

import com.google.sps.data.Comment;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * A Servlet that returns some example content. 
 * TODO(ntarn): Modify this file to handle comments data.
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private int previousMax = 1;
  private final String TEXT = "text";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the maximum amount of comments to display data from the server.
    int maxCommentsObtained = getMaxComments(request);
    if (previousMax == 1 && maxCommentsObtained != -1){
      previousMax = maxCommentsObtained;
    }
    else if (maxCommentsObtained == -1){
      maxCommentsObtained = previousMax;
      System.err.println("previous max: " + maxCommentsObtained);
    }

    // Prepare a Query instance with the Comment kind of entity to load.
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Get the maximum amount of comments that can be displayed on a page.
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String text = (String) entity.getProperty("text");
      long timestamp = (long) entity.getProperty("timestamp");
      String imageUrl = (String) entity.getProperty("imageUrl");
      Comment comment = new Comment(id, text, timestamp, imageUrl);
      comments.add(comment);
      if(comments.size() >= maxCommentsObtained){
        break;
      }
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  // @Override
  // public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
  //   // Get the comment text input from the form.
  //   String text = request.getParameter("text-input");
  //   long timestamp = System.currentTimeMillis();

  //   // Create an Entity for the comment that can be entered into the DataStore.
  //   Entity commentEntity = new Entity("Comment");
  //   commentEntity.setProperty("text", text);
  //   commentEntity.setProperty("timestamp", timestamp);

  //   // Put newly created Entity.
  //   DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  //   datastore.put(commentEntity);

  //   // Redirect back to the HTML page.
  //   response.sendRedirect("/comments.html");
  // }

  /** Returns the maximum number of comments to display, or -1 if the choice was invalid. */
  private int getMaxComments(HttpServletRequest request) {
    // Get the number of comments to display from the maximum comments selection form.
    String stringMaxComments = request.getParameter("max-comments");

    // Convert the input to an int.
    int maxComments = -1;
    try {
      maxComments = Integer.parseInt(stringMaxComments);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + maxComments);
      return -1;
    }

    return maxComments;
  }
}
