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
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['I have built an app that tracks the carbon footprint of companies', 'worked on detecting sarcasm in Twitter and Reddit posts', 'visualized dominance hierarchies in zebra finches'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

// Adds "Hello Natalie!" to the page.
async function getHelloNameUsingAsyncAwait() {
  console.log('Fetching Hello Name!');
  const response = await fetch('/data');
  console.log('Handling the response.');
  const name = await response.text();
  console.log('Adding quote to dom: ' + name);
  document.getElementById('quote-container').innerHTML = name;
}

// Parse the hardcode messages as JSON.
function changeAListJson(){
  fetch('/data')  // Sends a request to /data .
  .then(response => response.json()) // Parses the response as JSON.
  .then((testGetMethodMessages) => { // Now we can reference the fields in hardcodeMessages.
    console.log(testGetMethodMessages[0]);
    console.log(testGetMethodMessages[1]);
    console.log(testGetMethodMessages[2]);
    console.log('Adding hardcode messages to dom: ' + testGetMethodMessages);
    document.getElementById('hardcode-messages').innerHTML = testGetMethodMessages;
});
}

// Parse the ArrayList comments as JSON.
function printComments(){
  fetch('/data')  // Sends a request to /data .
  .then(response => response.json()) // Parses the response as JSON.
  .then((comments) => { // Now we can reference the fields in hardcodeMessages.
    console.log(comments);
    console.log('Adding comments to dom: ' + comments);
    document.getElementById('print-comments').innerHTML = comments;
});
}

/** Fetches tasks from the server and adds them to the DOM. */
function loadTasks() {
  fetch('/list-tasks').then(response => response.json()).then((tasks) => {
    const taskListElement = document.getElementById('task-list');
    tasks.forEach((task) => {
      taskListElement.appendChild(createTaskElement(task));
    })
  });
}

/** Creates an element that represents a task, including its delete button. */
function createTaskElement(task) {
  const taskElement = document.createElement('li');
  taskElement.className = 'task';

  const titleElement = document.createElement('span');
  titleElement.innerText = task.title;

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteTask(task);

    // Remove the task from the DOM.
    taskElement.remove();
  });

  taskElement.appendChild(titleElement);
  taskElement.appendChild(deleteButtonElement);
  return taskElement;
}

/** Tells the server to delete the task. */
function deleteTask(task) {
  const params = new URLSearchParams();
  params.append('id', task.id);
  fetch('/delete-task', {method: 'POST', body: params});
}

