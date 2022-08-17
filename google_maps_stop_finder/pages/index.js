import Head from 'next/head'
import Image from 'next/image'
import styles from '../styles/Home.module.css'
import {useLoadScript} from "@react-google-maps/api"
import Map from '../components/Map'

export default function Home() {
  /* load the map script*/
  const {isLoaded} = useLoadScript({
    googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAP_API_KEY,
    libraries: ["places"],

  })
  if(!isLoaded){
    return <div>Loading ...</div>
  }
  return (
    <div>
      <Map />
    </div>
  )
}