## Description
This program contains Player.java, SockBaseClient.java, Game.java, SockBaseServer.java

The server allows unlimited number of clients.
The client can connect to the server and is asked to provide their name
The server greets the client
The client displays the main menu
If the client selects 1 the server sends the leader board and the client displays it and then the main menu
If the client selects 2 the server sends the first task and the image of the current game
If the client selects 3 the server sends a goodbye message and the client dissconnects

### GamePlay
if the client completes the task correctly tiles are revealed and the updated image is sent to all clients
if the client is incorrect the server sends the same task with the message "Try Again"
if the client types exit the server sends a goodbye message and the client dissconnects
if the image is revealed all clients in the game win and the leaderboard is updated and they go back to main menu

### Tile Reveal
If a client has no wins they reveal 8 tiles per task
For each win the client reveals an extra 8 tiles per task
The maximum tiles per task is 35

### The procotol
You will see a response.proto and a request.proto file. You should implement these in your program. 
Protocol description
Request:
- NAME: a name is sent to the server, fields
	- name -- name of the player
	Response: GREETING, fields 
			- message -- greeting text from the server
- LEADER: client wants to get leader board
	- no further data
	Response: LEADER, fields 
			- leader -- repeated fields of Entry
- NEW: client wants to enter a game
	- no further data
	Response: TASK, fields
			- image -- current image as string
			- task -- current task for the cilent to solve
- ANSWER: client sent an answer to a server task
	- answer -- answer the client sent as string
	Response: TASK, fields 
			- image -- current image as string
			- task -- current task for the cilent to solve
			- eval -- true/false depending if the answer was correct
	OR
	Response: WON, fields
			- image -- competed image as string
- QUIT: clients wants to quit connection
	- no further data
	Response: BYE, fields 
		- message -- bye message from the server

Response ERROR: anytime there is an error you should send the ERROR response and give an appropriate message. Client should act appropriately
	- message

### How to run


#### Default 
Server is Java
Per default on 9099
runServer

You have one example client in Java using the Protobuf protocol

Clients runs per default on 
host localhost, port 9099
Run Java:
	runClient


#### With parameters:
Java
gradle runClient -Pport=9099 -Phost='localhost'
gradle runServer -Pport=9099

## Screencast

https://youtu.be/owy-qqYVas8

## Fulfilled Requirements
	1. Runs through gradle
	2. Implements Protobuf
	3. Main Menu
	4. Leader board
	5. Leader board is consistent
	6. Start game
	7. Multiple clients in the same game
	8. Win and return to main menu
	9. Server sends task and checks it
	10. Client presents and tasks are small
	11. game quits gracefully
	12. Server does not crash when client disconnects
	13. Good and fun tasks
	14. Server Running on aws and posted in #servers
	15. good set up for tile reveal
	16. test 3 other servers and comment on slack
	17. pushes image updates
	18. user can exit by typing exit in game
