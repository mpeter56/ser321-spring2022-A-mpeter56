# Assignment 6

## Screencast

https://www.youtube.com/watch?v=XzLcCsmjFXc

<iframe width="560" height="315" src="https://www.youtube.com/embed/XzLcCsmjFXc" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Description

This program consists of classes:

	1. auto.java - This is the client that runs automated test for the program.
	2. Client.java - This is the client that connects to the registry and runs 
	methods on servers with the requested method.
	3. EchoClient.java - This is the main client which users can use to access a local server
	4. Node.java - This is the main server which does not connect to a registry.
	5. NodeService.java - This is the server which can connect to the registry.
	6. JokeImpl.java - This is the starter joke implimentation
	7. EchoImpl.java - This is the starter echo implimentation
	8. HometownsImpl.java - This is the first protofile I implimented. It allows clients to
	get the list of all the students and their hometowns, search by city, or add a new entry
	to the hometowns list.
	9. TimerImpl.java - This is the second protofile I implimented. It allows clients to start
	a timer, check a timer, close a timer, and get the list of currently running timers.
	10. CalcshapeImpl.java - This is the protofile I designed and implimented(proto will be 
	described later.) This allows clients to get a list of all shapes availible for calculation
	, calculate area of a shape, calculate parimiter of a shape, and add a new shape to the 
	list.
	11. Register.java - This is used for the registry
	12. RegistryAnswerImpl.java - This is used for the registry
 
This program runs a registry that can connect to multiple servers that offer different services.
Clients can connect to the registry and ask which server offers a service, then the client can
connect to that server to call on its methods using gRPC to call the methods directly.

This program can also be run with a single server and a single client, where the client can connect
to the server and run methods on the server using gRPC.

This program can also be run with a auto mode which runs several test calls to methods from the client
to the server.

## How to run

gradle runRegistry 
	runs the registry with default values

gradle runClientJava
	runs the client that does not connect to the registry and connects directly to the server.

gradle runClient
gradle runClient -Phost=localhost -Pport=8000 -Pauto=1
	runs the client that performs auto test calls to the server and does not connect to the
	registry.

gradle runClient2
	runs the client that connects to the registry

gradle runNode
	runs the server that does not connect to the registry

gradle registerServiceNode
	runs the server that connects to the registry

## How to work

	The gradle runClient -Phost=localhost -Pport=8000 -Pauto=1 will just run all the test and
	terminate allowing you to scroll through and read the printout descriptions of the test
	and the results of the test.

### Main menu

	The gradle runClientJava should be run with gradle runNode and will open up onto the main
	menu:

	1: Timer services
	2: Home Town services
	3: Joke services
	4: Echo services
	5: Calc Shape services
	0: Quit

### Timer menu

	option 1 will take you to the Timer Menu:

	1: start timer - will ask for a timer name - success or error message will be displayed
	2: check timer - will ask for a timer name - timer name and time or error message will be
		displayed
	3: close timer - will ask for a timer name - success or error message will be displayed
	4: display timer list - does not ask for any input - timer list will be displayed
	5: main menu - returns to the main menu

### Home Town menu

	option 2 will take you to the Home Town Menu:

	1: read hometown list - hometown list will be displayed
	2: search for classmates in a hometown - will ask for city name - will display list or error
	3: write a new hometown entry - will ask for name, then city, then state - will display
		success or error message
	4: main menu - will return to the main menu

### Joke Menu

	option 3 will take you to the Joke Menu:

	1: get jokes - will ask you for a number - will display jokes or error message
	2: set jokes - will ask you for a joke - will display success or error message
	3: main menu - will return to the main menu

### Echo service

	option 4 will take you to the Echo service (no menu for this one)
	You can either enter the message to echo or enter 'back' to return to the main menu
	if message is entered the response from server will be displayed.

### Calc shape menu

	option 5 will take you to the calc shape menu:

	1: get list of available shapes - will display list, including directions, and equations
	2: calculate area - will ask for shape name, then width, then height - will display area
		or error message
	3: calculate parameter - will ask for shape name, then width, then height - will display
		parameter or error message
	4: add or update shape - will ask for shape name, then area equation(can only use lower
	case width and height for variables, then will ask for parameter equation(same for variables)
	then a description of how to use the equations( which parameters are for what)
	- will return either a success or error message
	5: main menu - will return to the main menu

### Quit
	
	will print good bye and quit

## My proto file description
	
	my protofile is called calcshape.proto

	It contains 4 methods:
		area 
			input - CalcshapeAreaRequest 
			returns - CalcshapeResponse
		parameter
			input - CalcshapeParameterRequest
			returns - CalcshapeResponse
		listShapes
			input - CalcshapeListRequest
			returns - CalcshapeListResponse
		addShape
			input - CalcshapAddShapeRequest
			returns - CalcshapeAddShapeResponse

	method structures:

		// Request to add a new shape to the calculations list
		// requires shape to have name, directions, areaEquation, parameterEquation
		message CalcshapeAddShapeRequest
			Shape shape = 1 // A shape object

		// response to tell if addShape was successfull
		message CalcshapeAddShapeResponse
			bool isSuccess = 1 // bool true if request was successfully executed, false if failed
			string error = 2 // error message

		// Request list of all shapes available for calculation
		message CalcshapeListRequest
			Empty empty = 1 // just an empty place holder

		// List all shapes available for calculation
		message CalcshapeListResponse
			repeated Shape shapes = 1 // each shape in the list of availible shapes

		// The response message
		message CalcshapeResponse
			bool isSuccess = 1 // bool true if request was successfully executed, false if failed
			double solution = 2 // the calculated solution
			string error 3 // error message

		// The Area Request message
		// requires shape to have name, width, height(sometimes ignored)
		message CalcshapeAreaRequest
			Shape shape = 1 // A shape object

		// The Parameter Request message
		// requires shape to have name, width, height(sometimes ignored)
		message CalcshapeParameterRequest
			Shape shape = 1 // A shape object

		// The shape object
		message Shape
			string name = 1; // name of the shape
 			double height = 2; // height of the shape
  			double width = 3; // width of the shape
  			string directions = 4; // directions on what arguments are needed for the shape.
  			string areaEquation = 5; // equation for calculating area of shape(use lowercase 'width' and 'height')
  			string parameterEquation = 6; //equation for calculating parameter of shape(use lowercase 'width' and 'height')
	
## requirements


### Task 1

	1. yes gradle runNode and gradle runClientJava run correctly
	2. Timer and hometowns were both correctly implemented
	3. Menu system implemented
	4. gradle runClient -Phost=host -Pport=port -Pauto=1 works and runs the auto test client
	5. Server and client have been thoroughly tested and use error handling

### Task 2

	1. calcshape has 4 different request
	2. each request uses an input
	3. responses return different data
	4. listShapes returns a response with a repeated feild
	5. the list of available shapes is stored in a text file
	6. calculations are basic, but the add shape feature is pretty neet, because it uses strings to calculate the equations

### Task 3.1
	
	1. created Client.java and Node.java with gradle tasks
	2. uncommented code
	3. registered services on NodeService in the registry
	4. Client connected to the registry
	5. reprogrammed client to ask for server that offers the service then connects to that service

### Task 3.2

	


