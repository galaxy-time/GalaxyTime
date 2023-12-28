import log from "@lab75/logger"

import pkg from '../../package.json' assert { type: "json" }
import zones from '../data/data.json' assert { type: "json" }
import Astronomy from 'astronomy-engine'

import { AddZoneInput, Zone } from './graph.js'

// console.log( pkg.name )
// console.log( pkg.version )
// console.log( zones.map(zone => zone.name) )

const planets = () => {

  for (var i in Astronomy.Body) {
    log(
      Astronomy.Body[i]
    )
  }

}

planets()

// resolvers

const resolvers = {
  Query: {
    // heartbeat
    ping: () => 'pong',
    // api version
    version: () => pkg.name + ' ' + pkg.version,
    // all zones
    zones: () => zones,
    // query a zone
    zone: ( _parent: any, args: any ) => {
      return zones.find( zone => (
        zone.id === args.id ||
        zone.name === args.name
      ) )
    },
    // query local time for a zone
    time: ( _parent: any, args: any ): String => {
      return `The time on ${args.zone} is ${Date.now()}`
    },
},
  Mutation: {
    // addZone: ( _parent: any, { input }: { input: AddZoneInput }): Zone => {
    //   const id = String(zones.length + 1);
    //   const zone = { id, ...input };
    //   zones.push(zone);
    //   return zone;
    // },
  },
};

export default resolvers
