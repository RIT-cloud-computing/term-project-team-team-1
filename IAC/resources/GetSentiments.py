import json
import boto3
def lambda_handler(event, context):
    comprehend = boto3.client("comprehend")
    response = {
        'statusCode': 200,
        'articleResults': [],
        'userEmails': event['Payload']['userEmails']
    }
    api_response = json.loads(event['Payload']['body'])
    for article in api_response['articles']:
        article_text = article['title'] + '\n' + article['description'] + '\n' + article['content']
        sentiment_response = comprehend.detect_sentiment(Text=article_text, LanguageCode="en")
        sentiment_result = {
            'Sentiment': sentiment_response['Sentiment'],
            'SentimentScore': sentiment_response['SentimentScore']
        }
        article_result = {
            'url': article['url'],
            'title': article['title'],
            'description': article['description'],
            'sentimentResult': sentiment_result
        }
        response['articleResults'].append(article_result)
    return response;
