package server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

import org.json.*;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

class SockBaseServer extends Thread{
	// file name for log file
    static String logFilename = "logs.txt";
    // file name for leader board file
    static String leaderBoardFilename = "leaderBoard.txt";
    // server socket
    ServerSocket serv = null;
    // input stream
    InputStream in = null;
    // output stream
    OutputStream out = null;
    // client socket
    Socket clientSocket = null;
    // default port
    int port = 9099;
    // current game
    Game game;
    // client's name
    String name = "";
    // mutex to protect critical area
    Semaphore mutex;
    // 2d string array to hold all tasks and their answers
    String[][] tasks;
    // string to hold answer to current task
    String taskAnswer;
    // string to hold current task
    String task;
    // number of wins of current client
    int wins;
    // number of tiles that should be revealed for each correct answer
    // minimum of 6
    // plus 6 for each win
    // maximum of 30
    int tileReveal;

    /**
     * Constructor method used to initialize each connection with a client
     * @param sock[socket to client]
     * @param game[current game]
     * @param mutex[mutex]
     */
    public SockBaseServer(Socket sock, Game game, Semaphore mutex){
    	// initialize variables
        this.clientSocket = sock;
        this.game = game;
        this.mutex = mutex;
        wins = 0;
        setTasks();
        // open streams
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (Exception e){
            System.out.println("Could not connect with client: " + e);
        }
    }
    
