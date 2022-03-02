package example.grpcclient;

import io.grpc.Server;                            
import io.grpc.ServerBuilder;                     
import io.grpc.ServerMethodDefinition;            
import io.grpc.stub.StreamObserver;
import service.logs.Logs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;                       
import java.util.concurrent.TimeUnit;             
import service.*;                                 
import java.util.Stack;                           
                                                  
import java.io.InputStream;                       
import java.io.OutputStream;                      
import java.net.ServerSocket;                     
import java.net.Socket;                           
import java.util.ArrayList;
import java.util.Date;

import buffers.RequestProtos.Request;             
import buffers.RequestProtos.Request.RequestType; 
import buffers.ResponseProtos.Response;  

public class HometownsImpl extends HometownsGrpc.HometownsImplBase{
	
	String logFilename = "Hometowns.txt";
	
	public HometownsImpl() {
		super();
		
	}

	@Override
	public void read(Empty empty, StreamObserver<HometownsReadResponse> responseObserver) {
		System.out.println("read");
		HometownsReadResponse.Builder response = HometownsReadResponse.newBuilder();
		String[] elements;
		boolean found = false;
		try {
		// read old log file 
        Logs.Builder logs = readLogFile();
        Logs logsObj = logs.build();
        // iterate through the leader board
        for (String log: logsObj.getLogList()){
            System.out.println(log);
            elements = log.split(",");
            Hometown home = Hometown.newBuilder()
      			  .setCity(elements[0])
      			  .setRegion(elements[1])
      			  .setName(elements[2])
      			  .build();
            response.addHometowns(home);
            if(elements[0].equals("")) {
            	found = false;
            }else {
            	found = true;
            }
        }
        
        if(!found) {
        	response.setIsSuccess(false);
        	response.setError("There are no entries yet...");
        }else {
        	response.setIsSuccess(true);
        }
		}catch(Exception e) {
			System.out.println("Could not read hometown entry list...");
			response.setIsSuccess(false);
			response.setError("Could not read hometown entry list...");
            e.printStackTrace();
		}
		
		
		HometownsReadResponse resp = response.build();  
		responseObserver.onNext(resp);       
		responseObserver.onCompleted(); 
	}
	
	@Override
	public void search(HometownsSearchRequest request, StreamObserver<HometownsReadResponse> responseObserver) {
		String city = request.getCity();
		HometownsReadResponse.Builder response = HometownsReadResponse.newBuilder();
		String[] elements;
		boolean found = false;
		try {
			// read old log file 
	        Logs.Builder logs = readLogFile();
	        Logs logsObj = logs.build();
	        // iterate through the leader board
	        for (String log: logsObj.getLogList()){
	            System.out.println(log);
	            elements = log.split(",");
	            Hometown home = Hometown.newBuilder()
	      			  .setCity(elements[0])
	      			  .setRegion(elements[1])
	      			  .setName(elements[2])
	      			  .build();
	            if(elements[0].equalsIgnoreCase(city)) {
	            	response.addHometowns(home);
	            	found = true;
	            }
	        }
        
        if(found) {
        	response.setIsSuccess(true);
        }else {
        	response.setIsSuccess(false);
        	response.setError("Sorry, no entries for this city yet.");
        }
		}catch(Exception e) {
			System.out.println("Error: could not read hometown entry list...");
			response.setIsSuccess(false);
			response.setError("Could not read hometown entry list...");
            e.printStackTrace();
		}
		
		HometownsReadResponse resp = response.build();  
		responseObserver.onNext(resp);       
		responseObserver.onCompleted(); 
		
	}
	
	@Override
	public void write(HometownsWriteRequest request, StreamObserver<HometownsWriteResponse> responseObserver) {
		System.out.println("Name: " + request.getHometown().getName());
		System.out.println("City: " + request.getHometown().getCity());
		System.out.println("Region: " + request.getHometown().getRegion());
		HometownsWriteResponse.Builder response = HometownsWriteResponse.newBuilder();
		
		try {
		writeToLog(request.getHometown());
		response.setIsSuccess(true);
		}catch (Exception e) {
			System.err.println("Could not write to file");
			response.setIsSuccess(false);
			response.setError("Could not write hometown entry to list...");
		}
		HometownsWriteResponse resp = response.build();
		responseObserver.onNext(resp);       
		responseObserver.onCompleted(); 
	}
	
    public void writeToLog(Hometown hometown) throws Exception{
        
            // read old log file 
            Logs.Builder logs = readLogFile();

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(hometown.getCity() + "," +  hometown.getRegion() + "," + hometown.getName());

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
            output.close();
        
    }
    
    /**
     * Reading the current log file
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }
	
}
