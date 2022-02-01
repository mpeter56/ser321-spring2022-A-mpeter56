package Assignment3Starter;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.json.*;

import Assignment3Starter.PicturePanel.InvalidCoordinateException;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 * 
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 *     -> modal means that it opens the GUI and suspends background processes. Processing 
 *        still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * 
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 * 
 * This ClientGui has been adapted to be the GUI for a server based quote guessing game
 * 
 * @author Maura Peterson, Ser321 team
 * 
 * @version 2.0
 * 
 * Completion time: 5 hours
 */
public class ClientGui implements Assignment3Starter.OutputPanel.EventHandlers {
	//Global variables
	// frame for GUI
	JDialog frame;
	// picture panel for GUI
	PicturePanel picturePanel;
	// Output text panel for GUI
	OutputPanel outputPanel;
	// socket to connect to server
	Socket sock;
	// output stream
	OutputStream out;
	// input stream
	InputStream in;
	// to count the number of times submit is clicked
	// used to tell if it is the first submit
	int clickCount;
	// IP address
	static String address;
	// port
	static int port;
  
	/**
	 * Constructor for the client GUI
	 */
	public ClientGui() {
		frame = new JDialog();
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(800, 800));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// setup the top picture frame
		picturePanel = new PicturePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.25;
		frame.add(picturePanel, c);

		// setup the input, button, and output area
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.75;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		outputPanel = new OutputPanel();
		outputPanel.addEventHandlers(this);
		frame.add(outputPanel, c);
		
