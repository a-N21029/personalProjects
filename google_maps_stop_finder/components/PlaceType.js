import usePlacesAutoComplete from "use-places-autocomplete";

const PlaceType = ({setType}) => {
    /* types of places that the user can search for
       types[i][0] = what is displayed to the user
       types[i][1] = corresponding name of types[i][0] that is used in the google maps objects
    */
    const types = [["Restaurant", "restaurant"], ["Gas Station", "gas_station"], ["Hospital", "hospital"], ["Police Station", "police"], ["Convenience Store", "convenience_store"]]
    return (
        <select onChange={(e) => {setType(e.target.value)}} className="combobox-input">
            <option disabled selected>Select type of stop</option>
            {types.map((type) => {return <option key={type[1]} value={type[1]}>{type[0]}</option>})}
        </select>
    )
}

export default PlaceType