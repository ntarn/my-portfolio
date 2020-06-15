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
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * When the user submits the form, Blobstore processes the file upload and then forwards the request
 * to this servlet. This servlet can then process the request using the file URL we get from
 * Blobstore.
 */
@WebServlet("/my-form-handler")
public class FormHandlerServlet extends HttpServlet {

  private int previousMax = 1;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the maximum amount of comments to display data from the server.
    int maxCommentsObtained = getMaxComments(request);
    if (previousMax == 1 && maxCommentsObtained != -1){
      previousMax = maxCommentsObtained;
      System.err.println("ntarn debug previous max change to current max: " + maxCommentsObtained);
    }
    else if (maxCommentsObtained == -1){
      maxCommentsObtained = previousMax;
      System.err.println("ntarn debug previous max: " + maxCommentsObtained);
    }
    else{
      System.err.println("ntarn debug previous max reach else statement: " + maxCommentsObtained);
    }

    // Prepare a Query instance with the Comment kind of entity to load.
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Get the maximum number of comments that can be displayed on a page.
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


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the comment text input from the form.
    String text = request.getParameter("text-input");
    List<String> parameterNamesList = Collections.list(request.getParameterNames());
    long timestamp = System.currentTimeMillis();
    String imageUrl = "";

    // Get the URL of the image that the user uploaded to Blobstore.
    if(request.getParameter("image") != null){
      imageUrl = getUploadedFileUrl(request, "image");
    }

    // Create an Entity for the comment that can be entered into the DataStore.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", text);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("imageUrl", imageUrl);// == null ? "" : imageUrl); //separate this somehow

    // Put newly created Entity into the Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/comments.html");
  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);
    System.out.println("ntarn debug: blobKeys obtained");

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's dev server, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }

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
