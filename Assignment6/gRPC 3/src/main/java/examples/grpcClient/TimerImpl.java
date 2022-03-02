package example.grpcclient;

import io.grpc.Server;                            
import io.grpc.ServerBuilder;                     
import io.grpc.ServerMethodDefinition;            
import io.grpc.stub.StreamObserver;               
import java.io.IOException;                       
import java.util.concurrent.TimeUnit;             
import service.*;                                 
import java.util.Stack;                           
                                                  
import java.io.InputStream;                       
import java.io.OutputStream;                      
import java.net.ServerSocket;                     
import java.net.Socket;                           
import java.util.ArrayList;                       
                                                  
import buffers.RequestProtos.Request;             
import buffers.RequestProtos.Request.RequestType; 
import buffers.ResponseProtos.Response;           

public class TimerImpl extends TimerGrpc.TimerImplBase {

	double[] TimerStartTimes;
	String[] TimerNames;
	int index;
	final long NANO_IN_SECONDS = 1000000000;
	
	public TimerImpl() {
		super();
		TimerStartTimes = new double[50];
		TimerNames = new String[50];
		index = 0;
	}
	
	@Override 
	public void start(TimerRequest req, StreamObserver<TimerResponse> responseObserver) {
		TimerResponse.Builder response = TimerResponse.newBuilder();
		boolean nameExists = false;
		
		if(req.getName().equals("")) {
			response.setIsSuccess(false);
			response.setError("Error: No timer name provided by client");
			System.out.println("Error: No timer name provided by client");
		}else {
			if(index < TimerNames.length) {
				for(int i = 0; i < index; i++) {
					if(TimerNames[i].equals(req.getName())) {
						nameExists = true;
					}
				}
				if(nameExists) {
					response.setIsSuccess(false);
					response.setError("Error: A Timer with that name already exists");
				}else {
					double startTime = System.nanoTime(); 
					startTime = startTime / NANO_IN_SECONDS; 
					TimerNames[index] = req.getName();
					TimerStartTimes[index] = startTime;
					index++;
					System.out.println(req.getName() + " timer successfully started!");
					response.setIsSuccess(true);
				}
				
			}else {
				String[] newTimerNames = new String[2* TimerNames.length + 1];
				double[] newTimerStartTimes = new double[2* TimerStartTimes.length + 1];
				for(int i = 0; i < index; i++) {
					newTimerNames[i] = TimerNames[i];
					newTimerStartTimes[i] = TimerStartTimes[i];
				}
				TimerNames = newTimerNames;
				TimerStartTimes = newTimerStartTimes;
				System.out.println("list have been made 2 times bigger.");
				
				double startTime = System.nanoTime(); 
				startTime = startTime / NANO_IN_SECONDS;
				TimerNames[index] = req.getName();
				TimerStartTimes[index] = startTime;
				index++;
				System.out.println(req.getName() + " timer successfully started!");
			}
		}
		
		
		TimerResponse resp = response.build();  
		responseObserver.onNext(resp);       
		responseObserver.onCompleted();  
	}
	
	@Override
	public void check(TimerRequest req, StreamObserver<TimerResponse> responseObserver) {
		TimerResponse.Builder response = TimerResponse.newBuilder();
		boolean timerExists = false;
		double secondsPassed = 0.0;
		
		if(req.getName().equals("")) {
			response.setIsSuccess(false);
			response.setError("Error: No timer name provided by client");
			System.out.println("Error: No timer name provided by client");
		}else {
			for(int i = 0; i < index; i++) {
				if(TimerNames[i].equals(req.getName())) {
					secondsPassed = System.nanoTime();
					secondsPassed = secondsPassed / NANO_IN_SECONDS;
					secondsPassed = secondsPassed - TimerStartTimes[i];
					
					timerExists = true;
				}
			}
			
			if(!timerExists) {
				response.setIsSuccess(false);
				response.setError("Error: No timer by that name exists");
				System.out.println("Error: No timer by that name exists");
			}else {
				Time.Builder time = Time.newBuilder().setName(req.getName());
				time.setSecondsPassed(secondsPassed);
				response.setTimer(time);
				response.setIsSuccess(true);
				System.out.println(req.getName() + " timer is at " + secondsPassed + " seconds.");
			}
		}
		
		TimerResponse resp = response.build();  
		responseObserver.onNext(resp);       
		responseObserver.onCompleted(); 
	}
	
	@Override
	public void close(TimerRequest req, StreamObserver<TimerResponse> responseObserver) {
		TimerResponse.Builder response = TimerResponse.newBuilder();
		int idx = -1;
		
		if(req.getName().equals("")) {
			response.setIsSuccess(false);
			response.setError("Error: No timer name provided by client");
			System.out.println("Error: No timer name provided by client");
		}else {
			for(int i = 0; i < index; i++) {
				if(TimerNames[i].equals(req.getName())) {
					idx = i;
				}
			}
			
			if(idx != -1) {
				String[] newTimerNames = new String[TimerNames.length];
				double[] newTimerStartTimes = new double[TimerStartTimes.length];
				for(int i = 0; i < idx; i++) {
					newTimerNames[i] = TimerNames[i];
					newTimerStartTimes[i] = TimerStartTimes[i];
				}
				
				for(int i = idx; i < index; i++) {
					newTimerNames[i] = TimerNames[i + 1];
					newTimerStartTimes[i] = TimerStartTimes[i + 1];
				}
				TimerNames = newTimerNames;
				TimerStartTimes = newTimerStartTimes;
				index--;
				response.setIsSuccess(true);
				System.out.println(req.getName() + " timer sucessfully closed.");
			}else {
				response.setIsSuccess(false);
				response.setError("Error: No timer by that name was found!");
				System.out.println("Error: No timer by that name was found!");
			}
		}
		
		TimerResponse resp = response.build();  
		responseObserver.onNext(resp);       
		responseObserver.onCompleted(); 
	}
	
	@Override
	public void list(Empty empty, StreamObserver<TimerList> responseObserver) {
		TimerList.Builder response = TimerList.newBuilder();
		double secondsPassed = 0;
		
		for(int i = 0; i < index; i++) {
			Time.Builder time = Time.newBuilder();
			time.setName(TimerNames[i]);
			
			secondsPassed = System.nanoTime();
			secondsPassed = secondsPassed / NANO_IN_SECONDS;
			secondsPassed = secondsPassed - TimerStartTimes[i];
			time.setSecondsPassed(secondsPassed);
			
			time.build();
			response.addTimers(time);
		}
		
		if(index == 0) {
			Time.Builder time = Time.newBuilder();
			time.setName("No timers yet...");
			
			
			time.build();
			response.addTimers(time);
		}
		
		
		TimerList resp = response.build();  
		responseObserver.onNext(resp);       
		responseObserver.onCompleted(); 
	}
	
}
