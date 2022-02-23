package Activity2;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.json.JSONObject;

public abstract class Node implements Runnable {
  private int _port;

  // abstract constructor
  public Node(int port) {
    _port = port;
  }

  // abstract methods
  public abstract JSONObject verify(JSONObject object);
  
  public abstract JSONObject canPayback(JSONObject object);

  public abstract JSONObject canCredit(JSONObject object);

  public abstract JSONObject credit(JSONObject object);
  
  public abstract JSONObject payback(JSONObject object);

  public abstract JSONObject error(String error);

  /**
   * Connects to leader, gets requests from leader and sends responses to leader
   */
  @Override
  public void run() {
    // separated so the finally can clean up the connection
    ServerSocket socket = null;
    try {
      // create the listening socket
      socket = new ServerSocket(_port);
      while (true) { // handle connections indefinitely
        Socket conn = null;
        try {
          // listen for connection
          conn = socket.accept();

          // read in a message
          JSONObject root = NetworkUtils.read(conn);

          // preset response to error
          JSONObject ret = error("");
          // which method leader is calling
          if (root.has("method")) {
            switch (root.getString("method")) {
            case ("canPayback"):
              ret = canPayback(root);
              break;
            case ("canCredit"):
              ret = canCredit(root);
              break;
            case ("payback"):
              ret = payback(root);
              break;
            case ("credit"):
                ret = credit(root);
                break;
            case ("verify"):
            	ret = verify(root);
            	break;
            }
          }

          // send response
          NetworkUtils.respond(conn, ret);

          // cleanup
          conn.close();
        } catch (SocketException | EOFException e) {
          // expected on timeout
        } catch (IOException ex) {
          ex.printStackTrace();
        } finally {
          // cleanup, just in case
          if (conn != null)
            try {
              conn.close();
            } catch (IOException ex) {
              ex.printStackTrace();
            }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      // cleanup, just in case
      if (socket != null)
        try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
  }
}
