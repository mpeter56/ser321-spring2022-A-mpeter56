package example.grpcclient;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import service.*;
import test.TestProtobuf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Client that requests `parrot` method from the `EchoServer`.
 */
public class auto {
	
  private static ManagedChannel channel;
	
  private final EchoGrpc.EchoBlockingStub blockingStub;
  private final JokeGrpc.JokeBlockingStub blockingStub2;
  private final TimerGrpc.TimerBlockingStub blockingStub4;
  private final HometownsGrpc.HometownsBlockingStub blockingStub5;

  /** Construct client for accessing server using the existing channel. */
  public auto(Channel channel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's
    // responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to
    // reuse Channels.
    blockingStub = EchoGrpc.newBlockingStub(channel);
    blockingStub2 = JokeGrpc.newBlockingStub(channel);
    blockingStub4 = TimerGrpc.newBlockingStub(channel);
    blockingStub5 = HometownsGrpc.newBlockingStub(channel);
  }
  
  public void askForStartTimer(String name) {
	  TimerRequest request = TimerRequest.newBuilder().setName(name).build();
	  TimerResponse response;
	  try {
		  response = blockingStub4.start(request);
	  }catch (Exception e) {
		  System.err.println("RPC failed: " + e.getMessage());
		  return;
	  }
	  
	  if(response.getIsSuccess()) {
		  System.out.println(name + " Timer was successfully started!");
	  }else{
		  System.out.println(response.getError());
  	  }
  }
  
  public void askForCheckTimer(String name) {
	  TimerRequest request = TimerRequest.newBuilder().setName(name).build();
	  TimerResponse response;
	  try {
		  response = blockingStub4.check(request);
		  
	  }catch (Exception e) {
		  System.err.println("RPC failed: " + e.getMessage());
		  return;
	  }
	  
	  if(response.getIsSuccess()) {
		  System.out.println(name + " Timer: " + response.getTimer().getSecondsPassed());
	  }else {
		  System.out.println(response.getError());
	  }
  }
  
  public void askForCloseTimer(String name) {
	  TimerRequest request = TimerRequest.newBuilder().setName(name).build();
	  TimerResponse response;
	  
	  try {
		  response = blockingStub4.close(request);
	  }catch (Exception e) {
		  System.err.println("RPC failed: " + e.getMessage());
		  return;
	  }
	  
	  if(response.getIsSuccess()) {
		  System.out.println(name + " Timer was successfully closed!");
	  }else {
		  System.out.println(response.getError());
	  }
  }
  
  public void askForListTimer() {
	  Empty request = Empty.newBuilder().build();
	  TimerList response;
	  
	  try {
		  response = blockingStub4.list(request);
	  }catch (Exception e) {
		  System.err.println("RPC failed: " + e.getMessage());
		  return;
	  }
	  
	  for (Time timers : response.getTimersList()) {
		  if(timers.getName().equals("No timers yet...")) {
			  System.out.println(timers.getName());
		  }else {
			  System.out.println("--- " + timers);
		  }
	    }
  }
  
  public void askForReadHometown() {
	  Empty request = Empty.newBuilder().build();
	  HometownsReadResponse response;
	  
	  try {
		  response = blockingStub5.read(request);
		  if(response.getIsSuccess()) {
			  for (Hometown hometowns: response.getHometownsList()){
				  System.out.println(hometowns);
			  }
		  }else {
			  System.out.println(response.getError());
		  }
	  }catch (Exception e) {
		  System.err.println("RPC failed: " + e.getMessage());
	  }
  }
  
  public void askForWriteHometown(String name, String city, String region) {
	  Hometown home = Hometown.newBuilder()
			  .setName(name)
			  .setCity(city)
			  .setRegion(region)
			  .build();
	  HometownsWriteRequest request = HometownsWriteRequest.newBuilder().setHometown(home).build();
			  
	  HometownsWriteResponse response;
	  
	  try {
		  response = blockingStub5.write(request);
		  
		  if(response.getIsSuccess()) {
			  System.out.println("Successfully wrote hometown entry");
		  }else {
			  System.out.println(response.getError());
		  }
		  
	  }catch (Exception e) {
		  	System.err.println("RPC failed: " + e.getMessage());
	  }
	  
  }
  