    /**
     * This method handles the game turn logic
     * @param out[output stream]
     * @param in[input stream]
     */
    private void gameTurn(OutputStream out,InputStream in) {
    	// get current game image
    	String image = game.getImage();
    	// get current reveal index
    	int currentIdx = 0;
    	
    	try {
    		// while there are no game updates from other clients
	    	while(game.getImage().equals(image)) {
	    		// if the client has sent an answer
	    		if(in.available() != 0) {
	    			// read in request from client
			    	Request op = Request.parseDelimitedFrom(in);
			        
			    	// if request is of type ANSWER
			        if(op.getOperationType() == Request.OperationType.ANSWER) {
			        	// make a response
			        	Response response;
			        	
			        	// if answer from client is correct
			        	if(op.getAnswer().compareToIgnoreCase(taskAnswer)==0) {
			        		// print out correct on server
			        		System.out.println(name + "is correct");
			        		
			        		// update image
			        		try {
			        			mutex.acquire();
			        			System.out.println(tileReveal);
			        			image = replace(tileReveal);
			        		}catch (InterruptedException e) {
			        			System.out.println("Could not update image tiles");
			        			e.printStackTrace();
			        		} finally {
			        			mutex.release();
			        		}
			        		
			        		// if there are still hidden tiles
			        		if(game.getIdx() < game.getIdxMax()) {
			        			//get next task
			        			String[] task = getTask();
			            		this.taskAnswer = task[1];
			            		this.task = task[0];
			            		
			            		// build new response
			        			response = Response.newBuilder()
			        					// of type task
			                            .setResponseType(Response.ResponseType.TASK)
			                            // set updated image
			                            .setImage(image)
			                            // set new task
			                            .setTask(task[0])
			                            .build();
			        			
			        			// send response
			        			response.writeDelimitedTo(out);
			        			// print answer for grading ease
			        			System.out.println(name + "'s task answer: " + taskAnswer);
			        			// next turn
			        			gameTurn(out,in);
			        			
			        		// if there are no hidden tiles
			        		}else {
			        			// print "winner"
			        			System.out.println("Winner");
			        			
			        			// build new response
			        			response = Response.newBuilder()
			        					// of type WON
			                            .setResponseType(Response.ResponseType.WON)
			                            // set completed image
			                            .setImage(image)
			                            .build();
			        			
			        					// update game status
			        					game.setWon();
			        			
			        			// send response
			        			response.writeDelimitedTo(out);
			        			
			        			// update the leader board
			        			try {
			        	    		mutex.acquire();
			        	    		writeToLeaderBoard(name,"win");
			        	    		
			        	    	}catch(InterruptedException e) {
			        				System.out.println("Could not update leader board: ");
			        				e.printStackTrace();
			        				
			        			} finally {
			        				mutex.release();
			        			}
			        			
			        			// return to the main menu
			        			mainMenu(out,in);
			        		}		
			        	// if the answer is exit
			        	}else if(op.getAnswer().compareToIgnoreCase("exit")==0){
			        		// build a new response
			        		response = Response.newBuilder()
			        				// of type BYE
			                        .setResponseType(Response.ResponseType.BYE)
			                        // set goodbye message
			                        .setMessage("Bye "+this.name+"! See you soon! :)")
			                        .build();
			        		// print bye on server 
				        	System.out.println("Bye " + name);
			        		
			        		// send response
			                response.writeDelimitedTo(out);
			                
			            // if answer is incorrect
			        	}else {
			        		// print out who was incorrect
			        		System.out.println(name + "was incorrect");
			        		
			        		// build new response
			        		response = Response.newBuilder()
			        				// of type task
		                            .setResponseType(Response.ResponseType.TASK)
		                            // set image
		                            .setImage(image)
		                            // add try again to begenning of current task
		                            .setTask("Try again\n" + task)
		                            .build();
			        		
			        		// send response
		        			response.writeDelimitedTo(out);
		        			
		        			// print answer for grading ease
		        			System.out.println(name + "'s task answer: " + taskAnswer);
		        			
		        			// next turn
		        			gameTurn(out,in);
			        	}
			        
			        // if response is not of type ANSWER
			        }else {
			        	// build new response
			        	Response response = Response.newBuilder()
			        			// of type error
			                    .setResponseType(Response.ResponseType.ERROR)
			                    // set error message
			                    .setMessage("Unexpected request type")
			                    .build();
			        	
			        	// send response
			            response.writeDelimitedTo(out);
			        }
	    		}
	    	}
	    	// if the image has been updated by another client
	    	// get current index
	    	try {
	    		mutex.acquire();
	    		currentIdx = game.getIdx();
	    	}catch(InterruptedException e) {
				System.out.println("Could not check game index:");
				e.printStackTrace();
			} finally {
				mutex.release();
			}
	    	
	    	// if there are still hidden tiles
	    	if(currentIdx < game.getIdxMax()) {
	    		
	    		// build a new response
		    	Response response = Response.newBuilder()
		    			// of type TASK
		                .setResponseType(Response.ResponseType.TASK)
		                // set updated image
		                .setImage(game.getImage())
		                // set current task
		                .setTask(task)
		                .build();
		    	
		    	// send response
				response.writeDelimitedTo(out);
				
				// print answer for grading ease
				System.out.println(name + "'s task answer: " + taskAnswer);
				
				// next turn
				gameTurn(out,in);
				
			// if there are no hidden tiles
	    	}else {
	    		// print "winner"
	    		System.out.println("Winner");
	    		
	    		// build a new response
				Response response = Response.newBuilder()
						// of type WON
	                    .setResponseType(Response.ResponseType.WON)
	                    // get updated image
	                    .setImage(game.getImage())
	                    .build();
				
						// update game status
						game.setWon();
						
				// send response
				response.writeDelimitedTo(out);
				
				// update leader board
				try {
		    		mutex.acquire();
		    		writeToLeaderBoard(name, "win");
		    		
		    	}catch(InterruptedException e) {
					System.out.println("Could not update leader board");
					e.printStackTrace();
					
				} finally {
					mutex.release();
				}
				
				// return to main menu
				mainMenu(out,in);
	    	}
	    	
	    // catch IOException
    	}catch (IOException e) {
    		System.out.println(name + "disconnected");
    	}
    }
    
