# Telegram plugin for Jenkins

Started with a fork of the Slack plugin:

https://github.com/tinyspeck/jenkins-slack-plugin

Which was a fork of the HipChat plugin:

https://github.com/jlewallen/jenkins-hipchat-plugin

Which was, in turn, a fork of the Campfire plugin.

# Jenkins Instructions

1. Create a Telegram Bot: https://core.telegram.org/bots#botfather
2. Install this plugin on your Jenkins server
3. Configure it in your Jenkins job and **add it as a Post-build action**.
 
# Get Telegram chat id
1. Add the Telegram BOT to the group.
2. Write something to the BOT with @botname
3. Get the list of updates for your BOT:

  ```
  https://api.telegram.org/bot<YourBOTToken>/getUpdates
  ```
  Example:
   ```
   https://api.telegram.org/botjbd78sadvbdy63d37gda37bd8/getUpdates
   ```
4. Look for the "chat" object

 ```json
 {"update_id":8393,"message":{"message_id":3,"from":{"id":7474,"first_name":"AAA"},"chat":{"id":,"title":""},"date":25497,"new_chat_participant":{"id":71,"first_name":"NAME","username":"YOUR_BOT_NAME"}}}
 ```
 This is a sample of the response when you add your BOT into a group.

5. Use the "id" of the "chat" object to send your messages.


# Developer instructions

Install Maven and JDK.  This was last build with Maven 3.3.9 and JDK
1.8.0\_73 on Windows 10.1601

Run unit tests

    mvn test

Create an HPI file to install in Jenkins (HPI file will be in `target/telegram.hpi`).

    mvn package
