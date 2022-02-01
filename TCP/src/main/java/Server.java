package Assignment3Starter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.json.*;

public class Server {
  /**
   * This is a server for a quote guessing game.
   * The server will allow one client to connect at a time, will
   * ask their name and allow them to play the game.
   * if the client answers 3 correct in a minute they will be put on the leader board
   * the leader board can be displayed.
   * 
   * @author Maura Peterson
   * 
   * @version 1.0
   * 
   * completion time: 10 hours
   */

	// Global variables
	// current client's score
	static int score;
	// current client's number of correct answers
	static int correct;
	// boolean to see if the game is over
	static boolean gameOver;
	// variable to hold the time the game started in nanoseconds
	static long time;
	// variable to get the number of nanoseconds to 1/100 a minute 
	// set to 100 in game
	// if the game is over it is set to 1000000 to prevent a mistaken lose condition
	static long timeDivide;
	// the number of nanoseconds in 1/100 minutes.
	static final long nanoInHundrethMinute = 600000000;
	// the current client's name
	static String name;
	// the socket used to connect with clients
	static Socket sock;
	
	/**
	 * ask the name of the client
	 * @return JSONObject
	 */
    public static JSONObject whatIsName() {
      //create json
      JSONObject json = new JSONObject();
      //make datatype 1 for string
      json.put("datatype", 1);
      //put the string "what is your name in the json
      json.put("data", "What is your name?");
      return json;
    }
  
    /**
     *create a leader board in a text file
     *called leader_board.txt with no scores
     *and the string "No HighScores
     *will be called only if there is no
     *file called leader_board.txt 
     */
    public static void createLeaderBoard() {
    	try {
    		//create file
    		File myObj = new File("leader_board.txt");
    			//if successful
    			if (myObj.createNewFile()) {
    				
    				System.out.println("File created: " + myObj.getName());
    				// open writer to file
    				FileWriter myWriter = new FileWriter("leader_board.txt");
    				// write "No HighScores
    				myWriter.write("No HighScores\n");
    				// close writer
    				myWriter.close();
    				System.out.println("Successfully wrote to the file.");
    				
    			} else {
    				System.out.println("File already exists.");
    			}
    	// catch IOException and print stack trace
    	} catch (IOException e) {
			System.out.println("Could not create leader_board.txt");
			e.printStackTrace();
		}
	}
  
