const AWS = require('aws-sdk');
const docClient = new AWS.DynamoDB.DocumentClient();
const stepClient = new AWS.StepFunctions();

const stateMachineArn = ""; //should look like "arn:aws:states:us-east-1:xxxxxxxxxxxx:stateMachine:CovAlertStateMachinexxxxxxxx-xxxxxxxxxxxx"

const docParams = {
  TableName : 'SWEN514__User_Registration_Log'
};

async function listItems(){
  try {
    const data = await docClient.scan(docParams).promise();
    return data.Items;
  } catch (err) {
    return err;
  }
}

function getLastWeekDateString() {
  const lastWeekDate = new Date();
  lastWeekDate.setDate(lastWeekDate.getDate() - 7);
  return lastWeekDate.toISOString();
}

function parseLocationKeyword(locationKey) {
  const keySplit = locationKey.split('#');
  return `${keySplit[1]}, ${keySplit[3]}`;
}

exports.handler = async (event, context) => {
  try{
    const items = await listItems();
    const fromDate = getLastWeekDateString();
    for(const item of items) {
      const locationKeyword = parseLocationKeyword(item.location);
      const emails = item.emails.values;
      const stepParams = {
        stateMachineArn,
        input: JSON.stringify({
          keywords: [locationKeyword],
          userEmails: emails,
          fromDate,
        })
      };
      const result = await stepClient.startExecution(stepParams).promise();
      console.log(locationKeyword, 'result:', result)
    }
    return { 
      status: 200
    };
  } catch (err) {
    return {
      errorMessage: err.toString()
    }
  }
};