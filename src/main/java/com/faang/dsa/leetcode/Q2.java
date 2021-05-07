import java.util.*;

/**
*Question #2 : https://leetcode.com/problems/add-two-numbers/
*
*
*<p>The question aim to check the following
*
*<li>How to traverse(recursively or iteratively) a LinkedList from head  </li>
*
*<li>Use Base 10 to reverse the SinglyLinkedLIst</li>
*
*<li>LinkedList insertion from head or tails</li>
*
*<li>Using the right data type for number that exceed (2^64)</li>
*
*<b>Most importantly : It aims to check one's understanding about int_long overflow value, and how to design a new datastructure that can handle such </b>
*</p>
*
*@author      Amuda Adeolu Badmus
*@email       badmusamuda@gmail.com
*@date        05-May-2021
*
*
*</p>
*/

/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
 
import java.math.BigInteger; 
 
public final class Q2{

   
   /*
   * Runtime: 22 ms, faster than 5.03% of Java online submissions for Add Two Numbers.
   * Memory Usage: 39.4 MB, less than 39.02% of Java online submissions for Add Two Numbers.
   *
   * This requires writing no algorith : Simple reverse the SinglyLinkedList by dividing the Node value using base_10
   */
 
   public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        
        BigInteger  x1      =  sum(l1); 
        BigInteger  x2      =  sum(l2);
        BigInteger  total   =  x1.add(x2);
        
        int compare = total.compareTo(BigInteger.valueOf(0));
        if(compare < 0)
            total = total.multiply(BigInteger.valueOf(-1));
        
       // System.out.println("Total ====> "+total);
        
        return insertFromHead(total);        
    }
    
    private BigInteger sum(ListNode list){
        
        BigInteger result      = BigInteger.valueOf(0);
        int baseIndex          = 0x0;
        BigInteger  base       = BigInteger.valueOf(0xA);
        BigInteger  output     = null;
        
        for(ListNode l = list; l != null; l = l.next){
            
            output              = BigInteger.valueOf(l.val);
            output              = output.multiply( base.pow(baseIndex) ); 
            
            result = result.add(output);
            
            baseIndex++;
        }
        return result;
    }
    
    private ListNode insertFromHead(BigInteger val){
        String[] token = String.valueOf(val).split("");
        ListNode head = null;
        
        for(int i = 0x0; i < token.length; i++){
            
            //If the list is empty
            if(head == null){
                head = new ListNode(Integer.valueOf(token[i]));
            }else{
                head = new ListNode(Integer.valueOf(token[i]), head);
                
            }   
        }        
        return head;
    }  
      
}
