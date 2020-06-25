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

package com.googl.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that processes a form with a comment request. When the user submits
 * the form, Blobstore processes the file upload and then forwards the request
 * to this servlet. The servlet can then process the request using the file URL
 * we get from Blobstore.
 */
@WebServlet("/form-handler")
public class FormHandlerServlet extends HttpServlet {

  private final static String COMMENT = "comment";
  private final static String TEXT = "text";
  private final static String TIMESTAMP = "timestamp";
  private final static String IMAGEURL = "imageUrl";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the maximum number of comments to display from the server.
    int maxCommentsObtained = getMaxComments(request);

    // Prepare a Query instance to load a Comment entity.
    Query query = new Query(COMMENT).addSort(TIMESTAMP, SortDirection.DESCENDING);

    PreparedQuery results = DatastoreServiceFactory.getDatastoreService().prepare(query);

    // Get the maximum number of comments that can be displayed on a page.
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String text = (String) entity.getProperty(TEXT);
      long timestamp = (long) entity.getProperty(TIMESTAMP);
      String imageUrl = (String) entity.getProperty(IMAGEURL);
      comments.add(new Comment(id, text, timestamp, imageUrl));
      if (comments.size() >= maxCommentsObtained) {
        break;
      }
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the comment text input from the form.
    String text = request.getParameter("text-input");
    long timestamp = System.currentTimeMillis();

    // Get the URL of the image that the user uploaded to Blobstore.
    String imageUrl = getUploadedFileUrl(request, "image");

    // Create an Entity for the comment that can be entered into the Datastore.
    Entity commentEntity = new Entity(COMMENT);
    commentEntity.setProperty(TEXT, text);
    commentEntity.setProperty(TIMESTAMP, timestamp);
    if (imageUrl != null) {
      commentEntity.setProperty(IMAGEURL, imageUrl);
    }

    // Put newly created Entity into the Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/comments.html"); //TODO (ntarn): Add parameter to front-end.
  }

  /**
   * Returns a URL that points to the uploaded file, or {@code null} if the user
   * didn't upload a file.
   */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    return (blobInfo.getSize() == 0) ? null : blobKey.getKeyString();
  }

  /**
   * Returns the maximum number of comments to display, or -1 if the choice was invalid.
   */
  private int getMaxComments(HttpServletRequest request) {
    // Get the number of comments to display from the maximum comments selection
    // form.
    String stringMaxComments = request.getParameter("max-comments");

    int maxComments = -1;
    try {
      maxComments = Integer.parseInt(stringMaxComments);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + stringMaxComments);
      return -1;
    }

    return maxComments;
  }
}
