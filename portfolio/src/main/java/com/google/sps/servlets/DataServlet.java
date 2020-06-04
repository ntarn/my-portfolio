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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.common.collect.ImmutableList; 
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
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
	
  private int maxCommentsObtained = 0;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    maxCommentsObtained = getMaxComments(request);

    Query query = new Query("Task").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<String> comments = new ArrayList<String>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String comment = (String) entity.getProperty("comment");
      long timestamp = (long) entity.getProperty("timestamp");

      comments.add(comment);
      if(comments.size() >= maxCommentsObtained){
        break;
      }
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));

     // Redirect back to the HTML page.
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String text = request.getParameter("text-input");
    long timestamp = System.currentTimeMillis();

    // // Get the input from the form. TODO: make sure that submitting max comments doesnt create a new comment too
    // maxCommentsObtained = getMaxComments(request);

    // Creates data in Datastore with the text as a comment property.
    Entity taskEntity = new Entity("Task");
    taskEntity.setProperty("comment", text);
    taskEntity.setProperty("timestamp", timestamp);

    // Put newly created Entity.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /** Returns the max number of comments to display, or -1 if the choice was invalid. */
  private int getMaxComments(HttpServletRequest request) {
    // Get the input from the form.
    String stringMaxComments = request.getParameter("max-comments");

    // Convert the input to an int.
    int maxComments = 1;
    try {
      maxComments = Integer.parseInt(stringMaxComments);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + maxComments);
      return -1;
    }

    // Check that the input is between 1 and 3.
    if (maxComments < 1 || maxComments > 3) {
      System.err.println("The maximum number of comments chosen is out of range: " + maxComments);
      return 1;
    }

    return maxComments;
  }
}