    /**
     * update the leader board after client wins a game
     * either adds them in order of highest score
     * or adds their game score to their overall score and
     * updates the order by highest score.
     */
    public static void updateLeaderBoard() {
    	//String array to hold each line read from the file
	    String[] leaderBoardLine = null;
	    // 2d String array to hold names in [0] and score in [1] for each line
	    String[][] leaderBoard = null;
	    // string read in from file
	    String data = "";
	    // boolean to determine if client's name was found on the leader board
	    boolean found = false;
	    // boolean to determine if client lost overall points and the scores need to
	    // be sorted in reverse order
	    boolean reverse = false;
	    // an int to temporarily hold scores
	    int tempInt = score;
	    // a string to temporarrily hold names
	    String tempName = name;
	    
	    // read in file
	    try {
	        File myObj = new File("leader_board.txt");
	        Scanner myReader = new Scanner(myObj);
	        while (myReader.hasNextLine()) {
	        	data  = data.concat(myReader.nextLine());
	        	// add new line character at the end of each line to seperate
	        	data = data.concat("\n");
	        }
	        myReader.close();
	        // catch file not found exception by creating a new leader board and
	        // calling updateLeaderBoard again
	    } catch (FileNotFoundException e) {
	    	createLeaderBoard();
	    	updateLeaderBoard();
	    }
	    // split the string data from the text file into the leaderBoardLine array by the 
	    // new line character
		leaderBoardLine = data.split("\n");
		// make leaderBoard size the row length of leaderBoardLine x 2
		leaderBoard = new String[leaderBoardLine.length][2];
		
		// split each line of leaderBoardLine at the space into the leaderBoard
		for(int i = 0; i < leaderBoardLine.length; i++) {
			leaderBoard[i] = leaderBoardLine[i].split(" ");
		}
		
		// check leader board for client's name
		for(int i = 0; i < leaderBoard.length; i++) {
			// if found update score, make found true, load name and score
			// into temp values for sorting and check if game score
			// was negative and lowered overall score and if yes set reverse to true
			if(leaderBoard[i][0].equals(name)) {
		    	int boardScore = Integer.parseInt(leaderBoard[i][1]);
		    	boardScore = boardScore + score;
		    	found = true;
		    	tempInt = boardScore;
		    	tempName = name;
		    	if(score < 0) {
		    		reverse = true;
		    	}
		    }
		}
		
		// check if leader board is has "No Highscores" and if yes
		// replace "No" with name and "Highscores" with current score
		if(leaderBoard[0][1].equals("HighScores")) {
			leaderBoard[0][0] = name;
			leaderBoard[0][1] = Integer.toString(score);
			found = true;
		}
		
		// if client's name is not on the leader board add it in order by score
	    if(!found) {
	    	String[][] newLeaderBoard = new String[leaderBoard.length+1][2];
	    	for(int i = 0; i < leaderBoard.length; i++) {
	    		if(leaderBoard[i][1] != "HighScores") {
		    		int scoreLB = Integer.parseInt(leaderBoard[i][1]);
		    		String nameLB = leaderBoard[i][0];
		    		if(scoreLB <= tempInt) {
		    			newLeaderBoard[i][0] = tempName;
		    			newLeaderBoard[i][1] = Integer.toString(tempInt);
		    			tempInt = scoreLB;
		    			tempName = nameLB;		
		    		}else {
		    			newLeaderBoard[i][0] = leaderBoard[i][0];
		    			newLeaderBoard[i][1] = leaderBoard[i][1];
		    		}
	    		}
	    	}
	    	newLeaderBoard[leaderBoard.length][0] = tempName;
			newLeaderBoard[leaderBoard.length][1] = Integer.toString(tempInt);
	    	leaderBoard = newLeaderBoard;
	    	
	    // if reverse is true sort the array in reverse order 
	    }else if(reverse){
	    	for(int i = leaderBoard.length-1; i > 0; i--) {
	    		int scoreLB = Integer.parseInt(leaderBoard[i][1]);
	    		String nameLB = leaderBoard[i][0];
	    		if(scoreLB >= tempInt) {
	    			if(nameLB.equals(name)) {
	    				leaderBoard[i][0] = tempName;
		    			leaderBoard[i][1] = Integer.toString(tempInt);
	    			}else {
		    			leaderBoard[i][0] = tempName;
		    			leaderBoard[i][1] = Integer.toString(tempInt);
		    			tempInt = scoreLB;
		    			tempName = nameLB;	
	    			}
	    		}
	    	}
	    	
	    // if name was found then sort the list
	    }else {
	    	for(int i = 0; i < leaderBoard.length; i++) {
	    		int scoreLB = Integer.parseInt(leaderBoard[i][1]);
	    		String nameLB = leaderBoard[i][0];
	    		if(scoreLB <= tempInt) {
	    			if(nameLB.equals(name)) {
	    				leaderBoard[i][0] = tempName;
		    			leaderBoard[i][1] = Integer.toString(tempInt);
	    			}else {
		    			leaderBoard[i][0] = tempName;
		    			leaderBoard[i][1] = Integer.toString(tempInt);
		    			tempInt = scoreLB;
		    			tempName = nameLB;	
	    			}
	    		}
	    	}
	    }
	    
	    // write the updated leader board to the text file
	    FileWriter myWriter;
		try {
			myWriter = new FileWriter("leader_board.txt");
			for(int i = 0; i < leaderBoard.length; i ++) {
				myWriter.write(leaderBoard[i][0] + " " + leaderBoard[i][1] + "\n");
			}
	        myWriter.close();
		} catch (IOException e) {
			System.out.println("Could not update leader board");
			e.printStackTrace();
		} 
	}
  
