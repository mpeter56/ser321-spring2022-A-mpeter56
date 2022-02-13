package client;

import java.net.*;
import java.io.*;

import org.json.*;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 * This class runs the client and sends requests to the server and reads responses.
 * 
 * @author Maura
 * @version 2.0
 */
class SockBaseClient {
	
	/**
	 * gameTurn handles reading in tasks and images from the server, as well as WON responses
	 * and BYE responses
	 * @param out [OutputStream]
	 * @param in [InputStream]
	 * @throws Exception
	 */
	public static void gameTurn(OutputStream out,InputStream in) throws Exception {
		//Request to send to server
		Request op;
		// Response to read in from server
		Response response;
		// Client input from command line
		String strToSend;
		// Input Stream Reader for command line
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		
		// wait for game update while player is thinking
        while(!stdin.ready()) {
        	//if there is an update
        	if(in.available() != 0) {
        		// read in response from server
        		response = Response.parseDelimitedFrom(in);
        		// if the server sent a task
        		if(response.getResponseType() == Response.ResponseType.TASK) {
        			//display image and task
    	        	System.out.println("Your Image:");
    	        	System.out.println(response.getImage());
    	        	System.out.println("Your Task:");
    	        	System.out.println(response.getTask());
    	        // if the server sent a response of type WON
            	}else if((response.getResponseType() == Response.ResponseType.WON)){
            		// print win message with completed picture, then go to main menu
    	        	System.out.println("You win!");
    	        	System.out.println(response.getImage());
    	        	mainMenu(out,in, null);
    	        // if server sent a response of type BYE
    	        }else if((response.getResponseType() == Response.ResponseType.BYE)) {
    	        	//print goodbye message and close streams and exit
    	        	System.out.println(response.getMessage());
            		in.close();
            		out.close();
            		System.exit(0);
            	// if server sent a response of type ERROR
    	        }else if(response.getResponseType() == Response.ResponseType.ERROR){
    	        	//print error message, close streams and exit
    	        	System.out.println(response.getMessage());
            		in.close();
            		out.close();
            		System.exit(0);
            	// if server sent a response of an unexpected type
    	        }else {
    	        	//Print out message, close streams, and exit
    	        	System.out.println("Unexpected response type");
            		in.close();
            		out.close();
            		System.exit(0);
    	        }
        	}
        }
        // once the player enters their answer
        // read in from command line
		strToSend = stdin.readLine();
		// build a request to send to server
		op = Request.newBuilder()
			// request type ANSWER
		   .setOperationType(Request.OperationType.ANSWER)
		   // set answer string to client input
		   .setAnswer(strToSend).build();
		// send request
		op.writeDelimitedTo(out);
		//next turn
		gameTurn(out, in);
		   
	}

	/**
	 * mainMenu prints the main menu from server and sends selected response
	 * @param out[outputStream]
	 * @param in[InputStream]
	 * @param response[past response only used for invalid selection, null all other times]
	 * @throws Exception
	 */
	public static void mainMenu(OutputStream out, InputStream in, Response response) throws Exception {
		//Request to send to server
		Request op;
		// Buffered Reader to read from command line
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        
		// if the response is null (not after invalid menu selection)
		if(response == null) {
			// read from the server
			response = Response.parseDelimitedFrom(in);
		}
		
        // print the server response. 
        System.out.println(response.getMessage());
        
        // read in selection from command line
        String strToSend = stdin.readLine();
        
        // if option "1" is selected
        if(strToSend.compareToIgnoreCase("1")==0) {
        	// build a new request
        	op = Request.newBuilder()
        			// of type LEADER
                    .setOperationType(Request.OperationType.LEADER).build();
        	
        	//send request
        	op.writeDelimitedTo(out);
        	
        	//get response from server
        	response = Response.parseDelimitedFrom(in);
        	
        	// print the leader board
        	System.out.print("\n");
        	System.out.println("Leader Board");
        	if(response.getResponseType() == Response.ResponseType.LEADER) {
        		for (Entry lead: response.getLeaderList()){
                    System.out.println(lead.getName() + ": " + lead.getWins());
                }
        	}
        	System.out.print("\n");
        	
        	// go back to main menu
        	mainMenu(out, in, null);
        	
        // if option "2" is selected	
        }else if(strToSend.compareToIgnoreCase("2")==0) {
        	// build a new request
        	op = Request.newBuilder()
        			// of type new
                    .setOperationType(Request.OperationType.NEW).build();
        	
        	// send request
        	op.writeDelimitedTo(out);
        	
        	// start game
        	gameTurn(out, in);
        	
        // if option "3" is selected
        }else if(strToSend.compareToIgnoreCase("3")==0) {
        	// build a new request
        	op = Request.newBuilder()
        			// of type QUIT
        			.setOperationType(Request.OperationType.QUIT).build();
        	
        	// send request
        	op.writeDelimitedTo(out);
        	
        	// read in response from server
        	response = Response.parseDelimitedFrom(in);
        	
        	// if server sent response of type BYE
        	if(response.getResponseType() == Response.ResponseType.BYE) {
        		// print out goodbye message, close streams, and exit
        		System.out.println(response.getMessage());
        		in.close();
        		out.close();
        		System.exit(0);
        	// if server sent response not of type BYE
        	}else {
        		// print out error message, close streams, and exit
        		System.out.println("Unexpected response ype from server, shutting down...");
        		in.close();
        		out.close();
        		System.exit(-1);
        	}
        // if client selection is not '1', '2', or '3'
        }else {
        	// print invalid message and call mainMenu with the main menu response from server
        	System.out.println("Invalid selection, please select '1','2', or '3'");
            mainMenu(out,in,response);
        }
	}
	
	/**]
	 * This is the main menu and entry point for the Client
	 * @param args[<host(String)> <port(int)>]
	 */
    public static void main (String args[]) {
    	// socket to connect to server
        Socket serverSock = null;
        // output stream to server
        OutputStream out = null;
        // input stream from server
        InputStream in = null;
        int i1=0, i2=0;
        // default port
        int port = 9099; 
        // String to hold name
        String strToSend= null;

        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        
        // get host
        String host = args[0];
        // get port
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }
        
        // Ask user for username
        System.out.println("Please provide your name for the server. ");
        
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        try {
        	strToSend = stdin.readLine();
        }catch (IOException e) {
        	System.out.println("Could not read name");
        	e.printStackTrace();
        }

        // Build the first request object just including the name
    	Request op = Request.newBuilder()
    			// request type NAME
                .setOperationType(Request.OperationType.NAME)
                .setName(strToSend).build();
        
        try {
            // connect to the server
            serverSock = new Socket(host, port);

            // Open streams
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();

            // send request to server
            op.writeDelimitedTo(out);

            //main menu
            mainMenu(out, in, null);
            
        // if server disconnects
        } catch (Exception e) {
        	// print out error message and stack trace
        	System.out.println("Disconnected from server");
            e.printStackTrace();
        // close streams and socket
        } finally {
        	try {
	            if (in != null)   in.close();
	            if (out != null)  out.close();
	            if (serverSock != null) serverSock.close();
        	}catch (IOException e) {
        		System.out.println("Could not close socket");
        		e.printStackTrace();
        	}
        }
    }
}


