const Route = ({leg}) => {
    if (!leg.distance || !leg.duration) return null;
    return (
        <div>
            <p>{leg.distance.text} away</p>
            <p>{leg.duration.text}</p>
        </div>
    )
}

export default Route