    /**
     * Displays the leader board read from text file:
     * leader_board.txt
     * @return JSONObject containing string with leader board
     */
	public static JSONObject displayLeaderBoard() {
		// make JSONObject
		JSONObject json = new JSONObject();
		// add title to leader board
	    String data = "Leader Board\nName Score\n";
	    
	    // read in leader_board.txt
	    try {
	        File myObj = new File("leader_board.txt");
	        Scanner myReader = new Scanner(myObj);
	        while (myReader.hasNextLine()) {
	        	data  = data.concat(myReader.nextLine());
	        	data = data.concat("\n");
	        }
	        myReader.close();
	    // catch file not found exception and create a new leaderboard
	    // and call displayLeaderBoard() again
	    } catch (FileNotFoundException e) {
	    	createLeaderBoard();
	        displayLeaderBoard();
	    }
	    // add text to describe how to return to main menu
	    data = data.concat("Enter your name to return to main menu");
	    // make data type 1 for string and put data into json
	    json.put("datatype", 1);
	    json.put("data", data);
	    return json;
	  }
  
	/**
	 * This method starts the game by setting all the game tracking paramaters 
	 * and sending the first quote then calls getResponse
	 * @param out [the output stream]
	 * @param in [the input stream]
	 * @param quotes [the 2d string array containing quotes and answers]
	 */
	public static void startGame(OutputStream out, InputStream in, String[][] quotes) {
		System.out.println("start game");
		byte[] output = null;
	  
		correct = 0;
		score = 0;
		gameOver = false;
		time = System.nanoTime();
	    timeDivide = 100;
	  
	    try {
	    	JSONObject firstQuote = new JSONObject();
	    	firstQuote = quote(quotes, 0, 0);
		    	  
		    output = JsonUtils.toByteArray(firstQuote);
		    NetworkUtils.Send(out, output);
		  
	    } catch (IOException e) {
			System.out.println("Could not send first quote");
			e.printStackTrace();
	    }
		  
		getResponse(out, in, quotes, 0, 0);
	}
  
	/**
	 * This method listens for a response from the client and decides how to respond
	 * based on client's answer
	 * @param out [output stream]
	 * @param in [input stream]
	 * @param quotes [2d array that holds quotes and correct answers]
	 * @param character [the index of the current character]
	 * @param quote [the index of the current number quote for a character]
	 */
	public static void getResponse(OutputStream out, InputStream in, String[][] quotes, int character, int quote) {
		// byte[] to hold the client's response
		byte[] responseBytes;
		// byte[] to hold the server's output to client
	    byte[] output = null;
	    // json to send output
	    JSONObject json = new JSONObject();
	    // string of the correct answer to the quote
	    String answer = quotes[character][4];
	  
	    // get response and check response
	    try {
	    	//wait for response
	    	boolean wait = true;
	    	while(wait) {
	    		responseBytes = NetworkUtils.Receive(in);
	    		JSONObject response = JsonUtils.fromByteArray(responseBytes);
	    		
	    		// if there is a response
	    		if(response != null) {
	    			
	    			// if time is up client loses the game
	    			if((System.nanoTime() - time)/timeDivide > nanoInHundrethMinute) {
	    				lose(out);
	    				System.out.println("lose");
	    				getResponse(out, in, quotes, character, quote);
	    				
	    			// if response is the clien's name and the game is over go to main menu
	    			}else if((response.getString("selected")).compareToIgnoreCase(name) == 0 && gameOver == true) {
	    				mainMenu(out, in, name);
	    				System.out.println("main menu");
	    				
	    			// if response is quit then close the connection with the client
	    			}else if((response.getString("selected")).compareToIgnoreCase("quit") == 0) {
	    				close(out, in);
	    				System.out.println("quit");
	    				
	    			//if response matches the correct answer update score, send updated score, and check for win
	    			}else if((response.getString("selected")).compareToIgnoreCase(answer) == 0) {
	    				System.out.println("correct");
	    				
	    				//update score
	    				score = score + 5;
	    				if(quote <= 2) {
	    					score = score - quote;
	    				}else {
	    					score = score - 4;
	    				}
	    				
	    				// send score to client
	    				json.put("datatype", 3);
	    				json.put("data", score );
	    				output = JsonUtils.toByteArray(json);
	    				NetworkUtils.Send(out, output);
	    				
	    				//check for win if correct == 3
	    				correct++;
	    				if(correct >= 3) {
	    					System.out.println("win");
	    					win(out);
	    					getResponse(out, in, quotes, character, quote);
	    				}else {
	    					nextTurn(out, in, quotes, character + 1, 0);
	    				}
	    				
	    			// if response is more send the next quote for the current character
	    			}else if((response.getString("selected")).compareToIgnoreCase("more") == 0) {
	    				System.out.println("more");
					
	    				nextTurn(out, in, quotes, character, quote + 1);
	    				
	    			// if response is next lower the score by 2, send score, and send first quote for next character
	    			}else if((response.getString("selected")).compareToIgnoreCase("next") == 0) {
	    				System.out.println("next");
	    				// client loses 2 points
	    				score = score - 2;
	    				// send updated score to client
	    				json.put("datatype", 3);
	    				json.put("data", score );
	    				output = JsonUtils.toByteArray(json);
	    				NetworkUtils.Send(out, output);
	    				// send first quote for next character
	    				nextTurn(out, in, quotes,character + 1, 0);
	    				
	    			// if answer is incorrect send "Try Again and wait for next response
	    			} else {
	    				System.out.println(response.getString("selected"));
	    				json.put("datatype", 1);
	    				json.put("data", "Try Again");
		    	    
	    				output = JsonUtils.toByteArray(json);
	    				NetworkUtils.Send(out, output);
	    				getResponse(out, in, quotes, character, quote);
	    			}
	    		}
	    	}
	    } catch (IOException e) {
	    	System.out.println("failed to get response from client");
	    	e.printStackTrace();
	    }
	}
	
