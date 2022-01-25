/*
Simple Web Server in Java which allows you to call 
localhost:9000/ and show you the root.html webpage from the www/root.html folder
You can also do some other simple GET requests:
1) /random shows you a random picture (well random from the set defined)
2) json shows you the response as JSON for /random instead the html page
3) /file/filename shows you the raw file (not as HTML)
4) /multiply?num1=3&num2=4 multiplies the two inputs and responses with the result
5) /github?query=users/amehlhase316/repos (or other GitHub repo owners) will lead to receiving
   JSON which will for now only be printed in the console. See the todo below

The reading of the request is done "manually", meaning no library that helps making things a 
little easier is used. This is done so you see exactly how to pars the request and 
write a response back
*/

package funHttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.nio.charset.Charset;

class WebServer {
  public static void main(String args[]) {
    WebServer server = new WebServer(9000);
  }

  /**
   * Main thread
   * @param port to listen on
   */
  public WebServer(int port) {
    ServerSocket server = null;
    Socket sock = null;
    InputStream in = null;
    OutputStream out = null;

    try {
      server = new ServerSocket(port);
      while (true) {
        sock = server.accept();
        out = sock.getOutputStream();
        in = sock.getInputStream();
        byte[] response = createResponse(in);
        out.write(response);
        out.flush();
        in.close();
        out.close();
        sock.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (sock != null) {
        try {
          server.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Used in the "/random" endpoint
   */
  private final static HashMap<String, String> _images = new HashMap<>() {
    {
      put("streets", "https://iili.io/JV1pSV.jpg");
      put("bread", "https://iili.io/Jj9MWG.jpg");
    }
  };


  private Random random = new Random();

  /**
   * Reads in socket stream and generates a response
   * @param inStream HTTP input stream from socket
   * @return the byte encoded HTTP response
   */
  public byte[] createResponse(InputStream inStream) {

    byte[] response = null;
    BufferedReader in = null;

    try {

      // Read from socket's input stream. Must use an
      // InputStreamReader to bridge from streams to a reader
      in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

      // Get header and save the request from the GET line:
      // example GET format: GET /index.html HTTP/1.1

      String request = null;

      boolean done = false;
      while (!done) {
        String line = in.readLine();

        System.out.println("Received: " + line);

        // find end of header("\n\n")
        if (line == null || line.equals(""))
          done = true;
        // parse GET format ("GET <path> HTTP/1.1")
        else if (line.startsWith("GET")) {
          int firstSpace = line.indexOf(" ");
          int secondSpace = line.indexOf(" ", firstSpace + 1);

          // extract the request, basically everything after the GET up to HTTP/1.1
          request = line.substring(firstSpace + 2, secondSpace);
        }

      }
      System.out.println("FINISHED PARSING HEADER\n");

      // Generate an appropriate response to the user
      if (request == null) {
        response = "<html>Illegal request: no GET</html>".getBytes();
      } else {
        // create output buffer
        StringBuilder builder = new StringBuilder();
        // NOTE: output from buffer is at the end

        if (request.length() == 0) {
          // shows the default directory page

          // opens the root.html file
          String page = new String(readFileInBytes(new File("www/root.html")));
          // performs a template replacement in the page
          page = page.replace("${links}", buildFileList());

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(page);

        } else if (request.equalsIgnoreCase("json")) {
          // shows the JSON of a random image and sets the header name for that image

          // pick a index from the map
          int index = random.nextInt(_images.size());

          // pull out the information
          String header = (String) _images.keySet().toArray()[index];
          String url = _images.get(header);

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: application/json; charset=utf-8\n");
          builder.append("\n");
          builder.append("{");
          builder.append("\"header\":\"").append(header).append("\",");
          builder.append("\"image\":\"").append(url).append("\"");
          builder.append("}");

        } else if (request.equalsIgnoreCase("random")) {
          // opens the random image page

          // open the index.html
          File file = new File("www/index.html");

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(new String(readFileInBytes(file)));

        } else if (request.contains("file/")) {
          // tries to find the specified file and shows it or shows an error

          // take the path and clean it. try to open the file
          File file = new File(request.replace("file/", ""));

          // Generate response
          if (file.exists()) { // success
            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append(new String(readFileInBytes(file)));
          } else { // failure
            builder.append("HTTP/1.1 404 Not Found\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("File not found: " + file);
          }
        } else if (request.contains("multiply?")) {
          // This multiplies two numbers, there is NO error handling, so when
          // wrong data is given this just crashes

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();

	  try{
          // extract path parameters
          query_pairs = splitQuery(request.replace("multiply?", ""));

	 	 try{

         	 // extract required fields from parameters
       		 Integer num1 = Integer.parseInt(query_pairs.get("num1"));
          	Integer num2 = Integer.parseInt(query_pairs.get("num2"));

          	// do math
          	Integer result = num1 * num2;

          	// Generate response
          	builder.append("HTTP/1.1 200 OK\n");
          	builder.append("Content-Type: text/html; charset=utf-8\n");
          	builder.append("\n");
          	builder.append("Result is: " + result);
		}catch(NumberFormatException ex){
			System.err.println("Invalid string in argumment");
		
			builder.append("HTTP/1.1 400 Bad Request\n");
			builder.append("ContentType: text/html; charset=utf-8\n");
			builder.append("\n");
			builder.append("Error 400: Bad Request....");
			builder.append("Please use format /multiply?num1=#&num2=#\n");
			builder.append("E.g. /multiply?num1=3&num2=4");
		
		}
	}catch(StringIndexOutOfBoundsException e){
		System.err.println("No arguments: StringIndexOutOfBoundsException");
		builder.append("HTTP/1.1 400 Bad Request\n");
		builder.append("ContentType: text/html; charset=utf-8\n");
		builder.append("\n");
		builder.append("Error 400 Bad Request...");
		builder.append("Please use format /multiply?num1=#&num2=#\n");
		builder.append("E.g. /multiply?num1=3&num2=4");
	}	
          // TODO: Include error handling here with a correct error code and
          // a response that makes sense

        } else if (request.contains("github?")) {
          // pulls the query from the request and runs it with GitHub's REST API
          // check out https://docs.github.com/rest/reference/
          //
          // HINT: REST is organized by nesting topics. Figure out the biggest one first,
          //     then drill down to what you care about
          // "Owner's repo is named RepoName. Example: find RepoName's contributors" translates to
          //     "/repos/OWNERNAME/REPONAME/contributors"
	

	  try{

          	Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	        query_pairs = splitQuery(request.replace("github?", ""));
        	String json = fetchURL("https://api.github.com/" + query_pairs.get("query"));
          	System.out.println(json);

		if(json.length() == 0){
			builder.append("HTTP/1.1 404 Not Found\n");
			builder.append("ContentType: text/html; charset=utf-8\n");
			builder.append("\n");
			builder.append("Error 404 Not Found...");
			builder.append("Please use format: github?query=users/<username>/repos and check username is valid.");
		}else{

	  		int entrees = 3;
	  		int notfirst =0; 
	  		String delims= "[\",]+";
	  		String[] repos = json.split("default_branch");
	  		String[] list= new String[entrees*(repos.length-1)];
	  		int repo = 0;
	  	
			if(list.length == 0 && json.length() != 0){
				if(json.length() == 2){

				builder.append("HTTP/1.1 200 OK\n");
				builder.append("ContentType: text/html; charset=utf-8\n");
				builder.append("\n");
				builder.append("This user does not have any Repos.");
				}else{
					builder.append("HTTP/1.1 400 Bad Request\n");
					builder.append("ContentType: text/html; charset=utf-8\n");
					builder.append("\n");
					builder.append("Error 400 Bad Request");
					builder.append("Please use format: github?query=users/<username>/repos");
				}
			}else{
		
				for(int i = 0; i < list.length; i++){
		  			String[] jsonArray = repos[repo].split(delims);
		  			list[i]=jsonArray[18+notfirst];
		  			i++;
		  			list[i]=jsonArray[20+notfirst].substring(1);
		  			i++;
		  			list[i]=jsonArray[8+notfirst];
		  			notfirst=4;
		  			repo++;
	  			} 

				builder.append("HTTP/1.1 200 OK\n");
				builder.append("ContentType: text/html; charset=utf-8\n");
				builder.append("\n");
	  			for(int i = 0; i < list.length; i=i+entrees){
					builder.append(list[i] + ", " + list[i+1] + " -> " + list[i+2] + "\n");
	  			}
			}
		}
	  }catch(StringIndexOutOfBoundsException e){
		  System.err.println("No Arguments: StringIndexOutOfBoundsException");
		  builder.append("HTTP/1.1 400 Bad Request\n");
		  builder.append("ContentType: text/html; charset=utf-8\n");
		  builder.append("\n");
		  builder.append("Error 400 Bad Request...");
		  builder.append("Please use format: query=users/<username>/repos");
	  }
		

          // TODO: Parse the JSON returned by your fetch and create an appropriate
          // response
          // and list the owner name, owner id and 
	  // name of the public repo on your webpage, e.g.
          // amehlhase, 46384989 -> memoranda
          // amehlhase, 46384989 -> ser316examples
          // amehlhase, 46384989 -> test316

        }else if(request.contains("color?")){
		try{
			Map<String, String> query_pairs = new LinkedHashMap<String, String>();
			query_pairs = splitQuery(request.replace("color?",""));

			String color1 = query_pairs.get("color1");
			String color2 = query_pairs.get("color2");

			if(color1!=null){
				if(color2!=null){
					builder.append("HTTP/1.1 200 OK\n");
					builder.append("ContentType: text/html; charset-utf-8\n");
					builder.append("\n");
					builder.append("<html>");
					builder.append("<body style=\"background: linear-gradient(to right, " + color1 + " 0%, " + color2 + " 100%);\">...</body>");
					builder.append("</html>");
				}else{
					builder.append("HTTP/1.1 200 OK \n");
					builder.append("ContentType: text/htmml; charset-utf-8\n");
					builder.append("\n");
					builder.append("<body bgcolor= "+ color1 +"> </body>");
				}
			}else{
				builder.append("HTTP/1.1 400 Bad Request\n");
				builder.append("ContentType: text/html; charset-utf-8\n");
				builder.append("\n");
				builder.append("Error 400 Bad Request...");
				builder.append("Please use format /color?color1=<color>&color2=<color>\n");
				builder.append("or /color?color1=<color>");
			}
		}catch(StringIndexOutOfBoundsException e){
			System.err.println("No Arguments: StringIndexOutOfBoundsException");
			builder.append("HTTP1.1 400 Bad Request\n");
			builder.append("ContentType: text/html; charset-utf-8\n");
			builder.append("\n");
			builder.append("<html>");
			builder.append("<body style=\"background: linear-gradient(to right, #66417F 0%, #B2D1FF 100%);\"> secret color... Error 400 Bad Request. Please use format /color?color1=color&color2=color");
		        builder.append(" find colors at https://www.w3schools.com/colors/color_names.asp</body>");
			builder.append("<html>");
		}
	}else if (request.contains("shape?")){
		try{
			Map<String, String> query_pairs = new LinkedHashMap<String, String>();
			query_pairs = splitQuery(request.replace("shape?",""));

			String shape = query_pairs.get("shape");
			String color = query_pairs.get("color");
			String width = query_pairs.get("width");
			String height = query_pairs.get("height");

			if(color == null){
				color = "black";
			}
 			if(width == null){
				width = "100";
			}
			if(height == null){
				height = "200";
			}		
			if(shape != null){
				builder.append("HTTP/1.1 200 OK\n");
				builder.append("ContentType: text/html; charset-utf-8\n");
				builder.append("\n");
				builder.append("<html>");
				builder.append("<body>");
				builder.append("<svg width=\"1600\" height=\"900\">");
				if(shape.equalsIgnoreCase("circle")){
					builder.append("<text x=\"0\" y=\"15\" fill=\"black\">");
					builder.append("Radius: "+ width +" Area: "+ (Integer.parseInt(width) * Integer.parseInt(width) * 3.1416) +"");
			        	builder.append("</text>");	
					builder.append("<circle cx=\"50%\" cy=\"50%\" r=\""+ width +"\" fill=\""+ color +"\"></circle>");
				}else if(shape.equalsIgnoreCase("square")){
					builder.append("<text x=\"0\" y=\"15\" fill=\"black\">");
					builder.append("Width: "+ width +" Area: "+ (Integer.parseInt(width) * Integer.parseInt(width)) +"");
					builder.append("</text>");
					builder.append("<rect width=\""+ width +"\" height=\""+ width +"\" fill=\""+ color +"\"></circle>");
				}else if(shape.equalsIgnoreCase("rectangle")){
					builder.append("<text x=\"0\" y=\"15\" fill=\"black\">");
					builder.append("Width: "+ width +" Height: "+ height +" Area: "+ (Integer.parseInt(width) * Integer.parseInt(height)) +"");
					builder.append("</text>");
					builder.append("<rect x=\"50%\" y=\"50%\" width=\""+ width +"\" height=\""+ height +"\" fill=\""+ color +"\"></rect>");
				}else if(shape.equalsIgnoreCase("ellipse")){
					builder.append("<text x=\"0\" y=\"15\" fill=\"black\">");
					builder.append("Width: "+ width +" Height: "+ height +" Area: "+ (Integer.parseInt(width) * Integer.parseInt(height) * 3.1416) +"");
					builder.append("</text>");
					builder.append("<ellipse cx=\"50%\" cy=\"50%\" rx=\""+ width +"\" ry=\""+ height +"\" fill=\""+ color +"\"></ellipse>");
				}else{
					builder.append("<text x=\"0\" y=\"15\" fills=\"black\">");
					builder.append("Availible shapes are square, circle, ellipse, and rectangle");
					builder.append("</text>");
				}
			}else{
				builder.append("HTTP/1.1 404 Not Found\n");
				builder.append("ContentType: text/html; charset=utf-8\n");
				builder.append("\n");
				builder.append("<html>");
				builder.append("<body>");
				builder.append("<svg width=\"1600\" height=\"900\">");
				builder.append("<text x\"0\" y=\"15\" fill=\"black\">");
				builder.append("Error 404 Not Found...");
				builder.append("Please use format /shape?shape='shape' you can add color, width, and height optionally");
				builder.append("</text>");
				builder.append("<circle cx=\"50%\" cy =\"50%\" rx=\""+ width +"\" fill=\""+ color +"\"></circle>");
			}	
		}catch(StringIndexOutOfBoundsException e){
			builder.append("HTTP/1.1 400 Bad Request\n");
			builder.append("ContentType: text/html; charset=utf-8\n");
			builder.append("\n");
			builder.append("<html>");
			builder.append("<body>");
			builder.append("<svg width=\"1600\" height=\"900\">");
			builder.append("<circle cx =\"50%\" cy=\"50%\" r=\"200\" fill=\"pink\"> </circle>");
			builder.append("<text x =\"0\" y = \"15\" fill=\"black\">Error 400 Bad Request...Please use /shape?shape='shape'&width='number between 1 and 900'&color='color'</text>");
		} catch(NumberFormatException ex){
			System.err.println("Invalid string in argument\n");
			builder.append("Error 400 Bad Request...Please use a smaller number");
		}
		builder.append("</svg>");
		builder.append("</body>");
		builder.append("</html>");

	}else {
          // if the request is not recognized at all

          builder.append("HTTP/1.1 400 Bad Request\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("I am not sure what you want me to do...");
        }

        // Output
        response = builder.toString().getBytes();
      }
    } catch (IOException e) {
      e.printStackTrace();
      response = ("<html>ERROR: " + e.getMessage() + "</html>").getBytes();
    }

    return response;
  }

  /**
   * Method to read in a query and split it up correctly
   * @param query parameters on path
   * @return Map of all parameters and their specific values
   * @throws UnsupportedEncodingException If the URLs aren't encoded with UTF-8
   */
  public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    // "q=hello+world%2Fme&bob=5"
    String[] pairs = query.split("&");
    // ["q=hello+world%2Fme", "bob=5"]
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
          URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
    }
    // {{"q", "hello world/me"}, {"bob","5"}}
    return query_pairs;
  }

  /**
   * Builds an HTML file list from the www directory
   * @return HTML string output of file list
   */
  public static String buildFileList() {
    ArrayList<String> filenames = new ArrayList<>();

    // Creating a File object for directory
    File directoryPath = new File("www/");
    filenames.addAll(Arrays.asList(directoryPath.list()));

    if (filenames.size() > 0) {
      StringBuilder builder = new StringBuilder();
      builder.append("<ul>\n");
      for (var filename : filenames) {
        builder.append("<li>" + filename + "</li>");
      }
      builder.append("</ul>\n");
      return builder.toString();
    } else {
      return "No files in directory";
    }
  }

  /**
   * Read bytes from a file and return them in the byte array. We read in blocks
   * of 512 bytes for efficiency.
   */
  public static byte[] readFileInBytes(File f) throws IOException {

    FileInputStream file = new FileInputStream(f);
    ByteArrayOutputStream data = new ByteArrayOutputStream(file.available());

    byte buffer[] = new byte[512];
    int numRead = file.read(buffer);
    while (numRead > 0) {
      data.write(buffer, 0, numRead);
      numRead = file.read(buffer);
    }
    file.close();

    byte[] result = data.toByteArray();
    data.close();

    return result;
  }

  /**
   *
   * a method to make a web request. Note that this method will block execution
   * for up to 20 seconds while the request is being satisfied. Better to use a
   * non-blocking request.
   * 
   * @param aUrl the String indicating the query url for the OMDb api search
   * @return the String result of the http request.
   *
   **/
  public String fetchURL(String aUrl) {
    StringBuilder sb = new StringBuilder();
    URLConnection conn = null;
    InputStreamReader in = null;
    try {
      URL url = new URL(aUrl);
      conn = url.openConnection();
      if (conn != null)
        conn.setReadTimeout(20 * 1000); // timeout in 20 seconds
      if (conn != null && conn.getInputStream() != null) {
        in = new InputStreamReader(conn.getInputStream(), Charset.defaultCharset());
        BufferedReader br = new BufferedReader(in);
        if (br != null) {
          int ch;
          // read the next character until end of reader
          while ((ch = br.read()) != -1) {
            sb.append((char) ch);
          }
          br.close();
        }
      }
      in.close();
    } catch (Exception ex) {
      System.out.println("Exception in url request:" + ex.getMessage());
    }
    return sb.toString();
  }
}
