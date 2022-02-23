package Activity2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Scanner;

import org.json.JSONObject;

/**
 * This class holds the methods for the nodes
 * @author Maura
 * @version 1
 */
public class Nodes extends Node {

  // port that the node is on
  private static int port;
  // amount of available money the node has
  private int amount;
  
  /**
   * Constructor for node objects
   * @param port[port the node is on]
   * @param amount[ amount of money the node has available]
   */
  public Nodes(int port, int amount) {
    super (port);
    this.port = port;
    this.amount = checkLedgerAmount("Node");
    // if node money is not in ledger set amount to amount
    if(this.amount == -1) {
    	this.amount = amount;
    }
    
    // update node amount in ledger
    updateNodeAmount();
  }

  /**
   * Builds a json object with the ledger held in a string
   */
  public JSONObject verify(JSONObject object) {
	  JSONObject ret = new JSONObject();
	  ret.put("method", "verify");
	  ret.put("ledger", getLedger());
	  return ret;
  }
  
  /**
   * decide if the payback request should be accepted
   * Build a json object with the decision
   */
  @Override
  public JSONObject canPayback(JSONObject object) {
	  //owed = amount client owes
	  // pay = amount client wants to pay this node
	int owed, pay;
	// does client have credit
	Boolean hasNoCredit = false;
	// is the amount less than or equal to the amount owed
	Boolean amountOk = false;
	
	// if client id was sent by leader
	if (object.has("ID")) {
		  // check ledger for client id
		  System.out.println(object.get("ID") + " wants to payback");
		   hasNoCredit = checkLedger(object.getString("ID"));
	}
	
	// if amount was sent by leader
	if(object.has("amount")) {
		// set pay to amount
		pay = object.getInt("amount");
		// check ledger for amount owed by client
		owed = checkLedgerAmount(object.getString("ID"));
		System.out.println("pay: " + pay);
		System.out.println("owed: "+ owed);
		
		// if client wants to pay node less than or equal to the amount owed
		if(pay <= owed) {
			// amount is ok
			amountOk = true;
		
		// if client wants to pay node more than the amount owed
		}else {
			// amount is not ok
			amountOk = false;
		}
	}
	
	// if client has credit and the amount is ok
	if(!hasNoCredit && amountOk) {
		
			// build and return acceptance response
		  System.out.println(object.getString("ID") + " can payback");
		  	JSONObject ret = new JSONObject();
		    ret.put("method", "can payback");
		    ret.put("available", amount);
		    return ret;
		    
	  // if client does not have credit or the amount is not ok
	  }else {
		  // build and return denial response
		  System.out.println(object.getString("ID") + " can not payback");
		  	JSONObject ret = new JSONObject();
		    ret.put("method", "can not payback");
		    return ret;
	  }
  }

  /**
   * Decide whether to accept the clients credit request sent from leader
   * Build and return a json object with that decision
   */
  @Override
  public JSONObject canCredit(JSONObject object) {
	  // does the client not have credit already
	  Boolean hasNoCredit = false;
	  // is the amount requested less than 66% the total available money for this node
	  Boolean amountOk = false;
	  
	  // if the leader sent id
	  if (object.has("ID")) {
		  // check the ledger to make sure client has no credit
		  System.out.println(object.get("ID")+ " wants to take out credit");
		   hasNoCredit = checkLedger(object.getString("ID"));
	  }
	  
	  // if the leader sent amount
	  if (object.has("amount")) {
		  // check that the node has 1.5x the amount
		  System.out.println("Requested Credit " + object.get("amount"));
		  int amt = object.getInt("amount");
		  
		  // if it does
		  if(amt *1.5 <= amount) {
			  // amount  is ok
			  amountOk = true;
		  }
	  }
	  
	  // if the client has no credit and the amount is ok
	  if(hasNoCredit && amountOk) {
		  // build and return acceptance response
		  System.out.println("can credit " + object.getString("ID") + " " + object.get("amount"));
		  	JSONObject ret = new JSONObject();
		    ret.put("method", "can credit");
		    ret.put("available", amount);
		    return ret;
		    
	  // if the client has credit or the amount is too large
	  }else {
		  // build and return refusal response
		  System.out.println("can not credit " + object.getString("ID") + " " + object.get("amount"));
		  	JSONObject ret = new JSONObject();
		    ret.put("method", "can not credit");
		    return ret;
	  }
  }

