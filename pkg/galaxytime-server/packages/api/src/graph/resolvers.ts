import { AddZoneInput, Zone } from './graph.js'
import zones from '../data/data.json' assert { type: "json" }

console.log( zones.map(zone => zone.name) )

const resolvers = {
  Query: {
    ping: () => 'pong',
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
