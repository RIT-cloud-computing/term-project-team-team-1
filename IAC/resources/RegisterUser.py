import json
import boto3

TABLE_NAME = "SWEN514__User_Registration_Log"

def lambda_handler(event, context):
    
    dynamodb = boto3.client("dynamodb")
    
    body = json.loads(event["body"])
    
    email = body["email"]
    city = body["city"]
    region = body["region"]
    location = f"CITY#{city}#REGION#{region}"
    
    # Check If Location Exists
    try:
        item = dynamodb.get_item(
            TableName=TABLE_NAME,
            Key={
                "location" : {
                    "S" : location
                }   
            }
        )["Item"]
    # If Location Not Found, Add New
    except KeyError:
        response = dynamodb.put_item(
            TableName=TABLE_NAME,
            Item={
                "location" : {
                    "S": location  
                },
                "emails" : {
                    "SS": [email]
                }
            }
        )
        return {
            'statusCode': 200,
            'body': json.dumps("New Location Added")
        }

    # Assuming Location Exists, Check If Email Exists At Location
    emails = item["emails"]["SS"]
    if email in emails:
        return {
            'statusCode': 400,
            'body': json.dumps("Email at Location Exists")
        }
    else:
        # Add Email to Emails
        emails.append(email)
        # This Will Overwrite
        response = dynamodb.put_item(
            TableName=TABLE_NAME,
            Item={
                "location": {
                    "S": location
                },
                "emails": {
                    "SS": emails
                }
            }
        )
        return {
            'statusCode': 200,
            'body': json.dumps("Added Email to Location")
        }