import './App.css';
import covid from './covid.png'
import { useState } from 'react';
import axios from 'axios';
import swal from 'sweetalert';

const url = 'https://qsd6yc2he8.execute-api.us-east-1.amazonaws.com/test'; //not sure if this is working, also not sure how to make sure this url is right once using IAC

function App() {
    const [email, setEmail] = useState('');
    const [city, setCity] = useState('');
    const [region, setRegion] = useState('');

    const buttonDisabled = email === '' || city === '' || region === '';

    const onSignUp = () => {
        axios.post(url, {
            email,
            city,
            region
        });
        setEmail('');
        setCity('');
        setRegion('');
        swal('Signed up for Cov-Alert!', '', 'success');
    };
  
    return (
        <div className="App">
            <h2> Cov-Alert Sign Up </h2>
            <h4> Sign up now for weekly updates on how Covid-19 is trending in your area </h4>

            <div className={"logo"}>
                <img src={covid} className="App-logo" alt="logo"/>
            </div>

            <div className="inputs">
                <div className="input-row">
                    <label> Email: </label>
                    <input type="text" value={email} onChange={(event) => setEmail(event.target.value)} />
                </div>

                <div className="input-row">
                    <label> City: </label>
                    <input type="text" value={city} onChange={(event) => setCity(event.target.value)} />
                </div>

                <div className="input-row">
                    <label> State/Country: </label>
                    <input type="text" value={region} onChange={(event) => setRegion(event.target.value)} />
                </div>
            </div>
            
            <button onClick={onSignUp} disabled={buttonDisabled}> Sign Up </button>
        </div>
  );
}

export default App;
