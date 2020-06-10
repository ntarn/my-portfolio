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

/**
 * Fetches comments from the server and adds them to the DOM. 
 * TODO(ntarn): Fix how many comments are displayed when submitting a comment.
 */
function loadComments() {
   const maxComments = document.getElementById('maxComments').value;
   fetch('/data?max-comments='+ maxComments)  // Sends a request to /the URL.
    .then(response => response.json()) // Parses the response as JSON.
    .then((comments) => { // Now we can reference the fields in comments.
      console.log(comments);
      const commentListElement = document.getElementById('comment-list'); // Retrieve the list of comments at the ElementById.
      commentListElement.innerHTML = "";
      comments.forEach((comment) => {
        commentListElement.appendChild(createCommentElement(comment));
      })
    });
}  


/** Creates an element that represents a comment, including its delete button. */
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';

  const titleElement = document.createElement('span');
  console.log('Adding comments to dom: ' + comment.text);
  titleElement.innerText = comment.text;

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);

    // Remove the comment from the DOM.
    commentElement.remove();
  });

  commentElement.appendChild(titleElement);
  commentElement.appendChild(deleteButtonElement);
  return commentElement;
}

/** Tells the server to delete the comment. */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  console.log('ID of comments to be removed' + params);
  fetch('/delete-data?' + params.toString(), {method: 'POST'});
}

function fetchBlobstoreUrlAndShowForm() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const messageForm = document.getElementById('my-form');
        messageForm.action = imageUploadUrl;
        messageForm.classList.remove('hidden');
      });
}
