import {useState, useMemo, useRef, useCallback} from 'react'
import { GoogleMap, Marker, DirectionsRenderer, Circle, MarkerClusterer } from '@react-google-maps/api'
import Places from './Places'
import Distance from "./Distance"



const Map = () => {
    /* used to change the state of the places being focused on depending on the user's search*/
    const [office, setOffice] = useState();
    const [directions, setDirections] = useState();
    /* will be used to be able to reference the map later on */
    const mapRef = useRef();
    /* utilizing useMemo to center the focust of the map around some location the first time it renders
       instead of recalculating it each time the map is rendered*/
    const center = useMemo(() => ({lat: 43, lng: -80}), []);
    
    /* disabling some options in the map (such as the option to switch between satellite and map view*/
    const options = useMemo(() => ({
        disableDefaultUI: true,
        clickableIcons: false
    }),
    []);

    /* when the google map finishes loading, we should acquire its ref*/
    const onLoad = useCallback(map => (mapRef.current = map), []);
    
    const houses = useMemo(() => generateHouses(center), [center]);

    /* get directions for destination using directions API*/
    const fetchDirections = (house) => {
      if(!office) return;

      const service = new google.maps.DirectionsService();
      service.route(
        {
          origin: house,
          destination: office,
          travelMode: google.maps.TravelMode.DRIVING
        },
        (result, status) => {
          if(status === "OK" && result) setDirections(result);
        }
      );
    }
  return (
    <div className="container">
        <div className='controls'>
            <h1>Commute</h1>
            <Places setOffice={(position) => {
              setOffice(position);
              mapRef.current.panTo(position);
            }}/>

            {!office && <p> Enter address of destination</p>}
            {directions && <Distance leg={directions.routes[0].legs[0]} />}
        </div>
        <div className='map'>
            <GoogleMap zoom={10} center={center} mapContainerClassName="map-container" options={options} onLoad={onLoad}>
              {directions && <DirectionsRenderer directions={directions}/>}
              {office && (
              <>
                <Marker position={office}/>
                  {clusterer => {houses.map((house) => (<Marker key={house.lat} position={house} clusterer={clusterer}/>))}}
                
                <MarkerClusterer>
                {(clusterer) => houses.map((house) => (
                    <Marker
                      key={house.lat}
                      position={house}
                      clusterer={clusterer}
                      onClick={() => {
                        fetchDirections(house);
                      }}
                    />
                  ))
                }
              </MarkerClusterer>
                <Circle center={office} radius={15000} options={closeOptions}/>
                <Circle center={office} radius={30000} options={middleOptions}/>
                <Circle center={office} radius={45000} options={farOptions}/>
              </>)}
            </GoogleMap>
        </div>
    </div>
  )
}

const defaultOptions = {
    strokeOpacity: 0.5,
    strokeWeight: 2,
    clickable: false,
    draggable: false,
    editable: false,
    visible: true,
  };
  const closeOptions = {
    ...defaultOptions,
    zIndex: 3,
    fillOpacity: 0.05,
    strokeColor: "#8BC34A",
    fillColor: "#8BC34A",
  };
  const middleOptions = {
    ...defaultOptions,
    zIndex: 2,
    fillOpacity: 0.05,
    strokeColor: "#FBC02D",
    fillColor: "#FBC02D",
  };
  const farOptions = {
    ...defaultOptions,
    zIndex: 1,
    fillOpacity: 0.05,
    strokeColor: "#FF5252",
    fillColor: "#FF5252",
  };

  const generateHouses = (position) => {
    const _houses = [];
    for (let i = 0; i < 100; i++) {
      const direction = Math.random() < 0.5 ? -2 : 2;
      _houses.push({
        lat: position.lat + Math.random() / direction,
        lng: position.lng + Math.random() / direction,
      });
    }
    return _houses;
  };

export default Map