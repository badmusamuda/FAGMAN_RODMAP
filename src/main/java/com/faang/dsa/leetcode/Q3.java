import java.util.*;

/**
*Question #3 : https://leetcode.com/problems/longest-substring-without-repeating-characters/
*
*
*<p>The question aim to check the following
*
*<li>ASCII Table  </li>
*
*<li>Indexing/Search ASCII Table</li>
*
*
*</p>
*
*@author         Amuda Adeolu Badmus
*@email          badmusamuda@gmail.com
*@date           06-May-2021
*@lastModfied    07-May-2021
*
*</p>
*/
 
import java.util.*;
 
public final class Q3{
   
   /*
   *211 / 987 test cases passed.
   */
   
   public static int compute(String s){
   	
   	if(s.length() <= 0x1) return s.length();
   	
   	//This assume an ASICC size of 0x100 (i.e 256)
   	boolean[] ascii            = new boolean[0x100];
   	boolean[] defAscii         = new boolean[0x100];
   	List<Integer> radix      = new LinkedList<>();
   	
   	int max = 0x0;
   	
   	//ascii[(int) s.charAt(i)] = true;
   	
   	for(int i = 0; i < s.length(); i++){
   	     boolean indexed = ascii[ (int) s.charAt(i) ];
   	     int x = (int) s.charAt(i);

   	    //Check if the asciiTable has been indexed
   	    if(indexed){
   	        radix.add(max);
   	     
   	        if(s.charAt(i-0x1) == s.charAt(i))        
   	    		max = 0x1;
   	    	else 
   	    	        max = 0x0;	
   	    	ascii = defAscii;
   	    }else{
   	        max = max + 0x1;
   	        ascii[x] = true;
   	          	        
 	        if( i == (s.length()-0x1) ) radix.add(max);
 	       
   	    }
   		
   	}
   	return Collections.max(radix);
   }
   
   
   public static int secondApproach(final String v){
   	HashMap<Character,Integer> asciiTable = new HashMap<>();
   	
   	int temp = 0x1;
   	
   	for(int i = 0; i < v.length(); i++){
   		
   	     char x = v.charAt(i);	
   	     if(asciiTable.containsKey(x) ){
   	     	  temp = Math.max(temp, asciiTable.get(x));
   	     	  //asciiTable.remove(x);
   	     	  asciiTable.put(x, temp);
   	     	  temp++;
   	     	  //temp--;
   	     }else{
   	     	asciiTable.put(x, temp);
   	     	temp++;
   	     	//temp = Math.max(temp-1, 0x1);
   	     }	
   	}
   	return temp;
   }
   
   public static void main(String...args){
   	System.out.println( secondApproach(args[0]) );
   
   }  
}
