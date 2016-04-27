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

# Developer instructions

Install Maven and JDK.  This was last build with Maven 3.3.9 and JDK
1.8.0\_73 on Windows 10.1601

Run unit tests

    mvn test

Create an HPI file to install in Jenkins (HPI file will be in `target/telegram.hpi`).

    mvn package