    /**
     * This method holds the logic for the main menu
     * @param out [output stream]
     * @param in [input stream]
     */
    private void mainMenu(OutputStream out,InputStream in) throws IOException {
    	
    	try {
    		// build a new response
	        Response response = Response.newBuilder()
	        		// of type GREETING
	                .setResponseType(Response.ResponseType.GREETING)
	                // set greeting message
	                .setMessage("Hello " + this.name + " and welcome. \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - to quit")
	                .build();
	        
	        // send response
	        response.writeDelimitedTo(out);
	    	
	        // get a request from client
	        Request op = Request.parseDelimitedFrom(in);
	        
	        // if request is of type LEADER
	        if(op.getOperationType() == Request.OperationType.LEADER) {
	        	// send leader board
	        	sendLeaderBoard(out,in);
	        	
	        // if request is of type NEW
	        }else if(op.getOperationType() == Request.OperationType.NEW) {
	        	// start a new game
	        	game.newGame();
	        	
	        	// get a random task
	        	String[] task = getTask();
	        	// set current task answer
	    		this.taskAnswer = task[1];
	    		// set current task
	    		this.task = task[0];
	    		
	    		// build a new response
	        	response = Response.newBuilder()
	        			// of type TASK
	                    .setResponseType(Response.ResponseType.TASK)
	                    // set game image
	                    .setImage(game.getImage())
	                    // set task
	                    .setTask(this.task)
	                    .build();
	        	
	        			// print out answer for grading ease
	        			System.out.println(name + "'s task answer: " + taskAnswer);
	        			
	        	// send response
	            response.writeDelimitedTo(out);
	            
	            // next turn
	            gameTurn(out, in);
	            
	        // if request is of type QUIT
	        }else if(op.getOperationType() == Request.OperationType.QUIT) {
	        	
	        	// build a new response
	        	response = Response.newBuilder()
	        			// of type BYE
	                    .setResponseType(Response.ResponseType.BYE)
	                    // set goodbye message
	                    .setMessage("Bye "+this.name+"! See you soon! :)")
	                    .build();
	        	// print bye on server 
	        	System.out.println("Bye " + name);
	        	
	        	// send response
	            response.writeDelimitedTo(out);
	            
	        // if request is not of type LEADER, NEW, or QUIT    
	        }else {
	        	// print out error message
	        	System.out.println("Unexpected request type from "+ name);
	        	
	        	// build a new response
	        	response = Response.newBuilder()
	        			// of type ERROR
	                    .setResponseType(Response.ResponseType.ERROR)
	                    // set error message
	                    .setMessage("Unexpected request type")
	                    .build();
	        	
	        	// send response
	            response.writeDelimitedTo(out);
	        }
	        
	    // catch IOException
    	}catch(IOException e) {
    		System.out.println("IOException in main menu for: " + name);
    		e.printStackTrace();
    	}
    }


    /**
     * This method is the entry point for each cliet connection
     */
    public void run() {
        // print ready
        System.out.println("Ready...");
        try {
            // read the proto object and put into new objct
            Request op = Request.parseDelimitedFrom(in);

            // if the operation is NAME (so the beginning then say there is a commention and greet the client)
            if (op.getOperationType() == Request.OperationType.NAME) {
                // get name from proto object
            	this.name = op.getName();

            	// writing a connect message to the log with name and CONNENCT
            	writeToLog(this.name, Message.CONNECT);
            	writeToLeaderBoard(this.name, "login");
                System.out.println("Got a connection and a name: " + this.name);
            }

            // main menu
            mainMenu(out, in);

        // catch Exception if the client disconnects unexpectedly
        } catch (IOException ex) {
        	System.out.println("Client: " +name+ " disconnected");
            ex.printStackTrace();
        } 
    }

    /**
     * Replaces num characters in the image. I used it to turn more than one x when the task is fulfilled
     * @param num -- number of x to be turned
     * @return String of the new hidden image
     */
    public String replace(int num){
        for (int i = 0; i < num; i++){
            if (game.getIdx()< game.getIdxMax())
                game.replaceOneCharacter();
        }
        return game.getImage();
    }

    /**
     * Gets a random task from the task list
     * @return [string array with task and task answer]
     */
    public String[] getTask() {
    	Random rand = new Random(); 
        int randInt = rand.nextInt(tasks.length);
        
        return tasks[randInt];
    }
    