  /**
   * Applies the credit to the client
   */
  @Override
  public JSONObject credit(JSONObject object) {
	  // amount of credit applied
	  int amt = 0;
	  // client id
	  String id = "";
	  
	  // if the leader sent an amount
	  if(object.has("amount")) {
		  
		  // get amount
		  amt = object.getInt("amount");
		  
		  // if the leader sent an ID
		  if(object.has("ID")) {
			  
			  // get id
			  id = object.getString("ID");
			  System.out.println(id + " has been credited " + amt);
			  
		  // if leader did not provide id
		  }else {
			  // print out error message and return null
			  System.out.println("Missing ID in credit");
			  return null;
		  }
		  
	  // if leader did not provide amount
	  }else {
		  // print out error message and return null
		  System.out.println("Missing amount in credit");
		  return null;
	  }
	  
	  // update ledger for client
	  creditLedger(id,amt);
	  
	  // update available money for node
	  amount = amount - amt;
	  updateNodeAmount();
	  
	  	// build and return response
	  	JSONObject ret = new JSONObject();
	    ret.put("method", "credit");
	    return ret;
  }

  /**
   * Payback credit for client
   */
  @Override
  public JSONObject payback(JSONObject object) {
	  // amount to payback
	  int amt = 0;
	  // client id
	  String id = "";
	  
	  // if leader sent amount
	  if(object.has("amount")) {
		  
		  // get amount
		  amt = object.getInt("amount");
		  
		  // if leader sent id
		  if(object.has("ID")) {
			  
			  // get id
			  id = object.getString("ID");
			  System.out.println(id + " has payed back " + amt);
			  
		  // if leader did not send id
		  }else {
			  // print out error message and return null
			  System.out.println("Missing ID in payback");
			  return null;
		  }
		  
	  // if leader did not send amount
	  }else {
		  // print out error message and return null
		  System.out.println("Missing amount in payback");
		  return null;
	  }
	  
	  // update ledger for client
	  paybackLedger(id,amt);
	  
	  // update amount of money node has available
	  amount = amount + amt;
	  updateNodeAmount();
	  
	  // build and return success response
	  JSONObject ret = new JSONObject();
	  ret.put("method", "payback");
	  return ret;
  }

  /**
   * build and return error response
   */
  public JSONObject error(String error) {
    JSONObject ret = new JSONObject();
    ret.put("error", error);
    return ret;
  }

  /**
   * returns a string holding the ledger for a node
   * @return[string holding ledger for node]
   */
  public static String getLedger() {
	  // string to hold ledger
	  String data = "";
	  
	  try {
		  	// open file
	        File myObj = new File("ledger"+ port + ".txt");
	        // open scanner
	        Scanner myReader = new Scanner(myObj);
	        // while file not at end
	        while (myReader.hasNextLine()) {
	        	data  = data.concat(myReader.nextLine());
	        	// add new line character at the end of each line to seperate
	        	data = data.concat("\n");
	        }
	        myReader.close();
	        // catch file not found exception by creating a new ledger and
	        // calling updateledger again
	    } catch (FileNotFoundException e) {
	    	System.out.println("Ledger not found");
	    	createLedger();
	    	getLedger();
	    }
	  return data;
  }
  
