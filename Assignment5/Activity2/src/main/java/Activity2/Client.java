package Activity2;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import Activity2.NetworkUtils;

/**
 * This is the Client interface
 * @author Maura
 * @version 1.0
 */
public class Client  implements Runnable{
	
	// socket used for the connection to leader
	private Socket sock;
	// Input Stream to leader
	private InputStream in;
	// OutputStream to leader
	private OutputStream out;
	
	/**
	 * The constructor for client
	 * @param sock[socket to leader]
	 * @param in[input stream for leader]
	 * @param out[output stream for leader]
	 */
	public Client(Socket sock, InputStream in, OutputStream out) {
		this.sock = sock;
		this.in = in;
		this.out = out;
	}
	
	/**
	 * This method gets the first request from the Leader and sends the client ID to leader
	 * then goes to the mainMenu()
	 */
	public void run() {
		try {
			while(true) {
				// receive in byte array from the leader using Client protocol
				byte[] messageBytes = NetworkUtils.receiveClient(in);
				// convert byte array into JSONObject
	            JSONObject message = JsonUtils.fromByteArray(messageBytes);
				
	            // print out the message in the json
	            System.out.println(message.get("ID"));
	            
	            // Create a json for the response
				JSONObject response = new JSONObject();
				// use getID to load the response
				response = getID();
				
				// if getID was successful
				if(response != null) {
					// send ID to leader
					NetworkUtils.sendClient(out, JsonUtils.toByteArray(response));
					
					// receive in byte array from the leader using Client protocol
					messageBytes = NetworkUtils.receiveClient(in);
					// convert byte array into JSONObject
		            message = JsonUtils.fromByteArray(messageBytes);
					
		            // print out the message in the json
		            System.out.println(message.get("hello"));
					
					// go to main menu
					mainMenu();
				}else {
					// error message
					System.out.println("Error: could not read your ID");
				}
				
				
			}
		}catch(IOException e) {
			System.out.println("IOException in run");
		}
	} 
	
	/**
	 * Send Request to Leader asking to payback
	 */
	public void payback() {
		// amount to payback
		int amount;
		
		// open a scanner
		Scanner scan = new Scanner(System.in);
		
		// ask user how much to pay back
		System.out.println("How much credit would you like to pay back?");
		
		while (true) {
			   // if user has entered something
	           if(scan.hasNext()) {
	        	   // check if input is an int
	            	if(scan.hasNextInt()) {
	            		// set amount to int
	            		amount = scan.nextInt();
	            		// if amount is valid
	            		if(amount > 0) {
	            			// leave while loop
	            			break;
	            		// if amount is 0 or negative
	            		}else {
	            			// ask for positive integer
	            			System.out.println("Amount must be a positive integer");
	            		}
	            	// if input is not a number
	            	}else {
	            		// ask for positive integer
	            		System.out.println("Amount must be a positive integer.");
	            		// empty scanner
	            		scan.next();
	            	}
	        	}
			}
		
		try {
			// create a new json object
			JSONObject response = new JSONObject();
			// set payback and amount
			response.put("payback", amount);
			
			// if loading the response was successful
			if(response != null) {
				// send request to leader using client protocol
				NetworkUtils.sendClient(out, JsonUtils.toByteArray(response));
				
				// receive response from leader using client protocol
				byte[] messageBytes = NetworkUtils.receiveClient(in);
				// convert byte array to json object
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                
                // if leader sent payback
                if(message.has("payback")) {
                	// print out corresponding message
                	System.out.println(message.getString("payback"));
                
                // if leader sent error
                }else if(message.has("error")) {
                	// print out corresponding message
                	System.out.println(message.getString("error"));
                }else {
                	// else print out error message
                	System.out.println("Error: unexpected response type from leader");
                }
            
            // if response is null
			}else {
				// print out error message
				System.out.println("Error: Could not send your payback request");
			}
		}catch (IOException e) {
			System.out.println("Could not send payback request");
		}
		
		// go to main menu
		mainMenu();
	}
	