    /**
     * Sets the task list with tasks and answers
     */
    public void setTasks() {
    	// number of tasks
    	int numTasks= 10;
    	// number of columns to hold task and answer
    	int taskAndAnswer = 2;
    	
    	// create a 2d string array with rows= numTasks and columns= taskAndAnswer
    	String[][] tasksList = new String[numTasks][taskAndAnswer];
    	
    	// set tasks and answers
    	tasksList[0][0]= "All answers are 1 word.\nType numbers as words.\nWhat is 2 + 2?";
    	tasksList[0][1]= "four";
    	
    	tasksList[1][0]= "All answers are 1 word.\nType numbers as words.\nWhat is the name of the rock in the sky?";
    	tasksList[1][1]= "moon";
    	
    	tasksList[2][0]= "All answers are 1 word.\nType numbers as words.\nWhat is the color of grass?";
    	tasksList[2][1]= "green";
    	
    	tasksList[3][0]= "All answers are 1 word.\nType numbers as words.\nWhat is 3 times 3?";
    	tasksList[3][1]= "nine";
    	
    	tasksList[4][0]= "All answers are 1 word.\nType numbers as words.\nWho made the song 'We Are the Champions'?";
    	tasksList[4][1]= "queen";
    	
    	tasksList[5][0]= "All answers are 1 word.\nType numbers as words.\nWho is the grey wizard in 'Lord of the Rings'?";
    	tasksList[5][1]= "gandalf";
    	
    	tasksList[6][0]= "All answers are 1 word.\nType numbers as words.\nHow many sides does a square have?";
    	tasksList[6][1]= "four";
    	
    	tasksList[7][0]= "All answers are 1 word.\nType numbers as words.\nWho is the best captain of the starship Enterprise";
    	tasksList[7][1]= "picard";
    	
    	tasksList[8][0]= "All answers are 1 word.\nType numbers as words.\nWhat is the name of the star the Earth orbits?";
    	tasksList[8][1]= "Sun";
    	
    	tasksList[9][0]= "All answers are 1 word.\nType numbers as words.\nSay cheese.";
    	tasksList[9][1]= "cheese";
    	
    	// set class variable tasks with tasklist
    	tasks = tasksList;
    }
    

    /**
     * Writing a new entry to our log
     * @param name - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect) 
     * @return String of the new hidden image
     */
    public static void writeToLog(String name, Message message){
        try {
            // read old log file 
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " +  name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"

            for (String log: logsObj.getLogList()){
                System.out.println(log);
            }

            // write to log file
            logsObj.writeTo(output);
        }catch(Exception e){
            System.out.println("Issue while trying to save");
            e.printStackTrace();
        }
    }
    
    /**
     * Reading the current log file
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public static Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }
    
    /**
     * Update the leader board
     * @param name [client's name]
     * @param loginWin ["win" if updating wins, if not "win" update logins]
     */
    public void writeToLeaderBoard(String name, String loginWin){
        try {
        	// boolean to track if name is on leaderboard already
        	boolean found = false;
        	
        	 // Creating response to read in the leader board
            Response.Builder in;

            // creating a response to write to the leader board file
            Response.Builder out = Response.newBuilder()
                    .setResponseType(Response.ResponseType.LEADER);
            
            // read old log file 
            in = readLeaderFile();
            Response readIn = in.build();
            
            // iterate through the leader board
            for (Entry lead: readIn.getLeaderList()){
            	// if current client is found on leader board
            	if(lead.getName().compareToIgnoreCase(name)==0) {
            		// set boolean found to true
            		found = true;
            		
            		// if this is a win update
            		if(loginWin.equals("win")) {
            			// calculate new wins
            			wins = lead.getWins() + 1;
            			// update how many tiles are revealed
            			setTileReveal(wins);
            			
            			// create a new leader board entry
            			Entry leader = Entry.newBuilder()
            					// set name
                                .setName(name)
                                // set updated wins
                                .setWins(wins)
                                // set logins
                                .setLogins(lead.getLogins())
                                .build();
            			
            				// add entry to output response
                            out.addLeader(leader);
                            
                    // if this is a login update
            		}else {
            			// set class variable wins to number of wins in leader board
            			wins = lead.getWins();
            			// set tile reveal
            			setTileReveal(wins);
            			
            			// create a new leader entry
            			Entry leader = Entry.newBuilder()
            					// set name
                                .setName(name)
                                // set wins
                                .setWins(wins)
                                // update logins
                                .setLogins(lead.getLogins()+1)
                                .build();
            			
            				// add entry to output response
                            out.addLeader(leader);
            		}
            		
            	// if entry is not current client
            	}else {
            		// create a new leader entry
            		Entry leader = Entry.newBuilder()
            				// copy name
                            .setName(lead.getName())
                            // copy wins
                            .setWins(lead.getWins())
                            // copy logins
                            .setLogins(lead.getLogins())
                            .build();
            		
            			// add entry to output response
                        out.addLeader(leader);
            	}
            }
            
            // if client is not on the leader board
            if(!found) {
            	// create a new leader entry
                Entry leader = Entry.newBuilder()
                	// set client name
                    .setName(name)
                    // set wins to 0
                    .setWins(0)
                    // set logins to 1
                    .setLogins(1)
                    .build();
                
                // add entry to output response
                out.addLeader(leader);
                
                // set tile reveal
                setTileReveal(wins);
            }
            
            // build response into new response
            Response writeOut = out.build();

            // print out updated leader board with login info on server
            for (Entry lead: writeOut.getLeaderList()){
                System.out.println(lead.getName() + ": wins: " + lead.getWins() + ", logins: " + lead.getLogins());
            }
        	
            // open log file
            FileOutputStream output = new FileOutputStream(leaderBoardFilename);
           
            // write to log file
            writeOut.writeTo(output);
            
        // catch Exception in writing to file
        }catch(Exception e){
            System.out.println("Issue while trying to save");
        }
    }
    
