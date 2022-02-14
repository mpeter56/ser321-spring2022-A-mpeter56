# Assignment 4 Activity 1
## Description
Activity 1 has Performer.java, Server.java, ThreadedServer.java, ThreadPoolServer.java, JsonUtils.java, NetworkUtils.java
Server.java allows 1 user to connect at a time and runs Performer
ThreadedServer allows unlimited users to connect at a time and runs Performer
ThreadPoolServer allows a set number of users to connect at a time and runs Performer

The client connects to a server and communicates with json.

Performer allows clients to:
add strings to the string list
pop strings from the string list
display the string list
count the items in the string list
switch items in the string list
quit

## Protocol

### Requests
request: { "selected": <int: 1=add, 2=pop, 3=display, 4=count, 5=switch,
0=quit>, "data": <thing to send>}

  data <string> add
  data <> pop
  data <> display
  data <> count
  data <int> <int> switch return a string

### Responses

sucess response: {"type": <"add",
"pop", "display", "count", "switch", "quit"> "data": <thing to return> }

type <String>: echoes original selected from request
data <string>: add = new list, pop = new list, display = current list, count = num elements, switch = current list


error response: {"type": "error", "message"": <error string> }
Should give good error message if something goes wrong


## How to run the program
### Terminal
Base Code, please use the following commands:
```
    For Server, run "gradle runTask1 -Pport=9099 -q --console=plain"
```
```
    For ThreadedServer, run "gradle runTask2 -Pport=9099 -q --console=plain"
``` 
```
    For ThreadPoolServer, run "gradle runTask3 -Pport=9099 -q --console=plain" 
``` 
```   
    For Client, run "gradle runClient -Phost=localhost -Pport=9099 -q --console=plain"
```   

## Screencast Link

https://youtu.be/JFXvIASvZC4

## Fulfilled Requirements
### Task1
	1. add
	2. pop
	3. display
	4. count
	5. switch
### Task2
	1. ThreadedServer
	2. allow unbounded incoming connections
	3. no client blocks
	4. string list is shared
### Task3
	1. ThreadPoolServer
	2. only allow a set number of connections
	3. threadPoolServer
### Gradle
	1. 3 gradle tasks to run servers
	2. gradle uses default values
	3. gradle runClient
	4. a) One for running Task 1: gradle runTask1
	   b) One for running Task 2: gradle runTask2
	   c) One for running Task 3: gradle runTask3
	5. Detailed readme
	6. Screencast



