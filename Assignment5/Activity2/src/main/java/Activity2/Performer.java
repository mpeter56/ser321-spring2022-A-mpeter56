/**
  File: Performer.java
  Author: Student in Fall 2020B
  Description: Performer class in package taskone.
*/

package Activity2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;


import java.util.concurrent.locks.*;

/**
 * Performer performs the tasks for individual Leader threads
 * @author Maura
 * @version 1
 */
class Performer {

	// socket to client
    private Socket conn;
    // mutex lock
    protected Lock mutex;
    // client id
    private String id;
    // port number to node1
    private int port1;
    // port number to node2
    private int port2;
    
    /**
     * Constructor for performer
     * @param sock[socket to client]
     * @param mutex1[mutex lock]
     * @param p1[port number for node1]
     * @param p2[port number for node2]
     */
    public Performer(Socket sock, Lock mutex1, int p1, int p2) {
    	// set variables
        this.conn = sock;
        this.mutex = mutex1;
        this.id = null;
        this.port1 = p1;
        this.port2 = p2;
        // ask nodes for their ledgers and create the ledger for leader accordingly
        verifyLedger();
    }
    
    /**
     * This method gets the ledgers from both nodes and updates the leader ledger
     * Assumption is that the node ledgers hold the correct information
     */
   public void verifyLedger() {
	   // strings to hold ledgers
	   String ledger1 = "", ledger2 = "";
	   // create a new json object
	   JSONObject response;
	   // send verify request to node 1 and store the response
	   response = NetworkUtils.send("localhost", (port1), verify());
	   // if node 1 response has method
	   if(response.has("method")) {
		   // if method was verify
		   if(response.getString("method").compareToIgnoreCase("verify") == 0) {
			   // if response has ledger
			   if(response.has("ledger")) {
				   // load ledger into ledger1
				   ledger1 = response.getString("ledger");
			   // if response does not have ledger
			   }else {
				   // print out error message
				   System.out.println("Could not update ledger for Node 1");
			   }
			   // if response does not have method verify
		   }else {
			   // print out error message
			   System.out.println("Could not update ledger for Node 1");
		   }
	   // if response does not have method
	   }else {
		   // print out error message
		   System.out.println("Could not update ledger for Node 1");
	   }
	   
	   // get ledger from node2
	   response = NetworkUtils.send("localhost", (port2), verify());
	   // if node 2 response has method
	   if(response.has("method")) {
		   // if method is verify
		   if(response.getString("method").compareToIgnoreCase("verify") == 0) {
			   // if response has ledger
			   if(response.has("ledger")) {
				   // load ledger into ledger2
				   ledger2 = response.getString("ledger");
			   // if response does not have ledger
			   }else {
				   // print out error message
				   System.out.println("Could not update ledger for Node 2");
			   }
		   // if method is not verify
		   }else {
			   // print out error message
			   System.out.println("Could not update ledger for Node 2");
		   }
	   // if response does not have method
	   }else {
		   // print out error message
		   System.out.println("Could not update ledger for Node 2");
	   }
	   // update the leader ledger usinging ledgers from node 1 and node 2
	   updateLedger(ledger1, ledger2);
   }
   
   /**
    * Builds a json object with method verify
    * @return [json object with method verify]
    */
   public static JSONObject verify() {
	   JSONObject req = new JSONObject();
	   req.put("method", "verify");
	   return req;
   }

   /**
    * Builds a json object with method canPayback, client id and amount
    * @param ID [client id]
    * @param amount[payback amount calculated for node]
    * @return[json object]
    */
   public static JSONObject canPayback(String ID, int amount) {
        JSONObject req = new JSONObject();
        req.put("method", "canPayback");
        req.put("ID", ID);
        req.put("amount", amount);
        return req;
      }
   
   
   /**
    * Builds a json object with methoc canCredit, client id, and credit amount
    * @param id[client id]
    * @param amount[credit amount calculated for node]
    * @return[json object]
    */
      public static JSONObject canCredit(String id, int amount) {
    	    JSONObject req = new JSONObject();
    	    req.put("method", "canCredit");
    	    req.put("ID", id);
    	    req.put("amount", amount);
    	    return req;
    	  }
      
