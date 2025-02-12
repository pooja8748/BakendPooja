require('dotenv').config()
const express = require('express');
const app = express();
const port = 4000;


app.get('/', (req, res) => {
    res.send('about.html');
});

app.get('/twitter', (req, res) => {
    res.send('Hello pooja!');
});

app.get('/login', (req, res) => {
    res.send('<h1>Please Login</h1>');
}); 

app.get('/youtube', (req, res) => {
  res.send('<h2>Hello Youtube!</h2>');
});

app.listen(process.env.PORT, () => {
    console.log(`Example app listening at http://localhost:${port}`);
});