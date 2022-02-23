package Activity2;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * This class connects with clients and starts their threads that run Performer
 * @author Maura
 * @version 1
 *
 */
public class Leader extends Thread {
  
  // socket to client
  private Socket conn;
  // mutex 
  private Lock mutex;
  // port to node 1
  private int port1;
  // port to node 2
  private int port2;
  
  /**
   * Constructor for Leader
   * @param sock
   */
  public Leader(Socket sock) {
	  // set connection to client
	  this.conn = sock;
	  // initialize mutex
	  mutex = new ReentrantLock();
	  // Node1 is always at port 8001
	  port1 = 8001;
	  // Node2 is always at port 8002
	  port2 = 8002;
  }
  
  /**
   * Starts performer thread for each client
   */
  public void run() {
	
	// create new performer thread
	Performer performer = new Performer(this.conn, mutex, port1, port2);
	// start performer
    performer.doPerform();
  }

  /**
   * Entry point for Leader
   * @param args
   */
  public static void main(String[] args) {
	    // Create a socket
	    Socket sock = null;
	    try {
	      // port is always 8000
	      int portNo = 8000;
	      // create server socket at 8000
	      ServerSocket serv = new ServerSocket(portNo);
	      while (true) {
	        System.out.println("Leader waiting for connects on port " + portNo);
	        // wait for next client
	        sock = serv.accept();
	        System.out.println("Leader connected to client-");
	        
	        // create thread
	        Leader myServerThread = new Leader(sock);
	        // run thread and don't care about managing it
	        myServerThread.start();
	      }
	    } catch (Exception e) {
	      System.out.println("Exception in main");
	      e.printStackTrace();
	    } finally {
	    	try {
	    		// if socket is not null close socket
	    		if (sock != null) sock.close();
	    	}catch (IOException ex) {
	    		System.out.println("Could not close socket");
	    	}
	    }
	  }  
  }