      /**
       * Builds a json object with method payback, client id, and payback amount
       * @param id[client id]
       * @param amount[payback amount calculated for node]
       * @return[json object]
       */
      public static JSONObject payback(String id, int amount) {
    	    JSONObject req = new JSONObject();
    	    req.put("method", "payback");
    	    req.put("ID", id);
    	    req.put("amount", amount);
    	    return req;
    	  }
      
      /**
       * Builds a json object with method credit, client id, and credit amount
       * @param id[client id]
       * @param amount[credit amount calculated for node]
       * @return[json object]
       */
      public static JSONObject credit(String id, int amount) {
    	    JSONObject req = new JSONObject();
    	    req.put("method", "credit");
    	    req.put("ID", id);
    	    req.put("amount", amount);
    	    return req;
    	  }

      /**
       * Builds a json object with error and error message to send to client
       * @return[json object]
       */
      public static JSONObject error() {
        JSONObject req = new JSONObject();
        req.put("error", "Sorry for the inconvinience we are working to fix the problem, please try again later");
        return req;
      }
      
      /**
       * Builds a json asking client for their id
       * @return[json object]
       */
      public static JSONObject askID() {
    	  JSONObject req = new JSONObject();
    	    req.put("ID", "What is your client ID?");
    	    return req;
      }
      
      /**
       * gets amounts owed from the leader ledger
       * @param ID[client id]
       * @param Node[node being checked]
       * @return[amount client owes to node]
       */
      public static int checkLedgerAmount(String ID, int Node) {
  	  	//String array to hold each line read from the file
  		    String[] ledgerLine = null;
  		    // 2d String array to hold client ids in [0] and amount owed for node 1 in [1] 
  		    // and amount owed for node 2 in [2] for each line
  		    String[][] ledger = null;
  		    // string read in from file
  		    String data = "";
  		    
  		    // read in file
  		    try {
  		        File myObj = new File("ledgerLeader.txt");
  		        Scanner myReader = new Scanner(myObj);
  		        while (myReader.hasNextLine()) {
  		        	data  = data.concat(myReader.nextLine());
  		        	// add new line character at the end of each line to seperate
  		        	data = data.concat("\n");
  		        }
  		        myReader.close();
  		        // catch file not found exception by creating a new ledger and
  		        // calling checkLedger again
  		    } catch (FileNotFoundException e) {
  		    	createLedger();
  		    	checkLedger(ID);
  		    }
  		    // split the string data from the text file into the ledgerLine array by the 
  		    // new line character
  			ledgerLine = data.split("\n");
  			// make ledger size the row length of ledgerLine x 3
  			ledger = new String[ledgerLine.length][3];
  			
  			// split each line of ledgerLine at the space into the ledger
  			for(int i = 0; i < ledgerLine.length; i++) {
  				ledger[i] = ledgerLine[i].split(" ");
  			}
  			
  			// check ledger for client's name
  			for(int i = 0; i < ledger.length; i++) {
  			// if found return amount owed for current node
  				if(ledger[i][0].equals(ID)) {
  			    	return Integer.parseInt(ledger[i][Node]);
  			    } else {
  			    	
  			    }
  			}
  			return 0;
  	  }
    
    public static Boolean checkLedger(String ID) {
    	//String array to hold each line read from the file
  	    String[] ledgerLine = null;
  	    // 2d String array to hold client ids in [0] and amount owed for node 1 in [1] 
		// and amount owed for node 2 in [2] for each line
  	    String[][] ledger = null;
  	    // string read in from file
  	    String data = "";
  	    // boolean to determine if client's name was found on the ledger
  	    boolean found = false;
  	    
  	    // read in file
  	    try {
  	        File myObj = new File("ledgerLeader.txt");
  	        Scanner myReader = new Scanner(myObj);
  	        while (myReader.hasNextLine()) {
  	        	data  = data.concat(myReader.nextLine());
  	        	// add new line character at the end of each line to seperate
  	        	data = data.concat("\n");
  	        }
  	        myReader.close();
  	        // catch file not found exception by creating a new ledger and
  	        // calling checkLedger again
  	    } catch (FileNotFoundException e) {
  	    	createLedger();
  	    	checkLedger(ID);
  	    }
  	    // split the string data from the text file into the ledgerLine array by the 
  	    // new line character
  		ledgerLine = data.split("\n");
  		// make ledger size the row length of ledgerLine x 3
  		ledger = new String[ledgerLine.length][3];
  		
  		// split each line of ledgerLine at the space into the ledger
  		for(int i = 0; i < ledgerLine.length; i++) {
  			ledger[i] = ledgerLine[i].split(" ");
  		}
  		
  		// check ledger for client's name
  		for(int i = 0; i < ledger.length; i++) {
  			// if found set found to true
  			if(ledger[i][0].equals(ID)) {
  		    	found = true;
  		    } else {
  		    	
  		    }
  		}
  		// return false if found, true if not found
  		return !found;
    }
    
