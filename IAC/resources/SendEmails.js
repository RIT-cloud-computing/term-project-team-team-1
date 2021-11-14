var aws = require("aws-sdk");
var ses = new aws.SES({ region: "us-east-1" });
exports.handler = async function (event) {
    let allArticles = event.Payload.articleResults;
    function compareBySentiment(a,b) {
        if (a.sentimentResult.SentimentScore.Positive - a.sentimentResult.SentimentScore.Negative >
        b.sentimentResult.SentimentScore.Positive - b.sentimentResult.SentimentScore.Negative)
            return -1;
        if (a.sentimentResult.SentimentScore.Positive - a.sentimentResult.SentimentScore.Negative <
        b.sentimentResult.SentimentScore.Positive - b.sentimentResult.SentimentScore.Negative)
            return 1;
        return 0;
    }
    allArticles.forEach(function (article) {
        if(article.sentimentResult.SentimentScore.Neutral > .9) {
            article.sentimentResult.SentimentScore.Positive = .5;
            article.sentimentResult.SentimentScore.Negative = .5;
        }
    });
    allArticles = allArticles.slice(0, 5).sort(compareBySentiment);
    let html = "<div style=\"background-color: #383838; padding: 30px;\"><br><h1 style=\"color: white; margin: 15px;\">Your Daily Cov-Alert Snapshot: </h1><ol style=\"list-style: none;\">";
    allArticles.forEach(function (article) {
        let score = 2*(100*(article.sentimentResult.SentimentScore.Positive/(article.sentimentResult.SentimentScore.Positive + article.sentimentResult.SentimentScore.Negative)) - 50);
        let scoreLabel = "";
        if(score > 25) {
            scoreLabel = "<h2 style=\"width: 175px; display: inline-block; color: green\">&#128513;&nbsp;" + Math.round(score) + " % Positive </h2>";
        } else if(score > 0) {
            scoreLabel = "<h2 style=\"width: 175px; display: inline-block; color: yellow\">&#128512;&nbsp;" + Math.round(score) + " % Positive </h2>";
        } else if(score === 0) {
            scoreLabel = "<h2 style=\"width: 175px; display: inline-block; color: yellow\">&#128528;&nbsp; Neutral </h2>";
        } else if(score > -25) {
            scoreLabel = "<h2 style=\"width: 175px; display: inline-block; color: yellow\">&#128533;&nbsp;" + -Math.round(score) + " % Negative </h2>";
        } else if(-25 > score) {
            scoreLabel = "<h2 style=\"width: 175px; display: inline-block; color: red\">&#128552;&nbsp;" + -Math.round(score) + " % Negative </h2>";
        }
        html += "<li>" + scoreLabel + "<a style=\"text-decoration: none; font-size: 15px; color: aqua;\" href=\"" + article.url + "\">" + article.title + "<a>" +
            "<p style=\"margin-left: 150px; color: white;\">" + article.description.replace(/<[^>]*>?/gm, '') + "</p>" +
            "</li>";
    });
    html += "</ol><br></div>";
    var params = {
        Destination: {
            ToAddresses: event.Payload.userEmails,
        },
        Message: {
            Body: {
                Html: {
                    Charset: "UTF-8",
                    Data: html
                },
            },
            Subject: { Data: "Cov-Alert" },
        },
        Source: "covalerter@gmail.com",
    };
    return ses.sendEmail(params).promise()
};
