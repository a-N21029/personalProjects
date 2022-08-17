import {useState, useMemo, useRef, useCallback} from 'react'
import { GoogleMap, Marker, DirectionsRenderer, Circle, MarkerClusterer, DistanceMatrixService } from '@react-google-maps/api'
import Places from './Places';
import Route from './Route';
import PlaceType from './PlaceType';
import NearbyStops from './NearbyStops';
import MyApp from '../pages/_app';

const Map = () => {
  /* used to change the state of the places being focused on depending on the user's search*/
  const [origin, setOrigin] = useState();
  /* The destination that the user input */
  const [dest, setDest] = useState();
  /* the type of destination the user wants to search for*/
  const [type, setType] = useState();
  /* the result of the stops search*/
  const [stops, setStops] = useState();

  /* state of the direction from origin to dest*/
  const [directions, setDirections] = useState();
  /* directions from origin to stop and stop to dest*/
  const [origToStop, setOTS] = useState();
  const[stopToDest, setSTD] = useState();

  /* will be used to be able to reference the map later on */
  const mapRef = useRef();

  var originMarker = <Marker position={origin} />;
  var destMarker = <Marker position={dest}/>;

  /* utilizing useMemo to center the focus of the map around some location the first time it renders
       instead of recalculating it each time the map is rendered*/
  const center = useMemo(() => ({lat: 37, lng: -122}), []);
    
  /* disabling some options in the map (such as the option to switch between satellite and map view*/
  const options = useMemo(() => ({
      disableDefaultUI: true,
      clickableIcons: false
  }),
  []);

  /* when the google map finishes loading, we should acquire its ref*/
  const onLoad = useCallback((map) => {
    mapRef.current = map;
  }, []);


  /* get directions from src to dest*/
  const fetchDirections = (waypoint) => {
    if(!(origin && dest)) return;
    const service = new google.maps.DirectionsService();
    service.route(
      {
        origin: origin,
        destination: dest,
        travelMode: google.maps.TravelMode.DRIVING,
        waypoints: waypoint
      },
      (result, status) => {
        if(status === "OK" && result){
          console.log(result);
          setDirections(result);
        }
        
      }
    );
    /* after directions are fetched, return them so they can be displayed*/
    return <DirectionsRenderer className="directions" directions={directions}/>;
  }


  /* find nearby stops*/
  const fetchStops = () => {
    const path = directions.routes[0].overview_path;
    var request = {
      location: path[Object.keys(path).length / 2],
      radius: '5500',
      type: [type],
    };

    const service = new google.maps.places.PlacesService(mapRef.current);
    /* search for the nearby stops with the specified parameters in request
       and run a callback function upon completion of the request*/
    service.nearbySearch(request, (results, status) => {
      if (status == google.maps.places.PlacesServiceStatus.OK) {
        setStops(results);
      }
    })
  }

  const fetchStopDirections = (stop) => {
    if(!(origin && dest)) return;
    const service1 = new google.maps.DirectionsService();
    service1.route(
      {
        origin: origin,
        destination: stop,
        travelMode: google.maps.TravelMode.DRIVING
      },
      (result, status) => {
        if(status === "OK" && result){
          setOTS(result);
        }
        
      }
    );
    const service2 = new google.maps.DirectionsService();
    service2.route(
      {
        origin: stop,
        destination: dest,
        travelMode: google.maps.TravelMode.DRIVING
      },
      (result, status) => {
        if(status === "OK" && result){
          setSTD(result);
        }
        
      }
    );
  return (<>
    <DirectionsRenderer className="directions" directions={origToStop}/>
    <DirectionsRenderer className="directions" directions={stopToDest}/>
  </>)
  }

  return (
    <div className='container'>
      <div className='controls'>
        <Places placeholder={"Starting location"} setLocation={(position) => {
              setOrigin(position);
              mapRef.current.panTo(position);
            }}/>
        <Places placeholder={"Destination"} setLocation={(position) => {
          setDest(position);
        }}/>

        {directions && <Route leg={directions.routes[0].legs[0]} />}

        <PlaceType setType={(type) => {
              setType(type);
        }}/>
        {stops && <NearbyStops stops={stops}/>}
      </div>
      <GoogleMap zoom={10} center={center} options={options} id="map" mapContainerClassName="map-container" onLoad={onLoad}>
        {/* Generate a markers to show locations chosen */}
        {origin && originMarker}
        {dest && destMarker}
        {/* when the origing and destination are specified, show direcetions */}
        {origin && dest && fetchDirections([])}
        {origin && dest && type && fetchStops()}
        {/* show results of stop search */}
        {stops && stops.map((stop) => { return <Marker position={stop.geometry.location} onClick={() => {fetchDirections([{location: stop.geometry.location, stopover: true}])}}/>})}
      </GoogleMap>
    </div>
  )
}
export default Map

