import express from "express"
import log from "@lab75/logger"

import { readFileSync } from 'fs'
import dotenv from 'dotenv'
import pkg from '../package.json' assert { type: "json" }

import { ApolloServer } from '@apollo/server'
import { startStandaloneServer } from "@apollo/server/standalone"
import resolvers from './graph/resolvers.js'

log( 'BOOTING...')
log( '🪐 SPACETIME', pkg.name, pkg.version )

// GRAPHQL SERVER

const typeDefs = readFileSync('./src/graph/schema.graphql', { encoding: 'utf-8' })
const server = new ApolloServer({ typeDefs, resolvers })
const { url } = await startStandaloneServer( server, { listen: { port: 4000 } })
log(`🪐 GRAPH API listening on ${url}`)

// REST SERVER

const app = express()
app.use((req, res) => {
	log("Received request for URL:", req.url)
	res.end("Hello from SpaceTime!")
})
app.listen(3000, () => {
	log("🪐 REST API server listening on http://localhost:3000/")
})
