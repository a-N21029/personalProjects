const commutesPerYear = 260 * 2;
const litresPerKm = 10 / 100;
const gasLitreCost = 1.5;
const litreCostKm = litresPerKm * gasLitreCost
const secondsPerDay = 60 * 60 * 24

const Distance = ({leg}) => {
    if (!leg.distance || !leg.duration) return null;

    const days = Math.floor(commutesPerYear *  leg.duration.value) / secondsPerDay;
    const cost = Math.floor(leg.distance.value / 1000 * litreCostKm) * commutesPerYear;
  return (
    <div>
        <p>This home is {leg.distance.text} away from your office. That would take {leg.duration.text} each direction</p>
    </div>
  )
}

export default Distance