# Comp-370-Mini-Project-
This is a Server redundancy project for comp 370 Group #6 [Fusion Five] 

Link to google is on Whatsap groupchat for details. 

I want to make this repo private, but I need everyone to have a github account first (so I can set up a collaboration). 

Thanks. 
Add files via upload
Updated IBKs working file. 

updates: 
- Fixed shutoff
- Implemented Backup 2
- Implemented Promotion 
- Updated Monitor, and Client to handle promotion
- Implemented commands under main class for simulation (server instance shutoffs, reboot, to observe promotion) 
- Order of operation: Start Monitor, Main, Client. [This is important, if you dont' start up in the correct order it will break].
- Client detects disconnect when sending messages [if you guys want, try implementing monitor signals client to switch to available server, currently it is set up for the monitor to just inform and report, the client will switch once you try to send messages, and it doesn't receive a message back]



Updates: 

1. Ports:

Server / Client Ports 
PRIMARY_PORT = 8090;
BACKUP_PORT = 8089;
BACKUP_PORT2 = 8088;

Monitor / Server Port for Hearbeats
HEARTBEAT_PORT = 9000

Monitor / Client Port for Promotion 
CLIENT_API_PORT = 9001

2. Functions:

ServerProcess: 
Sets Port instance 
Sends heartbeat (continiously) with the port # and time stamp
Accepts Client Message, and responds to notify message received
Lets client know if it is not communicating with the current "primary" 
Stop function (to end server instance) 

[Primary, backup and backup2] 
Extend the funciton above + add a promotion message 

Monitor: 
Receives heartbeats [with all the metadata, ie port#, status, and time stamp] 
Updates primary status and signals to Main if primary death is noticed, and also signals to client so that promotion can be aligned. 
uses a funciton called rundeathchecker, that ensures proper promotion logic, and decides which server to set to primary (based on highest port #) 

Client: 

Sends messages to primary server
Switchces ports upon receiving notificaion from monitor 


Main: 
Runs instances of Serverprocess: 
Temporary interface for testing [bash to be implmented]
