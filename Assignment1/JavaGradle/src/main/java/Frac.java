import java.io.*;
/**
 * Purpose: demonstrate simple Java Fraction class with command line,
 * jdb debugging, and Ant build file.
 *
 * Ser321 Foundations of Distributed Applications
 * see http://pooh.poly.asu.edu/Ser321
 * @author Tim Lindquist Tim.Lindquist@asu.edu
 *         Software Engineering, CIDSE, IAFSE, ASU Poly
 * @version January 2020
 */
public class Frac {

   private int numerator, denominator;

   public Frac(){
      numerator = denominator = 0;
   }

   public void print() {
    System.out.print(numerator + "/" + denominator );
   }

   public void setNumerator (int n ){
      numerator = n;
   }

   public void setDenominator (int d) {
      denominator = d;
   }

   public int getDenominator() {
      return denominator;
   }

   public int getNumerator() {
      return numerator;
   }
   

   public static void main (String args[]) {
      try {
         // create a new instance
         // Fraction *frac = [[Fraction alloc] init];
         Frac frac = new Frac();
         
         int argX = 1;
         int argY = 1;
         
         System.out.println(args.length);
         if (args.length == 2) {
         
         try {
          argX = Integer.parseInt(args[0]);
          argY = Integer.parseInt(args[1]);
        } catch (Exception e) {
          System.out.println("Please use -Pnum=# and-Pdenom=#.");
          System.exit(1);
        }
        
        frac.setNumerator(argX);
        frac.setDenominator(argY);
         

         // print it
         System.out.print("The fraction is: ");
         frac.print();
         System.out.println("");
         }else{
         	System.out.println("Please use -Pnum=# and-Pdenom=#.");
         			System.exit(1);
         }
      }catch(Exception e) {
         e.printStackTrace();
      }
   }
}