	/**
	 * Check if requested quote is valid and if yes send that quote to client
	 * @param out [output stream]
	 * @param in [input stream]
	 * @param quotes [2d String array for quotes and correct answers]
	 * @param character [index for requested character]
	 * @param quote [index for requested quote]
	 */
	public static void nextTurn(OutputStream out, InputStream in, String[][] quotes, int character, int quote) {
		// output byte array
		byte[] output = null;
		// load character index into an int to be manipulated if invalid
		int currentChar = character;
	  
		// if requested character index is too large loop it to the first character
		if(currentChar > (quotes.length - 1)) {
			currentChar = 0;
			quote = 0;  
		}
	    // if quote index is too large then send a String json that says
		// "No more quotes for this character"
		if(quote > quotes[0].length - 3) {
			try {
				JSONObject json = new JSONObject();
				json.put("datatype", 1);
				json.put("data", "No more quotes for this character");
	  	    
				output = JsonUtils.toByteArray(json);
				NetworkUtils.Send(out, output);
			} catch (IOException e) {
				System.out.println("Could not notify client ther are no more quotes");
				e.printStackTrace();
			}
		// if quote is valid send the quote to the client
		}else {
			try {
				JSONObject JSONquote = new JSONObject();
				JSONquote = quote(quotes, currentChar, quote);
			    	  
				output = JsonUtils.toByteArray(JSONquote);
				NetworkUtils.Send(out, output);
			  
			 } catch (IOException e) {
				System.out.println("Could not send quote to client");
				e.printStackTrace();
			 }
		}
		// get next response
		getResponse(out, in, quotes, currentChar, quote);
	}

	/**
	 * Make a json containing the requested quote image
	 * @param quotes [2d array with quotes image filepaths and correct answers]
	 * @param character [requested character index]
	 * @param quote [requested quote index]
	 * @return JSONObject with image for quote
	 */
	public static JSONObject quote(String[][] quotes, int character, int quote) {
		// json object
		JSONObject json = new JSONObject();
		// file name
		String file;
		
		// load in character part of file path
		file = quotes[character][5];
		// load in quote part of file path
		file = file.concat(quotes[character][quote]);
		
		// print out correct answer for grading purposes
		System.out.println(quotes[character][4]); 
		// load image into json
		try {
			json =image(file);
		} catch (IOException e) {
			System.out.println("Could not load quote image file into json");
			e.printStackTrace();
		}
		return json;
  }

