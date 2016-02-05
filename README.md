# bowlorama-history-tracker

Part of the bowlorama demo application. This piece provides connectivity to a DynamoDB instance to manage
the bowling game state. Each running game has a unique game ID, which can contain any number of players and their 
ball history. 
 
Two functions are exposed from this library to Lambda:
- bowlorama-player-history, which returns the current game history for a player
- bowlorama-append-ball-to-history, which takes the most recent ball value and updates the players history in DynamoDB

Note: when executing as a Lambda function, bowlorama-append-ball-to-history will call the remote bowlorama-score
API to calculate the current score, and store that with the ball history. 

The other pieces of Bowlorama are in the following repos:
https://github.com/pdxdan/bowlorama-calculator
https://github.com/pdxdan/bowlorama-jsclient

## Usage

The DynamoDB connectivity here relies on the standard AWS configuration mechanisms in your runtime environment. 
On your local machine this can be managed with the AWS CLI and the 'aws configure' command. 
See here for more information: http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html

Once deployed to Lambda, the runtime will leverage any IAM Role permissions that you have configured for that function.  

## Testing
The integration tests expect connectivity to a DynamoDB instance. 
You can use the real thing, or configure your environment to use a local instance.
On OSX you can install that with "brew install dynamodb-local"