    /**
     * Read in leader board into a Response builder
     * @return [response builder]
     * @throws Exception
     */
    public static Response.Builder readLeaderFile() throws Exception{
    	// make a new response builder
        Response.Builder leader = Response.newBuilder()
        		// of type leader
            .setResponseType(Response.ResponseType.LEADER);
        
        try {
            // just read the file and put what is in it into the logs object
            return leader.mergeFrom(new FileInputStream(leaderBoardFilename));
            
        // catch file not foune exception and create a new file
        } catch (FileNotFoundException e) {
            System.out.println(leaderBoardFilename + ": File not found.  Creating a new file.");
            return leader;
        }
    }


    /**
     * Sends the leader board to the client
     * @param out[output stream]
     * @param in[input stream]
     */
    public void sendLeaderBoard(OutputStream out,InputStream in) {
    	 // Creating response builder
        Response.Builder readIn;
        
        // read leader board file into response
        try {
        readIn = readLeaderFile();
        Response response = readIn.build();
        
        // send response
        try {
            response.writeDelimitedTo(out);
            
            // catch IOException
            }catch (IOException e) {
            	System.out.println("Could not send leader board to: " + name);
            }
        
        // catch Exception from reading leaderboard
        }catch(Exception e) {
        	System.out.println("Could not read in leaderBoard.txt");
        	e.printStackTrace();
        }
        
        try {
        // return to the main menu
    	mainMenu(out, in);
    	
    	// catch IOEexception
        }catch (IOException e) {
        	System.out.println(name + " disconnected");
        }
    }
    
    /**
     * Sets the class variable tileReveal which determines how many tiles
     * will be revealed for a correct answer
     * minimum of 8
     * maximum of 35
     * add 8 to the minimum for each win the client has
     * @param wins[number of wins the client has]
     */
    public void setTileReveal(int wins) {
    	int maximum = 35;
    	int winBuff = 7;
    	if(wins < 3) {
    		tileReveal = (wins + 1) * winBuff;
    	}else {
    		tileReveal = maximum;
    	}
    	
    }
    

    /**
     * main method and entry point for the server
     * @param args[<port(int)> <delay(int)>]
     */
    public static void main (String args[]) {
    	// create a new game object
        Game game = new Game();
        // create a new mutex
        Semaphore mutex = new Semaphore(1);

        // check for the expected arguments e.g. gradle runServer -Pport=5555
        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }
        
        // default port
        int port = 9099; 
        // default delay
        int sleepDelay = 10000; 
        // create socket for client
        Socket clientSocket = null;
        // create socket for server
        ServerSocket serv = null;

        // set port and sleep delay from args
        try {
            port = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
            
        // catch number format exception if port is not an integer
        } catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }
        
        // connect to new client
        try {
            serv = new ServerSocket(port);
            while(true) {
            	clientSocket = serv.accept();
                SockBaseServer server = new SockBaseServer(clientSocket, game, mutex);
                server.start();
            }
            
        // catch exception while connecting to client
        } catch(Exception e) {
        	System.out.println("Error in main while connecting to clients");
            e.printStackTrace();
            System.exit(2);
            
        // close client socket
        } finally {
        	if(clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException e) {
					System.out.println("Could not close socket");
					e.printStackTrace();
				}
        	}
        }
    }
}