  /**
   * 
   */
  public void updateNodeAmount() {
	//String array to hold each line read from the file
	    String[] ledgerLine = null;
	    // 2d String array to hold client ids in [0] and credit in [1] for each line
	    String[][] ledger = null;
	    // string read in from file
	    String data = "";
	    String newData = "";
	    String ID = "Node";
	    Boolean found = false;
	    
	    // read in file
	    try {
	        File myObj = new File("ledger"+ port + ".txt");
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
		// make ledger size the row length of ledgerLine x 2
		ledger = new String[ledgerLine.length][2];
		
		// split each line of ledgerLine at the space into the ledger
		for(int i = 0; i < ledgerLine.length; i++) {
			ledger[i] = ledgerLine[i].split(" ");
		}
		
		if(data.equals("")) {
			
		}else {
			// check ledger for Node
			for(int i = 0; i < ledger.length; i++) {
				// if found update amount
				if(ledger[i][0].equals(ID)) {
			    	found = true;
			    	int tempInt = amount;
			    	if(tempInt > 0) {
			    		ledger[i][1] = Integer.toString(tempInt);
			    		newData = newData.concat(ledger[i][0] + " " + ledger[i][1] + "\n");
			    	}else {
			    		
			    	}
			    } else {
			    	newData = newData.concat(ledger[i][0] + " " + ledger[i][1] + "\n");
			    }
			}
		}
		
		// if Node is not in the ledger
		if(!found) {
			// add Node to the front of the ledger with current amount
			String temp = newData;
			String amt = Integer.toString(amount);
			newData = "Node ";
			newData = newData.concat(amt);
			newData = newData.concat("\n");
			newData = newData.concat(temp);
		}
		
		
		try {
		// open writer to file
		FileWriter myWriter = new FileWriter("ledger"+ port + ".txt");
		myWriter.write(newData);
		myWriter.close();
		}catch(IOException e) {
			System.out.println("Could not write to ledger in payback");
		}
  }
  
  /**
   * check how much is owed for a client or checks node money available
   * @param ID[either client ID or Node to check node money]
   * @return[credit for client or money available for node]
   */
  public static int checkLedgerAmount(String ID) {
	  	//String array to hold each line read from the file
		    String[] ledgerLine = null;
		    // 2d String array to hold names in [0] and score in [1] for each line
		    String[][] ledger = null;
		    // string read in from file
		    String data = "";
		    
		    // read in file
		    try {
		        File myObj = new File("ledger"+ port + ".txt");
		        Scanner myReader = new Scanner(myObj);
		        while (myReader.hasNextLine()) {
		        	// if id paramater is Node
		        	if(ID.equals("Node")) {
		        		// read in first line
		        		data  = data.concat(myReader.nextLine());
		        		// if file is not empty
		        		if(!(data.equals(""))) {
		        			// split line by space
		        			ledgerLine = data.split(" ");
		        			
		        			// if the id is Node
		        			if(ledgerLine[0].equals("Node")) {
		        				// return amount for Node in ledger
		        				return Integer.parseInt(ledgerLine[1]);
		        				
		        			// if the first line is not Node
		        			}else {
		        				// return -1
		        				return -1;
		        			}
		        			
		        		// if the file is empty
		        		}else {
		        			// return negative 1
		        			return -1;
		        		}
		        	}
		        	
		        	// if ID is not Node read in file
		        	data  = data.concat(myReader.nextLine());
		        	// add new line character at the end of each line to seperate
		        	data = data.concat("\n");
		        }
		        myReader.close();
		        
		        // if file is empty and id == Node
		        if(data.equals("") && ID.equals("Node")) {
		        	// return -1
		        	return -1;
		        }
		        
		        // catch file not found exception by creating a new ledger and
		        // calling updateledger again
		    } catch (FileNotFoundException e) {
		    	createLedger();
		    	return checkLedgerAmount(ID);
		    }
		    
		    // split the string data from the text file into the ledgerLine array by the 
		    // new line character
			ledgerLine = data.split("\n");
			// make ledger size the row length of ledgerLine x 2
			ledger = new String[ledgerLine.length][2];
			
			// split each line of ledgerLine at the space into the ledger
			for(int i = 0; i < ledgerLine.length; i++) {
				ledger[i] = ledgerLine[i].split(" ");
			}
			
			// check ledger for client's id
			for(int i = 0; i < ledger.length; i++) {
				// if found return the credit owed
				if(ledger[i][0].equals(ID)) {
			    	return Integer.parseInt(ledger[i][1]);
			    } else {
			    	
			    }
			}
			
			// if client id is not found return 0
			return 0;
	  }
  
  /**
   * checks if the client is on the ledger
   * @param ID[client id]
   * @return[false if on ledger, true if not on ledger]
   */
  public static Boolean checkLedger(String ID) {
  	//String array to hold each line read from the file
	    String[] ledgerLine = null;
	    // 2d String array to hold client ids in [0] and credit in [1] for each line
	    String[][] ledger = null;
	    // string read in from file
	    String data = "";
	    // boolean to determine if client's id was found on the ledger
	    boolean found = false;
	    
	    // read in file
	    try {
	        File myObj = new File("ledger"+ port + ".txt");
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
		// make ledger size the row length of ledgerLine x 2
		ledger = new String[ledgerLine.length][2];
		
		// split each line of ledgerLine at the space into the ledger
		for(int i = 0; i < ledgerLine.length; i++) {
			ledger[i] = ledgerLine[i].split(" ");
		}
		
		// check ledger for client's id
		for(int i = 0; i < ledger.length; i++) {
			// if found, found is true
			if(ledger[i][0].equals(ID)) {
		    	found = true;
		    } else {
		    	
		    }
		}
		
		//return not found
		return !found;
  }
  
  /**
   * update ledger for payback
   * @param ID[client id]
   * @param amt[amount to be payed back]
   */
  public static void paybackLedger(String ID,int amt) {
	  	//String array to hold each line read from the file
		    String[] ledgerLine = null;
		    // 2d String array to hold client ids in [0] and credit in [1] for each line
		    String[][] ledger = null;
		    // string read in from file
		    String data = "";
		    String newData = "";
		    
		    // read in file
		    try {
		        File myObj = new File("ledger"+ port + ".txt");
		        Scanner myReader = new Scanner(myObj);
		        while (myReader.hasNextLine()) {
		        	data  = data.concat(myReader.nextLine());
		        	// add new line character at the end of each line to seperate
		        	data = data.concat("\n");
		        }
		        myReader.close();
		        // catch file not found exception by creating a new ledger and
		        // calling paybackLedger again
		    } catch (FileNotFoundException e) {
		    	System.out.println("Node ledger is lost");
		    	createLedger();
		    }
		    // split the string data from the text file into the ledgerLine array by the 
		    // new line character
			ledgerLine = data.split("\n");
			// make ledger size the row length of ledgerLine x 2
			ledger = new String[ledgerLine.length][2];
			
			// split each line of ledgerLine at the space into the ledger
			for(int i = 0; i < ledgerLine.length; i++) {
				ledger[i] = ledgerLine[i].split(" ");
			}
			
			// check ledger for client's id
			for(int i = 0; i < ledger.length; i++) {
				// if found update amount owed = owed - amt
				if(ledger[i][0].equals(ID)) {
			    	String temp = (ledger[i][1]);
			    	int tempInt = Integer.parseInt(temp)- amt;
			    	if(tempInt > 0) {
			    		ledger[i][1] = Integer.toString(tempInt);
			    		newData = newData.concat(ledger[i][0] + " " + ledger[i][1] + "\n");
			    	}else {
			    		
			    	}
			    // if not the client just copy
			    } else {
			    	newData = newData.concat(ledger[i][0] + " " + ledger[i][1] + "\n");
			    }
			}
			
			try {
				// open writer to file
				FileWriter myWriter = new FileWriter("ledger"+ port + ".txt");
				// write to file
				myWriter.write(newData);
				myWriter.close();
			}catch(IOException e) {
				System.out.println("Could not write to ledger in payback");
			}
	  }
  
  /**
   * Update credit to ledger
   * @param id[client id]
   * @param amount[amount to be credited]
   */
  public static void creditLedger(String id, int amount) {
	  try {
		// hold the data from file
		String data = "";
		// open reader for file
		File myObj = new File("ledger"+ port + ".txt");
		  Scanner myReader = new Scanner(myObj);
	        while (myReader.hasNextLine()) {
	        	data  = data.concat(myReader.nextLine());
	        	// add new line character at the end of each line to seperate
	        	data = data.concat("\n");
	        }
	        myReader.close();
	  	// open writer to file
		FileWriter myWriter = new FileWriter("ledger"+ port + ".txt");
		// copy old file
		myWriter.append(data);
		// add new credit at the end
		myWriter.append(id + " " + amount+ "\n");
		// close writer
		myWriter.close();
		System.out.println("Successfully wrote to the file.");
	  }catch(IOException e) {
		  System.out.println("Could not updateLedger");
	  }
  }
  
  /**
   * Creates a blank ledger
   */
  public static void createLedger() {
  	try {
  		//create file
  		File myObj = new File("ledger"+ port + ".txt");
  			//if successful
  			if (myObj.createNewFile()) {
  				
  				System.out.println("File created: " + myObj.getName());
  				
  			} else {
  				System.out.println("File already exists.");
  			}
  	// catch IOException and print stack trace
  	} catch (IOException e) {
			System.out.println("Could not create ledger"+ port + ".txt");
			e.printStackTrace();
		}
	}
  
  /**
   * Entry point for nodes
   * @param args[port, node number]
   */
  public static void main(String[] args) {
	// create a new node
    new Nodes(Integer.valueOf(args[1]), Integer.valueOf(args[0])).run();
  }
}