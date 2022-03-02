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

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import java.lang.*;

public class CalcshapeImpl extends CalcshapeGrpc.CalcshapeImplBase{
	
	private String[][] shapeList;
	private String logFilename = "ShapeList.txt";
	
	public CalcshapeImpl() {
		super();
		
		try {
			readLogFile();
			
		}catch (Exception e) {
			System.err.println("Could not read in shape list: " + e.getMessage());
		}
		
	}
	
	public void listShapes(CalcshapeListRequest request, StreamObserver<CalcshapeListResponse> responseObserver) {
		CalcshapeListResponse.Builder response = CalcshapeListResponse.newBuilder();
		
		for(int i = 0; i < shapeList.length; i++) {
			Shape.Builder shapes = Shape.newBuilder();
			shapes.setName(shapeList[i][0]);
			shapes.setDirections(shapeList[i][1]);
			shapes.setAreaEquation(shapeList[i][2]);
			shapes.setParameterEquation(shapeList[i][3]);
			response.addShapes(shapes);
			System.out.println(shapes);
		}
		
		
		CalcshapeListResponse resp = response.build();  
		responseObserver.onNext(resp);       
		responseObserver.onCompleted(); 
	}
	
	public void area(CalcshapeAreaRequest request, StreamObserver<CalcshapeResponse> responseObserver) {
		CalcshapeResponse.Builder response = CalcshapeResponse.newBuilder();
		int index = -1;
		double width = request.getShape().getWidth();
		double height = request.getShape().getHeight();
		double solution;
		for(int i = 0; i < shapeList.length; i++) {
			if(shapeList[i][0].equalsIgnoreCase(request.getShape().getName())){
				index = i;
			}
		}
		
		if(index == -1) {
			response.setIsSuccess(false);
			response.setError("That shape is not availible for calculations");
		}else {
			try {
				ScriptEngineManager mgr = new ScriptEngineManager();
			    ScriptEngine engine = mgr.getEngineByName("JavaScript");
			    String equation = shapeList[index][2];
			    String wdth = new String().valueOf(width);
			    String hght = new String().valueOf(height);
			    equation = equation.replace("width", wdth);
			    equation = equation.replace("height" , hght);
			    
			    System.out.println(engine.eval(equation).toString());
			    solution = Double.parseDouble( engine.eval(equation).toString());
			    response.setSolution(solution);
			    response.setIsSuccess(true);
			}catch (ScriptException e) {
				System.out.println("Could not calculate equation");
				response.setIsSuccess(false);
				response.setError("Could not calculate area for " + request.getShape().getName()
					+ ". Please update Area equation for " + request.getShape().getName());
			}
		}
		
		CalcshapeResponse resp = response.build();  
		responseObserver.onNext(resp);       
		responseObserver.onCompleted(); 
	}
	
	public void parameter(CalcshapeParameterRequest request, StreamObserver<CalcshapeResponse> responseObserver) {
		CalcshapeResponse.Builder response = CalcshapeResponse.newBuilder();
		int index = -1;
		double width = request.getShape().getWidth();
		double height = request.getShape().getHeight();
		double solution;
		for(int i = 0; i < shapeList.length; i++) {
			if(shapeList[i][0].equalsIgnoreCase(request.getShape().getName())){
				index = i;
			}
		}
		
		if(index == -1) {
			response.setIsSuccess(false);
			response.setError("That shape is not availible for calculations");
		}else {
			try {
				ScriptEngineManager mgr = new ScriptEngineManager();
			    ScriptEngine engine = mgr.getEngineByName("JavaScript");
			    String equation = shapeList[index][3];
			    String wdth = new String().valueOf(width);
			    String hght = new String().valueOf(height);
			    equation = equation.replace("width", wdth);
			    equation = equation.replace("height" , hght);
			    
			    System.out.println(engine.eval(equation).toString());
			    solution = Double.parseDouble( engine.eval(equation).toString());
			    response.setSolution(solution);
			    response.setIsSuccess(true);
			}catch (ScriptException e) {
				System.out.println("Could not calculate equation");
				response.setIsSuccess(false);
				response.setError("Could not calculate parameter for " + request.getShape().getName()
					+ ". Please update parameter equation for " + request.getShape().getName());
			}
		}
		
		CalcshapeResponse resp = response.build();  
		responseObserver.onNext(resp);       
		responseObserver.onCompleted(); 
	}
	
