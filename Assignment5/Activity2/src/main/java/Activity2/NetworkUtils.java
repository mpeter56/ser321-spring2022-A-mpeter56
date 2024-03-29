package Activity2;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import org.json.JSONObject;
import org.json.JSONTokener;

import Activity2.NetworkUtils;

public class NetworkUtils {
  /**
   * Performs a request on a remote node and waits for a reply which it rebuilds into a message
   * 
   * @param message to send to remote node
   * @return the reply message it read back
   */
  public static JSONObject send(String host, int port, JSONObject message) {
    Socket socket = null;
    try {
      // open socket
      socket = new Socket(host, port);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      
      // write message
      out.println(message.toString());
      // expect message in reply
      String line = in.readLine();
      JSONTokener tokener = new JSONTokener(line);
      JSONObject root = new JSONObject(tokener);
      
      // cleanup
      in.close();
      out.close();
      socket.close();
      
      // give back reply
      return root;
    } catch (SocketException | EOFException e) {
      // client disconnect
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (socket != null)
        try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
    return null;
  }
  
  public static JSONObject read(Socket conn) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String line = in.readLine();
    JSONTokener tokener = new JSONTokener(line);
    JSONObject root = new JSONObject(tokener);
    return root;
  }
  
  
  public static void respond(Socket conn, JSONObject message) throws IOException {
    PrintWriter out = new PrintWriter(conn.getOutputStream(), true);
    out.println(message.toString());
  }
  
  // https://mkyong.com/java/java-convert-byte-to-int-and-vice-versa/
  public static byte[] intToBytes(final int data) {
      return new byte[] { (byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff), 
          (byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff), };
  }

  // https://mkyong.com/java/java-convert-byte-to-int-and-vice-versa/
  public static int bytesToInt(byte[] bytes) {
      return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8)
              | ((bytes[3] & 0xFF) << 0);
  }
  
  /**
   * used for client - leader communication
   * send the bytes on the stream.
   */
  public static void sendClient(OutputStream out, byte... bytes) throws IOException {
      out.write(intToBytes(bytes.length));
      out.write(bytes);
      out.flush();
  }

  // used for client - leader communication
  // read the bytes on the stream
  private static byte[] readClient(InputStream in, int length) throws IOException {
      byte[] bytes = new byte[length];
      int bytesRead = 0;
      try {
          bytesRead = in.read(bytes, 0, length);
      } catch (IOException e1) {
          System.out.println("Client disconnected");
      }

      if (bytesRead != length) {
          return null;
      }

      return bytes;
  }

  // Used for client - leader communication
  // first 4 bytes we read give us the length of the message we are about to
  // receive
  // next we call read again with the length of the actual bytes in the data we
  // are interested in
  /** 
   * Receive the bytes on the stream.
   */
  public static byte[] receiveClient(InputStream in) throws IOException {
      byte[] lengthBytes = readClient(in, 4);
      if (lengthBytes == null) {
          return new byte[0];
      }
      int length = NetworkUtils.bytesToInt(lengthBytes);
      byte[] message = readClient(in, length);
      if (message == null) {
          return new byte[0];
      }
      return message;
  }
  
}
