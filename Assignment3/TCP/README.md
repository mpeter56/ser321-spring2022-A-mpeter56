# Assignment 3 TCP

## Description
This program can run a server by using 'gradle TCPServer' and a client using 'gradle TCPClient'
The server will wait for a client to connect. When the client connect it will open a GUI
with a window for pictures and an output panel, with a text field and a submit button.
The server will automatically send the 'hi there' picture and ask the user for their name.
When the user responds with their name the server will greet the user by name and go to the main menu.
From the main menu the user can select 1 to see the leader board or 2 to play a game.
The leader board will display players names and overall scores ranked from highest to lowest.
To get back to the main menu the client must re-enter thier name.
Once 2 is selected in the main menu the server Will send over the first quote picture.
The client sends back a guess at who said the quote.
If the client is correct the server will update the score, track the number of correct answers and send
the next quote.
If the client is incorrect the server will send the string 'Try Again'.
If the client types next the server will send the first quote for the next character, if last character
the server will send the first quote for the first character, the score will be updated to score - 2.
If the client types more the server will send the next quote for the same character, if there are no
more quotes availible the server will send the string "No more quotes for this character"
If the client answers correctly on the first quote they will recieve 5 points, second quote 4, third 
quote 3 and last quote 1. 
If the client answers correctly three times before a minute they win and the server will send a winning
image.
The client can either type their name to get to the main menu or type quit to quit.
The server will update the leader board with the client's high score.
If the client does not answer three correctly within a minute they lose. The server will send a losing 
image and the client can either enter their name to go to the main menu or they can enter quit to quit.

### Checklist of Fullfilled Requirements

1. Ask for name
2. Receive and greet by name
3. Choice to see leader board or play the game
4. Leader board shows all players that have played and points(persistent if server is restarted)
5. Server sends first quote if start game is selected
6. Client can either guess, type "more" or "next
7. Server checks clients guess and acts accordingly.
8. Server sends over next quote for character if more is selected
9. Server sends over first quote for next character if next is selected
10.If three correct guesses and timer is less than a minute server sends winning image
11.If timer runs out the server sends over a losing image
12.Server keeps track of points
13.Server displays points and adds them to their old points on leaderboard
14.Evaluations of input happen on server side
15.Protocol has headers and paylodes
16.Error handling is provided
17.Players can get to the main menu by entering their name again or can quit typing quit

### How To Run
#### Server
To run the server open the TCP file and enter gradle build.
Then enter gradle TCPServer(you can either use the default port 8080, or add " -Pport='port'" at the end)
#### Client
To run the client open the TCP file and enter gradle build.
Then enter gradle TCPClient(you can either use the default port 8080 and default IP address "localhost"
or you can add " -Pport='port'" and/or " -Phost='IPAddress'" to the end.
Once the client is runnin a GUI window will open with a "Hi There" image and an output panel asking the 
client's name.
Enter your name
Then in the output panel there will be 2 options enter 1 to see the leader board or enter 2 to play a game
If 1 is selected then the leader board will appear in the output panel along with directions to get back
to the main menu.
Type your name to get back to the main menu.
If 2 is selected then the server will send the first quote and the client can either enter a guess, next,
or more.
If the guess is correct the server will adjust the points and send over the next quote.
If the guess is incorrect the server will send try again into the output panel and the client can guess again.
If the client enters more, they will see the next quote for the same character.
If the client enters next, they will see the first quote for the next character and their score will be 
updated to score-2 points.
If the client guesses 3 correctly in under a minute they win and their score will be added to the leader board.
If the client does not guess 3 correctly in under a minute they lose.
In both the winning and losing screen the client can either enter their name to return to the main menu or
the client can enter quit to quit. 

### UML
https://github.com/mpeter56/ser321-spring2022-A-mpeter56/blob/main/Assignment3/TCP/img/UMLdiagram.JPG
![alt text](img/UMLdiagram.JPG)

### Protocol
The protocol uses header datatype 1, 2, and 3.
If datatype is 1 then the payload is a string. 
If datatype is 2 then the payload is an image.
If datatype is 3 then the payload is an int.
The payload has a header that tells the program what it contains e.g. selected, name, etc.

### Robust

When the server asks the client's name, the client can enter anything and the server will accept it as a name.
In the main menu the client should enter either "1" or "2", if the client enters something else the server will
respond with "Invalid selection: " + message.get("selected") + " is not an option" and will reload the mainmenu
with the directions for input.
If the client is in the leaderboard they should type their name to get to the main menu, this is printed in the
leaderboard, if the client selects something else it will print "try again"
During the game if the client enter's anything that is not a valid option, the server will send "try again"
any IOException is caught and will print out the stack trace as well as a message describing what exactly the 
server was attempting to do: e.g. "Could not send win image to client"
If the fileNotFoundException occors for the leaderboard it will be caught and the leader board will be created.
If the connection is lost the server will catch the exception and print Client disconnect and then close the socket.

###Protocol Change from TCP to UDP

####TCP
TCP Server uses a server socket
TCP Client uses a Socket
both client and server use input and output streams to send data to eachother
TCP Server calls socket.accept(); to do a blocking wait for a client to connect

####UDP
UDP Server uses a Datagram socket
UDP Client uses a Datagram socket
UDP Server and client use NetworkUtils.Tuple to recieve data through the socket.
UDP client sends a message "hello" to the server and then the server will send over the first message.
UDP client and server use NetworkUtils.Send to send information.
UDP client and server convert to JSONObject using Tuple.Payload as paramaters in the JSONUtils.fromByteArray.
