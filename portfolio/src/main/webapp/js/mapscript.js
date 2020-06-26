/** Creates a map with dark grayscale styling and adds marker information for main locations. */
function createMap() {
  // Create a new StyledMapType object, passing it an array of styles, and the name to be displayed on the map type control.
  const styledMapType = new google.maps.StyledMapType(
    [
      {
        "elementType": "geometry",
        "stylers": [
          {
            "color": "#212121"
          }
        ]
      },
      {
        "elementType": "labels.icon",
        "stylers": [
          {
            "visibility": "off"
          }
        ]
      },
      {
        "elementType": "labels.text.fill",
        "stylers": [
          {
            "color": "#757575"
          }
        ]
      },
      {
        "elementType": "labels.text.stroke",
        "stylers": [
          {
            "color": "#212121"
          }
        ]
      },
      {
        "featureType": "administrative",
        "elementType": "geometry",
        "stylers": [
          {
            "color": "#757575"
          }
        ]
      },
      {
        "featureType": "administrative.country",
        "elementType": "labels.text.fill",
        "stylers": [
          {
            "color": "#9e9e9e"
          }
        ]
      },
      {
        "featureType": "administrative.land_parcel",
        "stylers": [
          {
            "visibility": "off"
          }
        ]
      },
      {
        "featureType": "administrative.locality",
        "elementType": "labels.text.fill",
        "stylers": [
          {
            "color": "#bdbdbd"
          }
        ]
      },
      {
        "featureType": "poi",
        "elementType": "labels.text.fill",
        "stylers": [
          {
            "color": "#757575"
          }
        ]
      },
      {
        "featureType": "poi.park",
        "elementType": "geometry",
        "stylers": [
          {
            "color": "#181818"
          }
        ]
      },
      {
        "featureType": "poi.park",
        "elementType": "labels.text.fill",
        "stylers": [
          {
            "color": "#616161"
          }
        ]
      },
      {
        "featureType": "poi.park",
        "elementType": "labels.text.stroke",
        "stylers": [
          {
            "color": "#1b1b1b"
          }
        ]
      },
      {
        "featureType": "road",
        "elementType": "geometry.fill",
        "stylers": [
          {
            "color": "#2c2c2c"
          }
        ]
      },
      {
        "featureType": "road",
        "elementType": "labels.text.fill",
        "stylers": [
          {
            "color": "#8a8a8a"
          }
        ]
      },
      {
        "featureType": "road.arterial",
        "elementType": "geometry",
        "stylers": [
          {
            "color": "#373737"
          }
        ]
      },
      {
        "featureType": "road.highway",
        "elementType": "geometry",
        "stylers": [
          {
            "color": "#3c3c3c"
          }
        ]
      },
      {
        "featureType": "road.highway.controlled_access",
        "elementType": "geometry",
        "stylers": [
          {
            "color": "#4e4e4e"
          }
        ]
      },
      {
        "featureType": "road.local",
        "elementType": "labels.text.fill",
        "stylers": [
          {
            "color": "#616161"
          }
        ]
      },
      {
        "featureType": "transit",
        "elementType": "labels.text.fill",
        "stylers": [
          {
            "color": "#757575"
          }
        ]
      },
      {
        "featureType": "water",
        "elementType": "geometry",
        "stylers": [
          {
            "color": "#000000"
          }
        ]
      },
      {
        "featureType": "water",
        "elementType": "labels.text.fill",
        "stylers": [
          {
            "color": "#3d3d3d"
          }
        ]
      }
    ],
  {name: 'Styled Map'});

  var sunnyvale = new google.maps.LatLng(37.402601, -122.015315);
  // Create a map object, and include the MapTypeId to add to the map type control.
  var map = new google.maps.Map(document.getElementById('map'), {
    center: sunnyvale,
    zoom: 3,
    mapTypeControlOptions: {
      mapTypeIds: ['roadmap', 'satellite', 'hybrid', 'terrain',
              'styled_map']
    }
  });

  // Associate the styled map with the MapTypeId and set it to display.
  map.mapTypes.set('styled_map', styledMapType);
  map.setMapTypeId('styled_map');
  var contentString = '<div id="content">'+
    '<div id="siteNotice">'+
    '</div>'+
    '<h1 id="firstHeading" class="firstHeading">Duke University</h1>'+
    '<div id="bodyContent">'+
    '<p><b>Major: Computer Science and Biology</b><br>' +
    'I aim to use data science to help with wildlife conservation and environmental issues. '+
    'Currently, I am working on a natural language processing project that analyzes newspaper topics for the Duke Chronicle. '+
    'Additionally, I am conducting research on identifying energy infrastructures using computer vision with Kyle Bradbury.'+
    '</p>'+
    '</div>'+
    '</div>';
  const sunnyvaleLandmark = [map, 37.402601, -122.015315, 'Sunnyvale', 'Where I\'m based'];
  const dukeLandmark = [map, 36.001339, -78.938218, 'Duke University', contentString]
  addLandmark(sunnyvaleLandmark[0], sunnyvaleLandmark[1], sunnyvaleLandmark[2], sunnyvaleLandmark[3], sunnyvaleLandmark[4]);
  addLandmark(dukeLandmark[0], dukeLandmark[1], dukeLandmark[2], dukeLandmark[3], dukeLandmark[4]);
}

/** Adds a marker that shows an info window when clicked. */
function addLandmark(map, lat, lng, title, description) {
  const marker = new google.maps.Marker(
      {position: {lat: lat, lng: lng}, map: map, title: title});

  const infoWindow = new google.maps.InfoWindow({content: description});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}