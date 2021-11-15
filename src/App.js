import './App.css';
import covid from './covid.png'
import {Col, Row, Button} from 'reactstrap'

function App() {
  return (
        <div className="App">
            <div className={"logo"}>
            <img src={covid} className="App-logo" alt="logo"/>
            </div>
            <p className="formfield">
                <label htmlFor="textarea">Enter Name: </label>
                <textarea name="Name" cols={"50"} rows={"1"}>
                </textarea>
            </p>
            <p className="formfield">
                <label htmlFor="textarea">Enter Email: </label>
                <textarea name="Email" cols={"50"} rows={"1"}>
                </textarea>
            </p>
            <p className="formfield">
                <label htmlFor="textarea">Enter Location: </label>
                <textarea name="Location" cols={"50"} rows={"1"}>
                </textarea>
            </p>
        <div className="Register">
            <Button> Register </Button>
        </div>
        </div>
  );
}

export default App;
