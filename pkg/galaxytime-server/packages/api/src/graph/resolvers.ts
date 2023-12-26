import { AddZoneInput, Zone } from './graph.js'
import pkg from '../../package.json' assert { type: "json" }
import zones from '../data/data.json' assert { type: "json" }

// console.log( pkg.name )
// console.log( pkg.version )
// console.log( zones.map(zone => zone.name) )

const resolvers = {
  Query: {

    ping: () => 'pong',
    version: () => pkg.name + ' ' + pkg.version,

    zones: () => zones,
    // zone: ( _parent: any, args: any ): Zone => {
    //   return zones.find( zone => zone.id === args.id )
    // },

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
