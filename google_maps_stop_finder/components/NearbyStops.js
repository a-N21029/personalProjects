const NearbyStops = ({stops}) => {
    return (
        <>
            <h1>Results:</h1>
            <div id="stopResults" style={{"overflow":"scroll", "height": "60%"}}>
                <div style={{"cursor":"pointer"}}>
                    {stops.map((stop) => {return (
                    <>
                        <h2>{stop.name}</h2>
                        <p>{stop.vicinity}</p>
                        <p>Rating: {stop.rating? stop.rating : "No Ratings"}</p>
                        <p>User reviews: {stop.user_ratings_total ? stop.user_ratings_total: 0}</p>
                        {stop.photos && <img src={stop.photos[0].getUrl()} style={{"width": "100%"}}></img>}
                        <p>{"_".repeat(40)}</p>
                    </>
                    )})}
                </div>
            </div>
        </>
    )
}

export default NearbyStops