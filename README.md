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
- Order of operation: Start Monitor, Main, Client. [This is important, if you dont' start up in the correc order it will break].
- Client detects disconnect when sending messages [if you guys want, try implementing monitor signals client to switch to available server, currently it is set up for the monitor to just inform and report, the client will switch once you try to send messages, and it doesn't receive a message back]
