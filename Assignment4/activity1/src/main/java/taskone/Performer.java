/**
  File: Performer.java
  Author: Student in Fall 2020B
  Description: Performer class in package taskone.
*/

package taskone;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import java.util.concurrent.locks.*;

/**
 * Class: Performer 
 * Description: Threaded Performer for server tasks.
 */
class Performer {

    private StringList state;
    private Socket conn;
    protected Lock mutex;
    private int id;

    public Performer(Socket sock, StringList strings, Lock mutex1, int id1) {
        this.conn = sock;
        this.state = strings;
        this.mutex = mutex1;
        this.id = id1;
    }
    
    public Performer(StringList strings, Lock mutex1, int id1) {
        this.state = strings;
        this.mutex = mutex1;
        this.id = id1;
    }
    
    public Performer(Socket sock, StringList strings, Lock mutex1) {
        this.conn = sock;
        this.state = strings;
        this.mutex = mutex1;
    }

    public JSONObject add(String str) {
        JSONObject json = new JSONObject();
        json.put("datatype", 1);
        json.put("type", "add");
        mutex.lock();
        try {
        	System.out.println("Client " + id +" In lock");
        	state.add(str);
        	json.put("data", state.toString());
        }catch (Exception e) {
        	json.put("data", "Could not add");
        }finally {
        	System.out.println("Client "+ id +" realeasing lock");
        	mutex.unlock();
        }
        
        
        return json;
    }
    
    public JSONObject pop() {
    	JSONObject json = new JSONObject();
    	String popped;
    	json.put("datatype", 1);
    	json.put("type", "pop");
    	mutex.lock();
    	try {
    		System.out.println("Client " + id +" In lock");
    		popped = state.pop();
    		json.put("data", popped);
    	}catch (Exception e) {
    		json.put("data", "Could not pop");
    	}finally {
    		System.out.println("Client "+ id +" realeasing lock");
    		mutex.unlock();
    	}
    	return json;
    }
    
    public JSONObject display() {
    	JSONObject json = new JSONObject();
    	json.put("datatype", 1);
    	json.put("type", "display");
    	json.put("data", state.toString());
    	return json;
    }
    
    public JSONObject count() {
    	JSONObject json = new JSONObject();
    	json.put("datatype", 1);
    	json.put("type", "count");
    	json.put("data", Integer.toString(state.size()));
    	return json;
    }
    
    public JSONObject Switch(int idx1, int idx2) {
    	JSONObject json = new JSONObject();
    	json.put("datatype", 1);
    	json.put("type", "count");
    	mutex.lock();
    	try {
    		System.out.println("Client " + id +" In lock");
    		json.put("data", state.Switch(idx1, idx2));
    	}catch (Exception e) {
    		json.put("data", "Could not switch");
    	} finally {
    		System.out.println("Client "+ id +" realeasing lock");
    		mutex.unlock();
    	}
    	
    	return json;
    }
    
    public JSONObject quit() {
    	JSONObject json = new JSONObject();
    	json.put("datatype", 1);
    	json.put("type", "quit");
    	json.put("data", "goodbye");
    	return json;
    }

    public static JSONObject error(String err) {
        JSONObject json = new JSONObject();
        json.put("error", err);
        return json;
    }
    

    public void doPerform() {
        boolean quit = false;
        boolean quitAfter = false;
        OutputStream out = null;
        InputStream in = null;
        try {
            out = conn.getOutputStream();
            in = conn.getInputStream();
            System.out.println("Server connected to client:");
            while (!quit) {
                byte[] messageBytes = NetworkUtils.receive(in);
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                JSONObject returnMessage = new JSONObject();
   
                int choice = message.getInt("selected");
                    switch (choice) {
                        case (1):
                            String inStr = (String) message.get("data");
                            returnMessage = add(inStr);
                            break;
                        case (2):
                            returnMessage = pop();
                            break;
                        case (3):
                            returnMessage = display();
                            break;
                        case (4):
                            returnMessage = count();
                            break;
                        case (5):
                            String indexes = (String) message.get("data");
                        	String[] idxStrings = indexes.split(" ");
                        	int idx1 = Integer.parseInt(idxStrings[0]);
                        	int idx2 = Integer.parseInt(idxStrings[1]);
                            returnMessage = Switch(idx1,idx2);
                            break;
                        case (0):
                        	returnMessage = quit();
                        	quitAfter = true;
                        	break;
                        default:
                            returnMessage = error("Invalid selection: " + choice 
                                    + " is not an option");
                            break;
                    }
                // we are converting the JSON object we have to a byte[]
                byte[] output = JsonUtils.toByteArray(returnMessage);
                NetworkUtils.send(out, output);
                
                if(quitAfter) {
                	quit = true;
                }
            }
            // close the resource
            System.out.println("close the resources of client ");
            out.close();
            in.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
