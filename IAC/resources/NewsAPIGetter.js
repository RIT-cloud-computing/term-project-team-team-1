const https = require('https');

const apiKey = "e82ea4693fd047118b7e19d49505a870";

// Starter keywords we want on every news api get
const starterKeywords = ["Covid-19", "Covid", "Pandemic", "Coronavirus"];

// Creates a string of all the keywords in the array in parentheses, separated by the separator
const createGrouping = (keywordsArray, separator) => `(${keywordsArray.reduce((prev, cur) => prev + ` ${separator} ` + cur)})`;

const starterKeywordsGrouping = createGrouping(starterKeywords, "OR");

/** 
 * Input parameters: 
  *   event.keywords = an array of additional keywords to search with (typically user locations to search for)
  *   event.fromDate = the last date from which articles can be found from, leave undefined to search articles from whenever
  *   event.userEmails = an array of emails of users with the location being searched for
 */
exports.handler = async (event) => {
    const wrappedEventKeywords = event.keywords.map(keyword => `"${keyword}"`);
    const queryPhrase = `${starterKeywordsGrouping} AND ${createGrouping(wrappedEventKeywords, "AND")}`;
    console.log(`Query phrase used for get request: ${queryPhrase}`);
    
    const fromDate = event.fromDate || '';
    const uri = `https://newsapi.org/v2/everything?q=${queryPhrase}&apiKey=${apiKey}&language=en&sortBy=relevancy&from=${fromDate}`;
  
    const response = await new Promise((resolve, reject) => {
        const req = https.get(uri, function(res) {
          let dataString = '';
          res.on('data', chunk => {
            dataString += chunk;
          });
          res.on('end', () => {
            resolve({
                statusCode: 200,
                body: JSON.stringify(JSON.parse(dataString))
            });
          });
        });
        
        req.on('error', (e) => {
          reject({
              statusCode: 500,
              body: 'Error calling news api'
          });
        });
    });
    
    response.userEmails = event.userEmails;
    
    return response;
};