	/**
	 * Takes a filepath for an image and loads the image into a json object.
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	public static JSONObject image(String filepath) throws IOException {
		// JSONObject to hold image
		JSONObject json = new JSONObject();
		// file at file path
		File file = new File(filepath);
		
		// set datatype to 2 for image
		json.put("datatype", 2);
		json.put("type", "image");
		// if file doesn't exist throw IOException
		if (!file.exists()) {
			System.err.println("Cannot find file: " + file.getAbsolutePath());
			IOException e = new IOException();
			throw e;
		}
		// Read in image
		BufferedImage img = ImageIO.read(file);
		byte[] bytes = null;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			ImageIO.write(img, "png", out);
			bytes = out.toByteArray();
		}
		if (bytes != null) {
			Base64.Encoder encoder = Base64.getEncoder();
			json.put("data", encoder.encodeToString(bytes));
			return json;
		}
		return error("Unable to save image to byte array");
	}

	/**
	 * This method initializes the 2d array containing the quotes
	 * @param character [number of characters in array]
	 * @param quote [number of quotes per character]
	 * @return 2d String array containing quote file paths, correct answers, and character file paths.
	 */
	public static String[][] initializeQuotes(int character, int quote) {
		// make a 2d string array with rows == character and columns == quotes + 2(to hold answer and character file path
		String[][] quotes = new String[character][quote+2];
	    // load in the quote file path from column 0 to column quote - 1
		for(int i = 0; i < character; i++) {
			for(int j = 0; j < quote; j++) {
				quotes[i][j] = "/quote" +( j + 1 )+ ".png";
			}
		}
		
		// load in correct answers into column quote
		// load in character part of file path into column quote + 1
		quotes[0][quote] = "Captain America";
		quotes[0][quote +  1] = "img/Captain_America";
		quotes[1][quote] = "darth vader";
		quotes[1][quote + 1] = "img/Darth_Vader";
		quotes[2][quote] = "homer simpson";
		quotes[2][quote + 1] = "img/Homer_Simpson";
		quotes[3][quote] = "jack sparrow";
		quotes[3][quote + 1] = "img/Jack_Sparrow";
		quotes[4][quote] = "joker";
		quotes[4][quote + 1] = "img/Joker";
		quotes[5][quote] = "tony stark";
		quotes[5][quote + 1] = "img/Tony_Stark";
		quotes[6][quote] = "wolverine";
		quotes[6][quote + 1] = "img/Wolverine";
    
		return quotes;
	}
  
	/**
	 * This method sends lose image to client sets the gameOver to true
	 * changes timeDivide to prevent mistaken lose() call.
	 * @param out [output stream]
	 */
	public static void lose(OutputStream out) {
		// byte[] for output
		byte[] output = null;
		//JSONObject to send to client
		JSONObject JSONquote = new JSONObject();
	  
		gameOver = true;
		timeDivide = 1000000;
	  
		// send image
		try {
			JSONquote = image("img/lose.jpg");	  
			output = JsonUtils.toByteArray(JSONquote);
			NetworkUtils.Send(out, output);
		} catch (IOException e) {
			System.out.println("Could not send lose image to client");
			e.printStackTrace();
		}
	}
  
	/**
	 * This method is called if client wins and sends the win image to client,
	 * sets gameOver to true, and changes the timeDivide to prevent mistaken lose() call
	 * @param out [output stream]
	 */
	public static void win(OutputStream out) {
		// byte[] for output
		byte[] output = null;
		//JSONObject to send to client
		JSONObject JSONquote = new JSONObject();
	  
		gameOver = true;
		timeDivide = 1000000;
	  
		try {
			JSONquote = image("img/win.jpg");	  
			output = JsonUtils.toByteArray(JSONquote);
			NetworkUtils.Send(out, output);
		} catch (IOException e) {
			System.out.println("Could not send win image to client");
			e.printStackTrace();
		}
		updateLeaderBoard();
	}
  