    /**
     * Applies payback to the leader ledger
     * @param ID[client id]
     * @param Node[node number being updated]
     * @param amt[amount being payed back]
     */
    public static void paybackLedger(String ID,int Node, int amt) {
	  	//String array to hold each line read from the file
		    String[] ledgerLine = null;
		    // 2d String array to hold client ids in [0] and amount owed for node 1 in [1] 
  		    // and amount owed for node 2 in [2] for each line
		    String[][] ledger = null;
		    // string read in from file
		    String data = "";
		    String newData = "";
		    
		    // read in file
		    try {
		        File myObj = new File("ledgerLeader.txt");
		        Scanner myReader = new Scanner(myObj);
		        while (myReader.hasNextLine()) {
		        	data  = data.concat(myReader.nextLine());
		        	// add new line character at the end of each line to seperate
		        	data = data.concat("\n");
		        }
		        myReader.close();
		        // catch file not found exception by creating a new ledger and
		        // calling checkLedger again
		    } catch (FileNotFoundException e) {
		    	createLedger();
		    	checkLedger(ID);
		    }
		    // split the string data from the text file into the ledgerLine array by the 
		    // new line character
			ledgerLine = data.split("\n");
			// make ledger size the row length of ledgerLine x 3
			ledger = new String[ledgerLine.length][3];
			
			// split each line of ledgerLine at the space into the ledger
			for(int i = 0; i < ledgerLine.length; i++) {
				ledger[i] = ledgerLine[i].split(" ");
			}
			
			
			// check ledger for client's name
			for(int i = 0; i < ledger.length; i++) {
				// if client is found
				if(ledger[i][0].equals(ID)) {
						// update the ledger for current node
			    		ledger[i][Node] = Integer.toString(amt);
			    		
			    		
			    		if(ledger[i][1].equals("0") && ledger[i][2].equals("0") ) {
			    			
			    		}else {
			    			newData = newData.concat(ledger[i][0] + " " + ledger[i][1] + " " + ledger[i][2] + "\n");
			    		}
			    // if ledger is empty
			    }else if (3 == ledger[i].length) {
			    	newData = newData.concat(ledger[i][0] + " " + ledger[i][1] + " " + ledger[i][2] + "\n");
			    }
			}
			
			try {
			// open writer to file
			FileWriter myWriter = new FileWriter("ledgerLeader.txt");
			// write new data to file
			myWriter.write(newData);
			// close writer
			myWriter.close();
			}catch(IOException e) {
				System.out.println("Could not write to ledger in payback");
			}
	  }
    
    /**
     * updates the leader ledger when credit is applied to both nodes
     * @param id[client id]
     * @param Node1[int for node 1, should be 1]
     * @param Node2[int for node 2, should be 2]
     */
    public static void creditLedger(String id, int Node1, int Node2) {
  	  try {
  		File myObj;
  		String data = "";
  		// open reader for file
  		  myObj = new File("ledgerLeader.txt");
  		  Scanner myReader = new Scanner(myObj);
  	        while (myReader.hasNextLine()) {
  	        	data  = data.concat(myReader.nextLine());
  	        	// add new line character at the end of each line to seperate
  	        	data = data.concat("\n");
  	        }
  	        myReader.close();
  	  	// open writer to file
  		FileWriter myWriter = new FileWriter("ledgerLeader.txt");
  		// copy old ledger
  		myWriter.append(data);
  		// add new entry
  		myWriter.append(id + " " + Node1+ " " + Node2 + "\n");
  		// close writer
  		myWriter.close();
  		System.out.println("Successfully wrote to the file.");
  	  }catch(IOException e) {
  		  createLedger();
  		  creditLedger(id, Node1, Node2);
  	  }
    }
    
