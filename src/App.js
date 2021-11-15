import './App.css';
import covid from './covid.png'
import EssayForm from "./EssayForm";

function App() {
  return (
        <div className="App">
            <div className={"logo"}>
            <img src={covid} className="App-logo" alt="logo"/>
            </div>
            <EssayForm />
        </div>
  );
}



export default App;