		// set the clickCount to 0
		clickCount = 0;
	}
	
  /**
   * Shows the current state in the GUI
   * @param makeModal - true to make a modal window, false disables modal behavior
   */
  public void show(boolean makeModal) {
    frame.pack();
    frame.setModal(makeModal);
    frame.setVisible(true);
  }

  /**
   * Creates a new game and set the size of the grid 
   * @param dimension - the size of the grid will be dimension x dimension
   */
  public void newGame(int dimension) {
    picturePanel.newGame(dimension);
    outputPanel.appendOutput("Started new game with a " + dimension + "x" + dimension + " board.");
    int receiveCount = 0;
    
    try{
		sock = new Socket(address, port);
		out = sock.getOutputStream();
        in = sock.getInputStream();
        while(receiveCount < 2) {
	        byte[] responseBytes = NetworkUtils.Receive(in);
	        JSONObject response = JsonUtils.fromByteArray(responseBytes);
	        if (response.has("error")) {
	          System.out.println(response.getString("error"));
	        } else {
	        	switch (response.getInt("datatype")) {
	        		case (1):
	        			outputPanel.appendOutput(response.getString("data"));
	        			receiveCount++;
	        			break;
	        		case (2): 
	        			System.out.println("Your Hello");
	        			Base64.Decoder decoder = Base64.getDecoder();
	        			byte[] bytes = decoder.decode(response.getString("data"));
	        			try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
	        				BufferedImage image = ImageIO.read(bais);
	        				picturePanel.insertImage(image,0,0);
	        			}catch (InvalidCoordinateException ex) {
	        				ex.printStackTrace();
	        				System.out.println("invalid coordinate");
	        			}
	        		receiveCount++;
	        		break;
	        	}
	        }
        }
    }catch(IOException e){
		e.printStackTrace();
		System.out.println("IOException");
	}
  }
  
  

  

  /**
   * Insert an image into the grid at position (col, row)
   * 
   * @param filename - filename relative to the root directory
   * @param row - the row to insert into
   * @param col - the column to insert into
   * @return true if successful, false if an invalid coordinate was provided
   * @throws IOException An error occured with your image file
   */
  public boolean insertImage(String filename, int row, int col) throws IOException {
    String error = "";
    try {
      // insert the image
      if (picturePanel.insertImage(filename, row, col)) {
      // put status in output
        outputPanel.appendOutput("Inserting " + filename + " in position (" + row + ", " + col + ")");
        return true;
      }
      error = "File(\"" + filename + "\") not found.";
    } catch(PicturePanel.InvalidCoordinateException e) {
      // put error in output
      error = e.toString();
    }
    outputPanel.appendOutput(error);
    return false;
  }

  /**
   * Submit button handling
   * 
   * Sends input to server when submit button is clicked
   */
  @Override
  public void submitClicked() {
	  clickCount++;
	  int receiveCount = 0;
	  // Pulls the input box text
	  String input = outputPanel.getInputText();
	  // if has input
	  if (input.length() > 0) {
		  // append input to the output panel
		  outputPanel.appendOutput(input);
		  // clear input text box
		  outputPanel.setInputText("");
      
		  // make request JSONObject to send to server
		  JSONObject request = new JSONObject();
      
		  if(input.equalsIgnoreCase("quit")) {
			  
			  try {
				out.close();
				in.close();
				sock.close();
			} catch (IOException e) {
				System.out.println("Error in dissconecting from server");
				e.printStackTrace();
			}
			  System.exit(0);
		  }
		  //check if it is first submit and if yes send name
		  if(clickCount == 1) {
			  request.put("datatype", 1);
			  request.put("name", input);
		  //check if play game was selected and set game to 1
		  }else if(clickCount > 1 && input.equals("2")) {
			  request.put("datatype", 1);
			  request.put("selected", input);
		  }else {
			  request.put("datatype", 1);
			  request.put("selected", input);
		  }
		  
		  //send request to server and get response
		  if (request != null) {
			  try {
				  //send request to server
				  NetworkUtils.Send(out, JsonUtils.toByteArray(request));
				  
				  while(receiveCount < 1) {
				  //get response
				  byte[] responseBytes = NetworkUtils.Receive(in);
				  JSONObject response = JsonUtils.fromByteArray(responseBytes);
				  
				  //check for error
				  if (response.has("error")) {
					  System.out.println(response.getString("error"));
				  } else {
					  //determine if text or image
					  switch (response.getInt("datatype")) {
					  //if text
					  	case (1):
					  		//read and output text to outputPanel
						  outputPanel.appendOutput(response.getString("data"));
					  	  System.out.println(response.getString("data"));
					  	  receiveCount++;
					  	  break;
					  //if image
					  	case (2):
						  //read image and output image to picturePanel
						  System.out.println("Your image");
						  Base64.Decoder decoder = Base64.getDecoder();
						  byte[] bytes = decoder.decode(response.getString("data"));
						  try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
							  BufferedImage image = ImageIO.read(bais);
							  picturePanel.insertImage(image,0,0);
						  }catch (InvalidCoordinateException ex) {
							  ex.printStackTrace();
							  System.out.println("invalid coordinate");
						  }
						  receiveCount++;
						  break;
						//if number
					  	case(3):
					  		outputPanel.setPoints(response.getInt("data"));
					  }
				  }
				  }
			  }catch(IOException e){
    			e.printStackTrace();
    			System.out.println("IOException in submitClicked()");
			  }
		  
		  }
	  }
  }
  
  /**
   * Key listener for the input text box
   * 
   * Change the behavior to whatever you need
   */
  @Override
  public void inputUpdated(String input) {
    if (input.equals("surprise")) {
      outputPanel.appendOutput("You found me!");
    }
  }

  /**
   * main method
   * @param args [command line input]
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
	  try {
			port = Integer.parseInt(args[0]);
			
		}catch (Exception e) {
	        System.out.println("Argument: " + args[0] + " must be an integer.");
	        System.exit(1);
	    }
		
		try {
			address = args[1];
		}catch (Exception e) {
	        System.out.println("Argument: " + args[1] + " must be an IP address.");
	        System.exit(1);
	    }
	  
	// create the frame
	ClientGui main = new ClientGui();
		
	// setup the UI to display on image
	main.newGame(1);


	// show the GUI dialog as modal
	main.show(true); 
	  
  }
}