    public static void updateLedger(String nodeLedger1, String nodeLedger2) {
    	//String array to hold each line read from the string nodeLedger1
	    String[] ledgerLine1 = null;
	    // 2d String array to hold client ids in [0] and amount owed for node 1 in [1] 
	    String[][] ledger1 = null;
	    
	    ledgerLine1 = nodeLedger1.split("\n");
		// make ledger size the row length of ledgerLine x 3
		ledger1 = new String[ledgerLine1.length][2];
		
		for(int i = 0; i < ledgerLine1.length; i++) {
			ledger1[i] = ledgerLine1[i].split(" ");
		}
		
		//String array to hold each line read from the string nodeLedger2
	    String[] ledgerLine2 = null;
	    // 2d String array to hold client ids in [0] and amount owed for node 2 in [1] for each line
	    String[][] ledger2 = null;
	    
	    ledgerLine2 = nodeLedger2.split("\n");
		// make ledger size the row length of ledgerLine x 3
		ledger2 = new String[ledgerLine2.length][2];
		
		for(int i = 0; i < ledgerLine2.length; i++) {
			ledger2[i] = ledgerLine2[i].split(" ");
		}
		
		
		// 2d String array to hold client ids in [0] and amount owed for node 1 in [1] 
		// and amount owed for node 2 in [2] for each line
	    String[][] ledger = new String[ledger1.length + ledger2.length][3];
	    
	    // make all entries null
	    for(int i = 0; i < ledger.length; i++) {
			ledger[i][0] = null;
			ledger[i][1] = null;
			ledger[i][2] = null;
		}
	    
	    // copy ledger from node1 to leader ledger, set node2 slot to 0
	    for(int i = 0; i < ledger1.length; i++) {
			ledger[i][0] = ledger1[i][0];
			ledger[i][1] = ledger1[i][1];
			ledger[i][2] = "0";
		}
	    
	    // for each item in ledger from node 2
	    for(int i = 0; i < ledger2.length; i++) {
	    	// holds index of open leader ledger slots
	    	int open = -1;
	    	// boolean for if item in ledger from node 2 was found in leader ledger
	    	boolean found = false;
	    	// iterate through each item in leader ledger
			for(int j = 0; j < ledger.length; j++) {
				// if leader ledger at j is empty
				if(ledger[j][0] == null) {
					// set open index to j
					open = j;
				// if leader ledger at j is not empty
				}else {
					// if leader ledger id at j equals leader ledger id at i
					if(ledger[j][0].equals(ledger2[i][0])) {
						// set found to true
						found = true;
						// update amount for node 2
						ledger[j][2] = ledger2[i][1];
					}
				}
			}
			// if ledger2 at i was not found in leader ledger
			if(!found) {
				// load entry into an open slot in leader ledger
				ledger[open][0] = ledger2[i][0];
				ledger[open][1] = "0";
				ledger[open][2] = ledger2[i][1];
			}
		}
	    
	    // string to hold ledger
	    String data = "";
	    // iterate through leader ledger
	    for(int i = 0; i < ledger.length; i++) {
	    	// skip empty entries in leader ledger
	    	if(ledger[i][0] == null) {
	    	// if its not empty
	    	}else {
	    		// add it to the data string
	    		data = data.concat(ledger[i][0] + " " + ledger[i][1] + " " + ledger[i][2] + "\n");
	    	}
	    }
		
	    try {
	  		File myObj;
	  		// open file
	  		  myObj = new File("ledgerLeader.txt");
	  	        
	  	  	// open writer to file
	  		FileWriter myWriter = new FileWriter("ledgerLeader.txt");
	  		// write data to file
	  		myWriter.write(data);
	  		// close writer
	  		myWriter.close();
	  		System.out.println("Successfully wrote to the file.");
	  		
	  		// if file does not exist create it and update it
	  	  }catch(IOException e) {
	  		  System.out.println("ledgerLeader.txt not found");
	  		  createLedger();
	  		  updateLedger(nodeLedger1, nodeLedger2);
	  	  }
    }
    
    /**
     * Creates a leader ledger file at "ledgerLeader.txt"
     */
    public static void createLedger() {
    	try {
    		//create file
    		File myObj = new File("ledgerLeader.txt");
    			//if successful
    			if (myObj.createNewFile()) {
    				
    				System.out.println("File created: " + myObj.getName());
    				
    			} else {
    				System.out.println("File already exists.");
    			}
    	// catch IOException and print stack trace
    	} catch (IOException e) {
  			System.out.println("Could not create ledgerLeader.txt");
  			e.printStackTrace();
  		}
  	}
      