  public void askForSearchHometown(String city) {
	  HometownsSearchRequest request = HometownsSearchRequest.newBuilder().setCity(city).build();
	  HometownsReadResponse response;
	  
	  try {
		  response = blockingStub5.search(request);
		  
		  if(response.getIsSuccess()) {
			  for (Hometown hometowns: response.getHometownsList()){
				  System.out.println(hometowns.getName());
			  }
		  }else {
			  System.out.println(response.getError());
		  }
	  }catch (Exception e) {
		  System.err.println("RPC failed: " + e.getMessage());
	  }
  }

  public void askServerToParrot(String message) {
    ClientRequest request = ClientRequest.newBuilder().setMessage(message).build();
    ServerResponse response;
    try {
      response = blockingStub.parrot(request);
    } catch (Exception e) {
      System.err.println("RPC failed: " + e.getMessage());
      return;
    }
    System.out.println("Received from server: " + response.getMessage());
  }

  public void askForJokes(int num) {
    JokeReq request = JokeReq.newBuilder().setNumber(num).build();
    JokeRes response;

    try {
      response = blockingStub2.getJoke(request);
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
    System.out.println("Your jokes: ");
    for (String joke : response.getJokeList()) {
      System.out.println("--- " + joke);
    }
  }

  public void setJoke(String joke) {
    JokeSetReq request = JokeSetReq.newBuilder().setJoke(joke).build();
    JokeSetRes response;

    try {
      response = blockingStub2.setJoke(request);
      System.out.println(response.getOk());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  
  public static void timerMenu(auto client) {
	  try {
		      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		      System.out.println("Timer Menu");
		      System.out.println("1: start timer");
		      System.out.println("2: check timer");
		      System.out.println("3: close timer");
		      System.out.println("4: display timer list");
		      System.out.println("5: main menu");
		      String choice = reader.readLine();
		      
		      if(choice.equals("1")) {
		    	  System.out.println("What would you like to name your timer?");
		          String name = reader.readLine();
		          client.askForStartTimer(name);
		      }else if(choice.equals("2")){
		    	  System.out.println("What is the name of the timer you want to check?");
		          String name = reader.readLine();
		    	  client.askForCheckTimer(name);
		      }else if(choice.equals("3")){
		    	  System.out.println("What is the name of the timer you want to close?");
		    	  String name = reader.readLine();
		    	  client.askForCloseTimer(name);
		      }else if (choice.equals("4")) {
		    	  client.askForListTimer();
		      }else if(choice.equals("5")) {
		    	  mainMenu(client);
		      }else {
		    	  System.out.println("Invalid selection");
		      }
		  }catch (IOException e) {
			  System.err.println("Error in timer menu: " + e);
		  }
	  
	  	  timerMenu(client);
  }
  
  public static void homeTownMenu(auto client) {
	  try {
	      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	
	      System.out.println("Home Town Menu");
	      System.out.println("1: read hometown list");
	      System.out.println("2: search for classmates in a hometown");
	      System.out.println("3: write a new hometown entry");
	      System.out.println("4: main menu");
	      String choice = reader.readLine();
	      
	      if(choice.equals("1")) {
	    	  client.askForReadHometown();
	      }else if(choice.equals("2")){
	    	  System.out.println("What city would you like to search for?");
	    	  choice = reader.readLine();
	    	  client.askForSearchHometown(choice);
	      }else if(choice.equals("3")){
	    	  String name = "", city = "", region = "";
	    	  
	    	  System.out.println("What is your name?");
	    	  choice = reader.readLine();
	    	  if(choice.contains(",")) {
	    		  System.out.println("Entries must not contain a ','");
	    		  homeTownMenu(client);
	    	  }else if(choice.equals("")) {
	    		  System.out.println("You did not provide your name...");
	    		  homeTownMenu(client);
	    	  }else {
	    		  name = choice;
	    	  }
	    	  
	    	  System.out.println("What is the name of your hometown?");
	    	  choice = reader.readLine();
	    	  if(choice.contains(",")) {
	    		  System.out.println("Entries must not contain a ','");
	    		  homeTownMenu(client);
	    	  }else if(choice.equals("")) {
	    		  System.out.println("You did not provide your hometown...");
	    		  homeTownMenu(client);
	    	  }else {
	    		  city = choice;
	    	  }
	    	  
	    	  System.out.println("What region( State or Country) is your hometown in?");
	    	  choice = reader.readLine();
	    	  if(choice.contains(",")) {
	    		  System.out.println("Entries must not contain a ','");
	    		  homeTownMenu(client);
	    	  }else if(choice.equals("")) {
	    		  System.out.println("You did not provide your region...");
	    		  homeTownMenu(client);
	    	  }else {
	    		  region = choice;
	    	  }
	    	  
	    	  client.askForWriteHometown(name, city, region);
	      }else if (choice.equals("4")) {
	    	  mainMenu(client);
	      }else {
	    	  System.out.println("Invalid selection");
	      }
	  }catch (IOException e) {
		  System.err.println("Error in timer menu: " + e);
	  }
  
  	  homeTownMenu(client);
  }
  
  public static void echoMenu(auto client) {
	  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	  try {
		  System.out.println("What message do you want to sent to the server?");
		  System.out.println("Enter 'back' to go back to the main menu.");
		  
		  String message = reader.readLine();
		  
		  if(message.equalsIgnoreCase("back")) {
			  return;
		  }
		  
		  // call the parrot service on the server
	      client.askServerToParrot(message);
	  } catch (Exception e) {
		  System.err.println("Error in echo menu: " + e);
	  }
  }
  
  public static void jokeMenu(auto client) {
	  Scanner reader = new Scanner( System.in );
	  BufferedReader readr = new BufferedReader(new InputStreamReader(System.in));
	  try {
			
		      System.out.println("Joke Menu");
		      System.out.println("1: get jokes");
		      System.out.println("2: set jokes");
		      System.out.println("3: main menu");
		      
		      String choice = readr.readLine();
		      
		      if(choice.equals("1")) {
		    	  System.out.println("How many jokes would you like?");
		    	  if(reader.hasNextInt()) {
		    		  client.askForJokes(reader.nextInt());
		    	  }else {
		    		  System.out.println("Number of jokes must be an integer");
		    	  }
		      }else if(choice.equals("2")){
		    	  System.out.println("What is your joke?");
		          String joke = reader.next();
		    	  client.setJoke(joke);
		      }else if(choice.equals("3")){
		    	  mainMenu(client);
		      }else {
		    	  System.out.println("Invalid selection");
		      }
		  }catch (IOException e) {
			  System.err.println("Error in main menu: " + e);
		  }
		  
		  jokeMenu(client);
  }
  
  public static void mainMenu(auto client) {
	  try {
		// ask the user for input how many jokes the user wants
	      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	
	      System.out.println("Main Menu");
	      System.out.println("1: Timer services");
	      System.out.println("2: Home Town services");
	      System.out.println("3: Joke services");
	      System.out.println("4: Echo services");
	      System.out.println("0: Quit");
	      String choice = reader.readLine();
	      
	      if(choice.equals("1")) {
	    	  client.timerMenu(client);
	      }else if(choice.equals("2")){
	    	  client.homeTownMenu(client);
	      }else if(choice.equals("3")){
	    	  client.jokeMenu(client);
	      }else if (choice.equals("4")) {
	    	  client.echoMenu(client);
	      }else if(choice.equals("0")) {
	    	  System.out.println("Good bye");
	    	  try {
		    	  channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		          System.exit(0);
	    	  }catch (InterruptedException e) {
	    		  System.err.println("Error: Could not shut down channels... " + e.getMessage());
	    	  }
	      }else {
	    	  System.out.println("Invalid selection");
	      }
	  }catch (IOException e) {
		  System.err.println("Error in main menu: " + e);
	  }
	  
	  mainMenu(client);
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.out
          .println("Expected arguments: <host(String)> <port(int)> <auto(int)>");
      System.exit(1);
    }
    int port = 9099;
    String host = args[0];
    int auto = 0;
    try {
      port = Integer.parseInt(args[1]);
      auto = Integer.parseInt(args[2]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port] and [auto] must be integers");
      System.exit(2);
    }

    // Create a communication channel to the server, known as a Channel. Channels
    // are thread-safe
    // and reusable. It is common to create channels at the beginning of your
    // application and reuse
    // them until the application shuts down.
    String target = host + ":" + port;
    channel = ManagedChannelBuilder.forTarget(target)
        // Channels are secure by default (via SSL/TLS). For the example we disable TLS
        // to avoid
        // needing certificates.
        .usePlaintext().build();

    
    try {

      // ##############################################################################
      // ## Assume we know the port here from the service node it is basically set through Gradle
      // here.
      // In your version you should first contact the registry to check which services
      // are available and what the port
      // etc is.

      /**
       * Your client should start off with 
       * 1. contacting the Registry to check for the available services
       * 2. List the services in the terminal and the client can
       *    choose one (preferably through numbering) 
       * 3. Based on what the client chooses
       *    the terminal should ask for input, eg. a new sentence, a sorting array or
       *    whatever the request needs 
       * 4. The request should be sent to one of the
       *    available services (client should call the registry again and ask for a
       *    Server providing the chosen service) should send the request to this service and
       *    return the response in a good way to the client
       * 
       * You should make sure your client does not crash in case the service node
       * crashes or went offline.
       */

      // Just doing some hard coded calls to the service node without using the
      // registry
      // create client
      auto client = new auto(channel);

      if(auto == 1) {
	      System.out.println("Test should be run on a server that just started");
	      
	   // call the parrot service on the server
	      System.out.println("\nTest echo:");
	      client.askServerToParrot("Testing");
	
	      System.out.println("\nTest get 6 jokes:");
	      client.askForJokes(6);
	      
	      System.out.println("\nTest set joke:");
	      client.setJoke("This is a funny joke");
	      
	      System.out.println("\nTest get 2 jokes after set joke:");
	      client.askForJokes(2);
	      
	      System.out.println("\nTest starting a large amount of timers");
	      for(int i = 0; i < 200; i++) {
	    	  client.askForStartTimer(Integer.toString(i));
	      }
	      
	      System.out.println("\nTest listing a large amount of timers");
	      client.askForListTimer();
	      
	      System.out.println("\nTest closing a large amount of timers");
	      for(int i = 0; i < 200; i++) {
	    	  client.askForCloseTimer(Integer.toString(i));
	      }
	      
	      System.out.println("\nTest get timer list");
	      client.askForListTimer();
	      
	      System.out.println("\nTest check a timer named Test that doesn't exist ");
	      client.askForCheckTimer("Test");
	      
	      System.out.println("\nTest closing a timer named Test that doesn't exist");
	      client.askForCloseTimer("Test");
	      
	      System.out.println("\nTest starting a timer named Test");
	      client.askForStartTimer("Test");
	      
	      System.out.println("\nTest starting a timer named Test that already exists");
	      client.askForStartTimer("Test");
	      
	      System.out.println("\nTest checking an existing timer named Test");
	      client.askForCheckTimer("Test");
	      
	      System.out.println("\nTest get timer list with 1 timer in it");
	      client.askForListTimer();
	      
	      System.out.println("\nTest clising an existing timer");
	      client.askForCloseTimer("Test");
	      
	      System.out.println("\nTest checking a timer that has already been closed");
	      client.askForCheckTimer("Test");
	      
	      System.out.println("\nTesting an empty timer list after Test has been closed");
	      client.askForListTimer();
	      
	      System.out.println("\nTest reading the hometown list");
	      client.askForReadHometown();
	      
	      System.out.println("\nTest writing a hometown entry");
	      client.askForWriteHometown("name", "city", "region");
	      
	      System.out.println("\nTest reading a howntown list after writing an entry");
	      client.askForReadHometown();
	      
	      System.out.println("\nTest searching for a successful hometown search");
	      client.askForSearchHometown("city");
	      
	      System.out.println("\nTest adding a second name to the city");
	      client.askForWriteHometown("name2", "city", "region");
	      
	      System.out.println("\nTest reading list of hometown after 2nd name added");
	      client.askForReadHometown();
	      
	      System.out.println("\nTest searching for city with at least two entries");
	      client.askForSearchHometown("city");
	      
	      System.out.println("\nTest searching for a city with no entries");
	      client.askForSearchHometown("nowhere");
      }else {
    	  System.out.println("Auto testing is turned off.");
    	  mainMenu(client);
      }
      
      // Reading data using readLine
      //System.out.println("How many jokes would you like?"); // NO ERROR handling of wrong input here.
      //String num = reader.readLine();

      // calling the joked service from the server with num from user input
      //client.askForJokes(Integer.valueOf(num));

      // adding a joke to the server
      //client.setJoke("I made a pencil with two erasers. It was pointless.");

      // showing 6 joked
      //client.askForJokes(Integer.valueOf(6));

      // ############### Contacting the registry just so you see how it can be done

      // Comment these last Service calls while in Activity 1 Task 1, they are not needed and wil throw issues without the Registry running
      // get thread's services
//      client.getServices();

      // get parrot
//      client.findServer("services.Echo/parrot");
      
      // get all setJoke
//      client.findServers("services.Joke/setJoke");

      // get getJoke
//      client.findServer("services.Joke/getJoke");

      // does not exist
//      client.findServer("random");


    } finally {
      // ManagedChannels use resources like threads and TCP connections. To prevent
      // leaking these
      // resources the channel should be shut down when it will no longer be used. If
      // it may be used
      // again leave it running.
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