	/**
	 * This method sends the main menue and takes the response
	 * either 1 for leader board or 2 to play game
	 * @param out
	 * @param in
	 * @param name1
	 */
	public static void mainMenu(OutputStream out, InputStream in, String name1) {
		JSONObject returnMessage = new JSONObject();
		JSONObject message;
		byte[] output;
		byte[] messageBytes;
	  
		// recieve message from client and direct accordingly
		try {
			// if we do not have the clients name get name from message, set it to global variable name
			if(name1 == null) {
				messageBytes = NetworkUtils.Receive(in);
				message = JsonUtils.fromByteArray(messageBytes);
	          
				if (message.has("name")) {
					if (message.get("name") instanceof String) {
						name = message.getString("name");
					}
				}
			}
			// send main menu option string to client
			returnMessage.put("datatype", 1);
			returnMessage.put("data", "Hello " + name + "!\nEnter: 1 to see leader board.\nEnter: 2 to play game.");
	      
			// we are converting the JSON object we have to a byte[]
			output = JsonUtils.toByteArray(returnMessage);
			NetworkUtils.Send(out, output);
	    
			// get client menu selection
			messageBytes = NetworkUtils.Receive(in);
			message = JsonUtils.fromByteArray(messageBytes);
          
			// if message is 1 display leader board
			if( message.has("selected")) {
				if (message.get("selected").equals("1")) {
			  		System.out.println("selected is 1");
			  		returnMessage= displayLeaderBoard();
			  	// if message is 2 start game
				}else if(message.get("selected").equals("2")) {
			  		System.out.println("selected is 2");
			        startGame(out, in, initializeQuotes(7,4));
			     // else send error
				}else {
					returnMessage = error("Invalid selection: " + message.get("selected") + " is not an option");
				}
			}
		  
			// we are converting the JSON object we have to a byte[]
			output = JsonUtils.toByteArray(returnMessage);
			NetworkUtils.Send(out, output);
			
			// if leader board is selected go back to main menu after client reenters their name
			if(!message.get("selected").equals("2")) {
				mainMenu(out, in, null);
			}
		
		} catch (IOException e) {
			System.out.println("IOException in main menu");
			e.printStackTrace();
		}
	}
  
	/**
	 * This method closes the connection with the client
	 * @param out [output stream]
	 * @param in [input stream]
	 */
	public static void close(OutputStream out,InputStream in) {
		try {
			out.close();
			in.close();
			sock.close();
		} catch (IOException e) {
			System.out.println("Could not close connection with client");
			e.printStackTrace();
		}
	}

	/**
	 * this method sends an error message to client
	 * @param err [string that holds the error message]
	 * @return json containing error message
	 */
	public static JSONObject error(String err) {
		JSONObject json = new JSONObject();
		json.put("error", err);
		return json;
	}

	/**
	 * The main method
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		int port = 8080;
		try {
			port = Integer.parseInt(args[0]);
			
		}catch (Exception e) {
	        System.out.println("Argument: " + args[0] + " must be an integer.");
	        System.exit(1);
	    }
		
		// server socket
		ServerSocket serv = null;
		try {
			serv = new ServerSocket(port);
			// NOTE: SINGLE-THREADED, only one connection at a time
			while (true) {
				sock = null;
				try {
					sock = serv.accept(); // blocking wait
					// output and input stream
					OutputStream out = sock.getOutputStream();
					InputStream in = sock.getInputStream();
					// byte[] to hold output
					byte[] output;
					
					// send response while connected with client
					while (true) {
						// JSONObject to send to client
						JSONObject returnMessage = null;
						
						// get String json to ask client their name
						returnMessage = whatIsName();
						output = JsonUtils.toByteArray(returnMessage);
						NetworkUtils.Send(out, output);
						System.out.println("Asked name");
        		  
						// get image json to say hi to client
						returnMessage= image("img/hi.png");
						output = JsonUtils.toByteArray(returnMessage);
						NetworkUtils.Send(out, output);
						System.out.println("sent hi image");
        	  
						// go to the main menu
						mainMenu(out, in, null);
					}
				} catch (Exception e) {
					System.out.println("Client disconnect");
				// close socket if client disconnects
				} finally {
					if (sock != null) {
						sock.close();
					}
				}
			}
		} catch (IOException e) {
			System.out.println("IOException in main");
			e.printStackTrace();
		// close socket if IOException in main
		} finally {
			if (serv != null) {
				serv.close();
			}
		}
	}
}