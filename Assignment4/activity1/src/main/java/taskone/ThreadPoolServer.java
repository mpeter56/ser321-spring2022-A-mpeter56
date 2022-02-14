package taskone;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class ThreadPoolServer extends Thread {
	 private Socket conn;
	 private int id;
	 private static StringList strings;
	 private Lock mutex;
	 private static int numConnections;
	 
	 public ThreadPoolServer(Socket sock, int id) {
		    this.conn = sock;
		    this.id = id;
		    mutex = new ReentrantLock();
		  }
	  
	 public void run() {
		   try {
		      // setup read/write channels for connection
		      
		      Performer performer = new Performer(this.conn, strings, mutex, id);
	          performer.doPerform();
		   } catch (Exception e) {
			      e.printStackTrace();
			   	  System.out.println("Client Disconnected");
			   	  numConnections--;
		   }
	 }
	 
	 public static void main(String args[]) throws IOException {
		 	strings = new StringList();
		    Socket sock = null;
		    int id = 0;
		    int numberWorkers = 2;
		    numConnections = 0;
		    
		    try {
		      if (args.length != 1) {
		        System.out.println("Usage: gradle ThreadedServer -Pport=9099 -q --console=plain");
		        System.exit(0);
		      }
		      int portNo = Integer.parseInt(args[0]);
		      if (portNo <= 1024)
		        portNo = 8888;
		      ServerSocket serv = new ServerSocket(portNo);
		      while (true) {
		    	  if(numConnections < numberWorkers) {
			        System.out.println("Threaded server waiting for connects on port " + portNo);
			        sock = serv.accept();
			        System.out.println("Threaded server connected to client-" + id);
			        // create thread
			        ThreadedServer myServerThread = new ThreadedServer(sock, id++);
			        // run thread and don't care about managing it
			        myServerThread.start();
			        numConnections++;
		    	  }
		      }
		    } catch (Exception e) {
		      e.printStackTrace();
		    } finally {
		      if (sock != null) sock.close();
		    }
		  }
}
