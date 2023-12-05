import express from "express"
import log from "@lab75/logger"

const app = express();

app.use((req, res) => {
	log("Received request for URL:", req.url);
	res.end("Hello World!");
})

app.listen(3000, () => {
	log("The server is running on port http://localhost:3000!");
})
