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

/** Fetches comments from the server and adds them to the DOM. */
function loadComments() {
  var maxComments = document.getElementById('max-comments').value;
  var previous = sessionStorage.getItem('max-comments');
  if (maxComments == -1 && previous != null) {
    console.log('ntarn debug: Setting max to previous max:' + previous);
    maxComments = parseInt(previous);
  }
  fetch('/form-handler?max-comments='+ maxComments)  // Send a request to the URL.
    .then(response => response.json()) // Parse the response as JSON.
    .then((comments) => { // Access the comments.
      console.log('ntarn debug: ' + comments);
      element = document.getElementById('max-comments');
      console.log('ntarn debug: Setting default to:' + comments.length.toString());
      element.value = comments.length;

      sessionStorage.setItem('max-comments', comments.length);

      // Retrieve the list of comments at the ElementById.
      const commentListElement = document.getElementById('comment-list'); 
      commentListElement.innerHTML = '';
      comments.forEach((comment) => {
        commentListElement.appendChild(createCommentElement(comment));
      });
    });
}  

/** Creates an element that represents a comment, including its delete button. */
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';

  const titleElement = document.createElement('p');
  console.log('Adding comments to dom: ' + comment.text);
  titleElement.innerText = comment.text;
  commentElement.appendChild(titleElement);

  const imageUrlElement = null;
  if (comment.imageUrl != null){
    const request = new Request('/blobstore-serve?blob-key=' + comment.imageUrl);
    const imageUrlElement = document.createElement('img');
    fetch(request).then(response => response.blob()).then((blob) => {
      console.log('Adding images to dom: ' + comment.imageUrl);
      imageUrlElement.src = window.URL.createObjectURL(blob);
    })
    commentElement.appendChild(imageUrlElement);
  }
  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);

    // Remove the comment from the DOM.
    commentElement.remove();
  });

  commentElement.appendChild(deleteButtonElement);
  return commentElement;
}

/** Tells the server to delete the comment. */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  console.log('ntarn debug: ID of comments to be removed' + params);
  fetch('/delete-comment?' + params.toString(), {method: 'POST'});
}

function fetchBlobstoreUrlAndShowForm() {
  console.log('ntarn debug: Checking if fetchBlob is called');
  fetch('/blobstore-upload-url')
    .then((response) => {
      return response.text();
    })
    .then((imageUploadUrl) => {
      const messageForm = document.getElementById('my-form');
      messageForm.action = imageUploadUrl;
    });
}

function handleForm() {
  console.log('ntarn debug: Checking if handleForm is called');
  const messageForm = document.getElementById('my-form');
  messageForm.action = '/my-form-handler'; 
}

