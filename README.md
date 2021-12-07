
## Installation
**Requirements**
- maven
- java 17
- git

```
git clone https://github.com/mspark/aoc-discord
cd aoc-discord
mvn package spring-boot:repackage
```
## Running
**Requirements**
- one custom AOC leaderboard which isn't in use (used for verification)
- one private AOC leaderboard which is monitored by the bot

Navigate to the JAR file (per default this is `aoc-discord/build/`
Create configuration file inside the directory with the jar and fill them with values:

```
bot:
  prefix: "!"
  apiTokens:
    - 
aoc: 
  dailyChannelId: 
  privateLeaderboardId:
  inviteCode: 
  verificationLeaderboardId: 
  session: 
  
```

Start it with `java -jar target/aoc-1.0-spring-boot.jar` (maybe you have to change the version which is inside the jar file)