	/**
	 * send credit request to leader
	 */
	public void getCredit() {
		// requested amount
		int amount;
		
		// open a scanner to read user input
		Scanner scan = new Scanner(System.in);
		
		// ask user how much credit
		System.out.println("How much credit would you like to take out?");
		
		while (true) {
			// if user entered something
           if(scan.hasNext()) {
        	    // if the user entered an int
            	if(scan.hasNextInt()) {
            		// load user input into amount
            		amount = scan.nextInt();
            		// if amount is positive
            		if(amount > 0) {
            			// leave while loop
            			break;
            		// if amount is not positive
            		}else {
            			// ask for a positive integer
            			System.out.println("Amount must be a positive integer.");
            		}
            	// if input is not a number
            	}else {
            		// ask for a positive integer
            		System.out.println("Amount must be a positive integer.");
            		// empty scanner
            		scan.next();
            	}
        	}
		}
		
		
		try {
			// create a json object to hold the response
			JSONObject response = new JSONObject();
			// put credit and amount into response
			response.put("credit", amount);
		
			// if response was successfully loaded
			if(response != null) {
				// send response to leader using client protocol
				NetworkUtils.sendClient(out, JsonUtils.toByteArray(response));
				
				// get response from leader using client protocol
				byte[] messageBytes = NetworkUtils.receiveClient(in);
				// convert byte array into json object
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                
                // if message from leader has credit
                if(message.has("credit")) {
                	// print out corresponding message
                	System.out.println(message.getString("credit"));
                // if message from leader has error
                }else if(message.has("error")) {
                	// print out corresponding message
                	System.out.println(message.getString("error"));
                }
			}
		}catch (IOException e) {
			System.out.println("Could not send credit request");
		}
		// go to main menu
		mainMenu();
	}
	
	/**
	 * sends exit message to leader, gets response and quits
	 */
	public void exit() {
		
		try {
			// make a new json object
			JSONObject  response = new JSONObject();
			// load exit and exit message to json object
			response.put("exit", "bye");
			
			// send response to leader using client protocol
			NetworkUtils.sendClient(out, JsonUtils.toByteArray(response));
			
			// get response from leader using client protocol
			byte[] messageBytes = NetworkUtils.receiveClient(in);
			// convert byte array into json object
            JSONObject message = JsonUtils.fromByteArray(messageBytes);
            
            // if leader's response has exit
            if(message.has("exit")) {
            	// print out corresponding message
            	System.out.println(message.getString("exit"));
            // if message from leader has error
            }else if(message.has("error")) {
            	// print out corresponding message
            	System.out.println(message.getString("error"));
            }
		}catch (IOException e) {
			System.out.println("Could not send goodbye to server");
		}finally {
			try {
				// close resources
				out.close();
				in.close();
				sock.close();
				// exit
				System.exit(0);
			}catch (Exception e) {
				System.out.println("Could not close resources properly");
				// exit
				System.exit(-1);
			}
		}
	}
	
	/**
	 * the main menu
	 */
	public void mainMenu() {
		// print the main menu
		System.out.println("Please select 1 or 2(0 to quit)");
		System.out.println("1: get credit");
		System.out.println("2: payback credit");
		
		// create a new buffered reader to read input
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		// string to hold input
		String input;
		
		while (true) {
            try {
            	// read in input from user
                input= stdin.readLine();
                // if there is input from user
                if(input != null) {
                	// if input is 1
                	if(input.equalsIgnoreCase("1")) {
                		// print out get credit selected
                		System.out.println("get credit selected");
                		// go to get credit
                		getCredit();
                		
                	// if input equals 2	
                	}else if(input.equalsIgnoreCase("2")) {
                		// print out payback credit selected
                		System.out.println("payback credit selected");
                		// go to payback
                		payback();
                		
                	// if input equals 0
                	}else if (input.equalsIgnoreCase("0")) {
                		// call exit
                		exit();
                		
                	}else {
                		// print out invalid selection and reprint the main menu
                		System.out.println("Invalid selection");
                		System.out.println("Please select 1 or 2(0 to quit)");
                		System.out.println("1: get credit");
                		System.out.println("2: payback credit");
                		// empty the input
                		input = null;
                	}
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	// ask user for id
	public JSONObject getID() {
		// string to hold id
		String id = null;
		// open a buffer reader to read user input
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
            try {
            	// read in user input
                id= stdin.readLine();
                // if input was read
                while(id != null) {
                	// if user id is Node
                	if(id.equalsIgnoreCase("Node")) {
                		// tell user this is invalid
                		System.out.println("ID can not be Node");
                		// empty id
                		id = null;
                	// if user input contains spaces
                	}if(id.contains(" ")) {
                		// tell user this is invalid
                		System.out.println("ID can not contain spaces");
                	}
                	// create a new json object
                	JSONObject ret = new JSONObject();
                	// load ID and id in
                	ret.put("ID", id);
                	// return json
                	return ret;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
	  }
	
	/**
	 * Entry point for Client
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// host is always localhost
		String host = "localhost";
		// port is always 8000
		int port = 8000;
		// create socket
		Socket sock = null;
		
		try {
			// connect to leader
			sock = new Socket(host, port);
			// open output stream to leader
            OutputStream out = sock.getOutputStream();
            // open input stream to leader
            InputStream in = sock.getInputStream();
            
            // create new client object and run client
            new Client(sock, in, out).run();
           
		} catch (IOException e) {
            e.printStackTrace();
        }finally {
        	try {
        		// if socket is not null
        		if(sock != null) {
        			// close socket
        			sock.close();
        		}
        	}catch (Exception e) {
        		System.out.println("Could not close socket");
        	}
        }
	}
}