	public void addShape(CalcshapeAddShapeRequest request, StreamObserver<CalcshapeAddShapeResponse> responseObserver) {
		CalcshapeAddShapeResponse.Builder response = CalcshapeAddShapeResponse.newBuilder();
		
		int index = -1;
		double width = 5;
		double height = 5;
		double solution = -1;
		boolean parameter = false, area = false;
		for(int i = 0; i < shapeList.length; i++) {
			if(shapeList[i][0].equalsIgnoreCase(request.getShape().getName())){
				index = i;
			}
		}
		
		System.out.println(request);
		try {
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");
			String equation = request.getShape().getParameterEquation();
			System.out.println(equation);
			String wdth = new String().valueOf(width);
			String hght = new String().valueOf(height);
			equation = equation.replace("width", wdth);
			equation = equation.replace("height" , hght);
			    
			System.out.println(engine.eval(equation).toString());
			solution = Double.parseDouble( engine.eval(equation).toString());
			parameter = true;
		}catch (ScriptException e) {
			System.out.println("Could not calculate equation");
			response.setIsSuccess(false);
			response.setError("Add Shape unsuccessful. Could not calculate parameter for " + request.getShape().getName()
				+ ". Please update parameter equation for " + request.getShape().getName());
		}
		
		try {
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");
			String equation = request.getShape().getAreaEquation();
			System.out.println(equation);
			String wdth = new String().valueOf(width);
			String hght = new String().valueOf(height);
			equation = equation.replace("width", wdth);
			equation = equation.replace("height" , hght);
			    
			System.out.println(engine.eval(equation).toString());
			solution = Double.parseDouble( engine.eval(equation).toString());
			area = true;
		}catch (ScriptException e) {
			System.out.println("Could not calculate equation");
			response.setIsSuccess(false);
			response.setError("Add Shape unsuccessful. Could not calculate area for " + request.getShape().getName()
				+ ". Please update area equation for " + request.getShape().getName());
		}
		
		if(area && parameter) {
			response.setIsSuccess(true);
			if(index == -1) {
				String[][] newShapeList = new String[shapeList.length + 1][4];
				
				for(int i = 0; i < shapeList.length; i++) {
					newShapeList[i][0] = shapeList[i][0];
					newShapeList[i][1] = shapeList[i][1];
					newShapeList[i][2] = shapeList[i][2];
					newShapeList[i][3] = shapeList[i][3];
				}
			
				newShapeList[shapeList.length][0] = request.getShape().getName();
				newShapeList[shapeList.length][1] = request.getShape().getDirections();
				newShapeList[shapeList.length][2] = request.getShape().getAreaEquation();
				newShapeList[shapeList.length][3] = request.getShape().getParameterEquation();
				
				shapeList = newShapeList;
				try {
					writeToLog();
				}catch (Exception e) {
					System.err.println("Could not write to log: " + e.getMessage());
				}
			}else {
				shapeList[index][1] = request.getShape().getDirections();
				shapeList[index][2] = request.getShape().getAreaEquation();
				shapeList[index][3] = request.getShape().getParameterEquation();
			}
		}else {
			response.setIsSuccess(false);
		}
		
		
		
		CalcshapeAddShapeResponse resp = response.build();  
		responseObserver.onNext(resp);       
		responseObserver.onCompleted(); 
	}
	
    public void writeToLog() throws Exception{
        
	    	Logs.Builder logs = Logs.newBuilder();
	
	        // we are writing shapeList to our log
	        // add a new log entry to the log list of the Protobuf object
	    	for(int i = 0; i < shapeList.length; i++) {
	    		logs.addLog(shapeList[i][0] + "~" + shapeList[i][1] + "~" + shapeList[i][2] + "~" + shapeList[i][3]);
	    	}
	    
	        // open log file
	        FileOutputStream output = new FileOutputStream(logFilename);
	        Logs logsObj = logs.build();
	
	        
	
	        // write to log file
	        logsObj.writeTo(output);
	        output.close();
        
    
}

/**
 * Reading the current log file
 * @return Logs.Builder a builder of a logs entry from protobuf
 */
public void readLogFile() throws Exception{
    Logs.Builder logs = Logs.newBuilder();
    String[][] list = new String[1][4];
    int length = 0;
    int i = 0;

    try {
        // just read the file and put what is in it into the logs object
        logs = logs.mergeFrom(new FileInputStream(logFilename));
        Logs List = logs.build();
        
        for (String log : List.getLogList()) {
		      length++;
		}
        
        list = new String[length][4];
        
        for (String log : List.getLogList()) {
        	list[i] = log.split("~");
        	System.out.println(log);
        	i++;
        }
       shapeList = list;
    } catch (FileNotFoundException e) {
        System.out.println(logFilename + ": File not found.  Creating a new file.");
        shapeList = list;
    }
}
}
