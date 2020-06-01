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

//adds Hello Natalie! to the page
async function getHelloNameUsingAsyncAwait() {
  console.log('Fetching Hello Name!');
  const response = await fetch('/data');
  console.log('Handling the response.');
  const name = await response.text();
  console.log('Adding quote to dom: ' + name);
  document.getElementById('quote-container').innerHTML = name;
}

//fetch
fetch('/my-data-url')  // sends a request to /my-data-url
.then(response => response.json()) // parses the response as JSON
.then((myObject) => { // now we can reference the fields in myObject!
  console.log(myObject.x);
  console.log(myObject.y);
  console.log(myObject.z);
});
