import usePlacesAutoComplete, {getGeocode, getLatLng} from "use-places-autocomplete";
import {Combobox, ComboboxInput, ComboboxPopover, ComboboxList, ComboboxOption} from "@reach/combobox";
import "@reach/combobox/styles.css";

const Places = ({setOffice}) => {
  /* autocomplete user searches*/
  /* ready = check that the map is loaded (this is already checked in Maps.js but it is still good practice to check here as well)
     value = the value that the user entered into an input box
     setValue = change the value everytime a change occurs (deleting or adding a letter(s))
     suggestions:
        status = the status of whether some suggestions were received (OK status) or not
        data = the actual suggestions if the status is ok
     clearSuggestions = whenever a suggestion is selected it is removed from the lsit of suggestions*/
  const {ready, value, setValue, suggestions: {status, data}, clearSuggestions} = usePlacesAutoComplete();
  /* function that runs when the user selects one of the drop down options*/
  const handleSelect = async (val) => {
    setValue(val, false);
    clearSuggestions();

    /* convert address into coordinates */
    const res = await getGeocode({address: val});
    const {lat, lng} = await getLatLng(res[0]);
    setOffice({lat, lng});

  }
  return (
    /* Combobx aka drop down list for search options*/
    <Combobox onSelect={handleSelect}>
        <ComboboxInput value={value} onChange={e => setValue(e.target.value)} className="combobox-input" placeholder="Search office address ..."/>
            <ComboboxPopover>
                <ComboboxList>
                    {status==="OK" && data.map(({place_id, description}) => <ComboboxOption key={place_id} value={description} />)}
                </ComboboxList>
            </ComboboxPopover>
    </Combobox>
  )
}

export default Places