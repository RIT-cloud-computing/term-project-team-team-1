import React from "react";
import {Form} from "reactstrap";

class EssayForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            value: ''
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange(event) {
        this.setState({value: event.target.value});
    }

    handleSubmit(event) {
        alert('Success! You are now registered for CovAlert' + this.state.value);
        event.preventDefault();
    }

    render() {
        return (
            <form onSubmit={this.handleSubmit}>
                <label>
                    <Form className="formfield">
                        <label htmlFor="textarea">Enter Email:</label>
                        <textarea onChange={this.handleChange} name="Email" cols={"50"} rows={"1"}>
                        </textarea>
                    </Form>
                </label>
                <label>
                    <Form className="formfield">
                        <label htmlFor="textarea">Enter Location:</label>
                        <textarea onChange={this.handleChange} name="Location" cols={"50"} rows={"1"}>
                        </textarea>
                    </Form>
                </label>
                <div className={"Register"}>
                <input type="submit" value="Submit"/>
                </div>
            </form>

        );
    }
}

export default EssayForm;