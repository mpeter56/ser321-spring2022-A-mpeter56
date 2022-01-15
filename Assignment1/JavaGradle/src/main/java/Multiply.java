import java.io.*;
/**
 * Purpose: demonstrate simple Java Muliply class with command line,
 * jdb debugging, and Gradle build file.
 *
 * Ser321 Foundations of Distributed Applications
 * @author Aman Kaushik akaush13@asu.edu
 *         Software Engineering, CIDSE, ASU Poly
 * @version March 2020
  * @version August 2020 Alexandra Mehlhase changs for Gradle
 */
public class Multiply {
   public static void main (String args[]) {
   	int argX = 0;
        int argY = 0;
    if (args.length == 2) {
        
        try {
          argX = Integer.parseInt(args[0]);
          argY = Integer.parseInt(args[1]);
        } catch (Exception e) {
          System.out.println("Arguments: " + args[0] + ", " + args[1] + " must be integers.");
          System.exit(1);
        }
        System.out.println(argX + " * " + argY + " = " + argX * argY);
      } else if(args.length ==1 ){
          try {
          argX = Integer.parseInt(args[0]);
          argY = 1;
        } catch (Exception e) {
          System.out.println("Arguments: " + args[0] + " must be an integer.");
          System.exit(1);
        }
        System.out.println(argX + " * " + argY + " = " + argX * argY);
      }else {
      	 argX = 1;
      	 argY = 1;
      	 System.out.println(argX + " * " + argY + " = " + argX * argY);
      	 }
    }
}
