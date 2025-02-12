require('dotenv').config();
const express = require('express');
const path = require('path');

const app = express();
const port = process.env.PORT || 4000; // Use environment variable or default to 4000

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'about.html')); // Serve the correct HTML file
});

app.get('/twitter', (req, res) => {
    res.send('Hello Pooja!');
});

app.get('/login', (req, res) => {
    res.send('<h1>Please Login</h1>');
});

app.get('/youtube', (req, res) => {
    res.send('<h2>Hello Youtube!</h2>');
});

app.listen(port, () => {
    console.log(`Example app listening at http://localhost:${port}`);
});