    /**
     * Holds the main logic for leader
     */
    public void doPerform() {
    	// boolean to exit out of while loop
        boolean quit = false;
        // boolean to signal to quit at end of current loop
        boolean quitAfter = false;
        // output stream to client
        OutputStream out = null;
        // input stream to client
        InputStream in = null;
        // is it the first time though the while loop?
        int intro = 0;
        // byte array to hold output for client
        byte[] output;
        // is the mutex locked?
        boolean lock = false;
        try {
        	// connect input and output streams to client
            out = conn.getOutputStream();
            in = conn.getInputStream();
            // while quit if false
            while (!quit) {
            	// make a new json object
            	JSONObject response = new JSONObject();
            	// canCredit = clients credit request is accepted
            	// canPayback1 = client payback request is valid for node 1
            	// can payback2 = client payback request is valid for node 2
            	// error = is a node down?
                boolean  canCredit = false, canPayback1 = false, canPayback2 = false, error = false;
                // amtN1 = amount Node 1 has availible
                // amtN2 = amount Node 2 has availible
                // amount1 = amount to send to node 1
                // amount2 = amount to send to node 2
                int amtN1 = 0, amtN2 = 0, amount1 = 0,amount2=0 ;
                // is this the first time in the while loop?
            	if(intro == 0) {
            		// load id request into response
            		response = askID();
            		// increment intro to prevent this code from being accessed again
            		intro++;
            	
            		// load json into byte array
	                output = JsonUtils.toByteArray(response);
	                // send to client using client protocol
	                NetworkUtils.sendClient(out, output);
            	}
            	
            	// get message from client using client protocol
                byte[] messageBytes = NetworkUtils.receiveClient(in);
                // load byte array into json
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                
                // if message from client has ID
                if(message.has("ID")) {
                	// load ID into variable id
                	id = message.getString("ID");
                	// reset response
                	response = new JSONObject();
                	// load hello message to client
                	response.put("hello", "Hello " + id + "! What can I do for you?");
                	
                	// load json into byte array
                	output = JsonUtils.toByteArray(response);
                    // send hello message to client
                    NetworkUtils.sendClient(out, output);
                    
                // if client message has credit
                }else if(message.has("credit")) {
                	// lock the mutex
                	mutex.lock();
                	System.out.println(id + " has the lock");
                	lock = true;
                	
                	// get requested credit amount
                	int amount = message.getInt("credit");
                	System.out.println(id + " requested a credit of " + amount);
                	
                	// reset json
                	response = null;
                	// ask node 1 if credit request is ok
                	response = NetworkUtils.send("localhost", port1, canCredit(id,amount));
                	
                	// if node 1 has responded
                	if(response != null) {
                		
                		// if node 1 response has method
	                	if(response.has("method")) {
	                		
	                		// if method is can Credit
	                		if(response.getString("method").equalsIgnoreCase("can Credit")) {
	                			// if node 1 response has available
	                			
	                			if(response.has("available")) {
	                				// load availible money node 1 has into amtN1
	                				amtN1 = response.getInt("available");
	                				
	                			// if node 1 did not send available
	                			}else {
	                				// print out error message
	                				System.out.println("Missing amount for Node1");
	                			}
	                			
	                			// reset response
	                			response = null;
	                			// ask node 2 if credit request is ok
	                			response = NetworkUtils.send("localhost", port2, canCredit(id,amount));
	                			
	                			// if node 2 responded
	                			if(response != null) {
	                				
	                				// if node 2 response has method
		                			if(response.has("method")) {
		                				
		                				// if method is can Credit
		                				if(response.getString("method").equalsIgnoreCase("can Credit")) {
		                					
		                					// set canCredit to true and print credit message
		                					System.out.println("Can Credit " + id);
		                					canCredit = true;
		                					
		                					// if node 2 sent available
		                					if(response.has("available")) {
		                						// load availible credit amount node 2 has
		                        				amtN2 = response.getInt("available");
		                        				
		                        			// if node 2 did not send available
		                        			}else {
		                        				// print out error message
		                        				System.out.println("Missing amount for Node2");
		                        			}
		                					
		                				// if method is not can Credit
		                				}else {
		                					// credit request denied
		                					System.out.println("Can Not Credit " + id);
		                				}
		                				
		                			// if node 2 did not send method
		                			}else {
		                				// print out error message
		            					System.out.println("Error in Node2");
		            				}
		                			
		                		// if response from node 2 was not recieved
	                			}else if(response == null){
	                				// send error message to client and set error to true
	                				error = true;
		                        	response = error();
		                        	output = JsonUtils.toByteArray(response);
		                        	NetworkUtils.sendClient(out, output);
		                        }
	                			
	                		// if node 1 method was not can Credit
	                		}else {
	                			// deny credit request
	        					System.out.println("Can Not Credit "+ id);
	        				}
	                		
	                	// if node 1 did not send method
	                	}else {
	                		// print out error message
	    					System.out.println("Error in Node1");
	    				}
	                	
	                	// if credit request accepted
	                	if(canCredit) {
	                		// convert amtN1 to a double to be used as a numerator
	                		double num = amtN1;
	                		// calculate the total availible amount in a double
	                		double den = (amtN2+amtN1);
	                		// calculate the ratio that amount for node 1 will have
	                		double ratio = num/den;
	                		// calculate the amount that node 1 will be crediting the client
	                		double N1Amount = ratio * amount;
	                		// conver that amount to an int
	                		int amtN1f = (int)N1Amount;
	                		
	                		// calculate the same for node 2
	                		num = amtN2;
	                		ratio = num/den;
	                		double N2Amount = (ratio * amount)+ 0.99;
	                		int amtN2f = (int)N2Amount;
	                		
	                		// tell the nodes to apply the credit
	                		response = NetworkUtils.send("localhost", port1, credit(id, amtN1f));
	                		response = NetworkUtils.send("localhost", port2, credit(id, amtN2f));
	                		
	                		// tell the client that they have been credited
	                		response = new JSONObject();
	                		response.put("credit","You have successfully be credited " + amount);
	                		
	                		// update the client's debt
	                		creditLedger(id, amtN1f, amtN2f);
	                		
	                		// update the Node availible slot in the ledger
	                		paybackLedger("Node", 1, (int)(amtN1 - amtN1f));
	                		paybackLedger("Node", 2, (int)(amtN2 - amtN2f));
	                		
	                	// if credit request was denied tell the client
	                	}else {
	                		response.put("credit", "Could not credit. Either payback credit or try a lower amount");
	                	}
	                	// if node 2 responded
	                	if(!error) {
	                		// send credit denial response to client
		                	output = JsonUtils.toByteArray(response);
		                    NetworkUtils.sendClient(out, output);
	                	}
	                	
	                // if node 1 did not respond
                	}else if(response == null){
                		// send error message to client and set error to true
                		error = true;
                		response = error();
                		output = JsonUtils.toByteArray(response);
                		NetworkUtils.sendClient(out, output);
                	}
                	
                	
                // if client message has payback
                }else if(message.has("payback")) {
                	// lock the mutex
                	mutex.lock();
                	System.out.println(id + " has the lock");
                	lock = true;
                	
                	// get the requested payback amount
                	int amount = message.getInt("payback");
                	System.out.println(id+ " wants to payback " + message.getInt("payback"));
                	
                	// check how much node 1 is owed by client
                	double owed1 = checkLedgerAmount(id,1);
                	// check how much node 2 is owed by client
                	double owed2 = checkLedgerAmount(id,2);
                	
                	// if the total owed is greater than 0
                	if(owed1+owed2 > 0) {
                		
                		// calculate how the percentage to be payed back to node 1
                		double ratio = (owed1/(owed1+owed2));
                		// calculate the amount to be payed to node 1
                		amount1 = (int)(amount * ratio);
                		
                		// calculate for node 2
                		ratio = (owed2/(owed1+owed2));
                		amount2 = (int)(amount * ratio);
                		
                		// if the total amount owed is greater than the amount payed back
                		if(owed1 + owed2 > amount) {
                			// if node 1 is owed more or the same as node 2
                			if(owed1 >= owed2) {
                				// increment the amount to pay node 1
                				// this is because 1 money is lost converting doubles to ints
                				amount1++;
                				
                			// if node 2 is owed more than node 1
                			}else {
                				// increment the amount to pay node 2
                				amount2++;
                			}
                		}
                		
                	// if the client owe 0
                	}else {
                		// make the amounts the full amounts
                		amount1 = amount;
                		amount2 = amount;
                	}
                	
                	// reset response
                	response = null;
                	// ask node 1 if the payback request is ok
                	response = NetworkUtils.send("localhost", port1, canPayback(id,amount1));
                	
                	// if node 1 responds
                	if(response != null) {
                		
                		// if node 1 sends method
	                	if(response.has("method")) {
	                		
	                		// if method is can payback( payback request accepted)
	                		if(response.getString("method").equalsIgnoreCase("can payback")) {
	                			
	                			// if node 1 sends availible(how much money node 1 has)
	                			if(response.has("available")) {
	                				
	                				// get how much money node 1 has and store it in amtN1
	                				amtN1 = response.getInt("available");
	                				// accept request for node 1
	                				canPayback1 = true;
	                				
	                				// reset response
	                				response = null;
	                				// ask node 2 if the payback request is ok
	                				response = NetworkUtils.send("localhost", port2, canPayback(id,amount2));
	                				
	                				// if node 2 responds
	                				if(response != null) {
	                					
	                					// if node 2 sends method
		                				if(response.has("method")) {
		                					
		                					// if method is can payback(node 2 accepted the request
		                    				if(response.getString("method").equalsIgnoreCase("can payback")) {
		                    					
		                    					// if node 2 send available(how much money node 2 has)
		                    					if(response.has("available")) {
		                    						// read in how much money node 2 has and store it in amtN2
		                            				amtN2 = response.getInt("available");
		                            				System.out.println(id + " can Payback");
		                            				// accept request for node 2
		                            				canPayback2 = true;
		                            				
		                            			// if node 2 did not send avaiable
		                            			}else {
		                            				// print out error message
		                            				System.out.println("Missing amount for Node2");
		                            			}
		                    				
		                    				// if method did not equal can payback
		                    				}else {
		                    					// deny the request for node 2
		                    					System.out.println("Can Not Payback 2");
		                    				}
		                    				
		                    			// if node 2 did not send method
		                				}else {
		                					// print out error message
		                					System.out.println("Error in Node2");
		                				}
		                				
		                			// if node 2 did not respond
	                				}else {
	                					// error equals true and send error message to client
	                					error = true;
	                            		response = error();
	                            		output = JsonUtils.toByteArray(response);
	                            		NetworkUtils.sendClient(out, output);
	                            	}
	                				
	                			// if node 1 did not send available
	                			}else {
	                				// print out error message
	                				System.out.println("Missing amount for Node1");
	                			}
	                			
	                		// if method for node 1 did not equan can payback
	                		}else {
	                			// deny the request for node 1
	                			System.out.println("Can Not Payback 1");
	                			
	                			// reset the response
	                			response = null;
	                			// ask node 2 if they accept the payback request
                				response = NetworkUtils.send("localhost", port2, canPayback(id,amount2));
                				
                				// if node 2 responds
                				if(response != null) {
                					
                					// if node 2 sends method
	                				if(response.has("method")) {
	                					
	                					// if method equals can payback
	                    				if(response.getString("method").equalsIgnoreCase("can payback")) {
	                    					
	                    					// if node 2 sends available
	                    					if(response.has("available")) {
	                    						
	                    						// read in the amount of money node 2 has available
	                            				amtN2 = response.getInt("available");
	                            				// accept request for node 2
	                            				System.out.println(id + " can Payback");
	                            				canPayback2 = true;
	                            				
	                            			// if node 2 does not send available
	                            			}else {
	                            				// print out error message
	                            				System.out.println("Missing amount for Node2");
	                            			}
	                    				
	                    				// if method is not can payback
	                    				}else {
	                    					// deny request for node 2
	                    					System.out.println("Can Not Payback 2");
	                    				}
	                    				
	                    			// if node 2 did not send method
	                				}else {
	                					// print out error message
	                					System.out.println("Error in Node2");
	                				}
	                				
	                			// if node 2 did not respond
                				}else if(response == null) {
                					// set error to true and send error message to client
                					error = true;
                            		response = error();
                            		output = JsonUtils.toByteArray(response);
                            		NetworkUtils.sendClient(out, output);
                            	}
	                		}
	                		
	                	// if node 1 did not send method
	                	}else {
	                		// print error message
	                		System.out.println("Error in Node1");
	                	}
	                	
	                	// if both node 1 and node 2 accepted payback request
	                	if(canPayback1 && canPayback2) {
	                		// tell node 1 and 2 to payback their calculated amounts
	                		NetworkUtils.send("localhost", port1, payback(id,amount1));
	                		NetworkUtils.send("localhost", port2, payback(id,amount2));
	                		
	                		// update client owes to node 1 in ledger
	                		paybackLedger(id, 1, (int)(owed1 - amount1));
	                		
	                		// update node available for node 1 in ledger
	                		paybackLedger("Node", 1, (int)(amtN1 + amount1));
	                		
	                		// update client owes node 2 in ledger
	                		paybackLedger(id, 2, (int)(owed2 - amount2));
	                		// update node available for node 2 in ledger
	                		paybackLedger("Node", 2, (int)(amtN2 + amount2));
	                		
	                		// reset response
	                		response = new JSONObject();
	                		// load client payback success message
	                		response.put("payback", "You have successfully payed back " + amount 
	                				+ "\nYou currently owe " + ((owed1 + owed2) - amount));
	                		
	                	// if only node 1 accepted payback request
	                	}else if(canPayback1 && !canPayback2) {
	                		// tell node 1 to payback full amount
	                		NetworkUtils.send("localhost", port1, payback(id,amount));
	                		
	                		// update ledger for client owes node 1
	                		paybackLedger(id, 1, (int)(owed1 - amount));
	                		
	                		// update node available for node 1 in ledger
	                		paybackLedger("Node", 1, (int)(amtN1 + amount));
	                		
	                		//reset response
	                		response = new JSONObject();
	                		// load success message
	                		response.put("payback", "You have successfully payed back " + amount 
	                				+ "\nYou currently owe " + ((owed1 + owed2) - amount));
	                		
	                	// if only node 2 accepted payback request
	                	}else if(!canPayback1 && canPayback2) {
	                		// tell node 2 to payback full amount
	                		NetworkUtils.send("localhost", port2, payback(id,amount));
	                		
	                		// update ledger for client owes node 2
	                		paybackLedger(id, 2, (int)(owed2 - amount));
	                		
	                		// update node available for node 2 in ledger
	                		paybackLedger("Node", 2, (int)(amtN2 + amount));
	                		
	                		// reset response
	                		response = new JSONObject();
	                		
	                		// load success message for client
	                		response.put("payback", "You have successfully payed back " + amount 
	                				+ "\nYou currently owe " + ((owed1 + owed2) - amount));
	                		
	                	// if neither node accepted request
	                	}else {
	                		// reset response
	                		response = new JSONObject();
	                		// set failure message
	                		response.put("payback", "The payback attempt was unsuccessful."
	                				+ "\nYou currently owe " + (owed1 + owed2));
	                	}
	                	
	                	// if both nodes responded
	                	if(!error) {
	                		// convert response to byte array
		                	output = JsonUtils.toByteArray(response);
		                	// send to client
		                    NetworkUtils.sendClient(out, output);
	                	}
	                	
	                // if node 1 did not respond
                	}else if(response == null) {
                		// set error to true and send client error message
                		error = true;
                		response = error();
                		output = JsonUtils.toByteArray(response);
                		NetworkUtils.sendClient(out, output);
                	}
                	
                // if client sent exit
	            }else if (message.has("exit")) {
	            	// reset response
	            	response = new JSONObject();
	            	// load good bye message
	            	response.put("exit", "Bye " + id + " ,See you later!");
	            	
	            	// conver response to byte array
	            	output = JsonUtils.toByteArray(response);
	            	// send goodbye message to client
            		NetworkUtils.sendClient(out, output);
            		
            		// set to quit at the end of this loop
            		quitAfter = true;
            		// close resources
            		try {
            			out.close();
            			in.close();
            			conn.close();
            		}catch(Exception e) {
            			System.out.println("Could not close resources");
            		}
	            }
                
                // if mutex is locked
            	if(lock) {
            			// unlock mutex
            			mutex.unlock();
            			System.out.println(id + " realeased the lock");
            			lock = false;
            	}
            	
            	// if quitAfter is true
                if(quitAfter) {
                	// exit while loop
                	quit = true;
                }
            }
            // close the resource
            System.out.println("Bye " +id + ", Closed the resources of client: " + id);
            out.close();
            in.close();
            conn.close();
        } catch (IOException e) {
        	System.out.println("error in performer run()");
            e.printStackTrace();
        } finally {
        	// if mutex is still locked
        	if(lock) {
        		// unlock it
    			mutex.unlock();
    			System.out.println(id + " realeased the lock");
    			lock = false;
        	}
        }
    }
}
