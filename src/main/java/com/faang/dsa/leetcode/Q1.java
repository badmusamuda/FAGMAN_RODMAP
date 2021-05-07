import java.util.*;

/**
*Question #1 : https://leetcode.com/problems/two-sum/
*
*
*<p>The question aim to check the following
*<li>How to use HashTable to simplify O(N^2) solution  </li>
*
*<li>Searching a linear data-structure using HashTable or BinaryTree</li>
*
*<li>Utilising Hashtable storage </li>
*</p>
*
*@author      Amuda Adeolu Badmus
*@email       badmusamuda@gmail.com
*@date        04-May-2021
*
*<p>Note : I was asked this same (similar or related) question for Amazon SDE-2 role(Onsite stage)</p>
*
*</p>
*/

public final class Q1{

   //Solution #1(Iteratively)
   /**
   *T = O(N^2) (Where N = @param nums.length i.e the size of the array
   *S = O(1)
   *<p>Leetcode submission details</p><br/>
   *<p>
   *Runtime: 0 ms, faster than 100.00% of Java online submissions for Two Sum.
   *
   *Memory Usage: 39.1 MB, less than 44.73% of Java online submissions for Two Sum.
   *</p>
   *
   *Note : This is a brute-force solution
   *
   * Algorithm
   *
   * Step[0] : Create two pointers
   *
   * Step[1] : Move first pointer only when second pointer have reached its tail
   *
   * Step[2] : Do the compuaton, to find the target
   *
   */
   public int[] twoSum_Iteratively(int[] nums, int target) {
    
   	int   i   = 0x0;
   	for(; i < nums.length; i++){
   	      
   	   for(int j = i + 0x1; j < nums.length; j++){
   	      int sum = nums[i] + nums[j];
   	      if(sum == target) return new int[]{i, j};
   	   }
        }
   	throw new IllegalArgumentException("NO_TWO_SUM_FOUND");
    }
    
    
    // Solution #2(Imporved version of Solution #1 using Single Table)
    /**
    * Runtime: 2 ms, faster than 32.11% of Java online submissions for Two Sum.
    * Memory Usage: 39 MB, less than 58.81% of Java online submissions for Two Sum.
    *
    *Algorithm
    *
    *Step[0] : Keep into hash-table
    *
    *Step[1] : Compute and Check the key
    *
    */
    
    
   public int[] twoSum_ImporvedVersionUsingHT(int[]nums, int target){
        
        HashMap<Integer,Integer> h = new HashMap<>();
        
        for(int i = 0x0; i < nums.length; i++){
        	int diff = target - nums[i];
            
        	System.out.println(h);
        	    if(h.containsKey(diff) ) return new int[]{i, h.get(diff)};
        	h.put(nums[i], i);
        }
        
        throw new IllegalArgumentException("NO_POSSIBLE_TWO_SUM");
        
   }
   
  
    
    //Solution #2(Imporved version of Solution #1 using Hashtable)
    /**
    *Runtime: 2 ms, faster than 32.11% of Java online submissions for Two Sum.
	Memory Usage: 39.3 MB, less than 20.57% of Java online submissions for Two Sum.
    
    **Algorithm
    *
    *Step[0] : Keep into hash-table
    *
    *Step[1] : Compute and Check the key
    
    *
    */
    public int[] twoSum_ImporvedVersionUsingHashTable(int[]nums, int target){
    	   Map<Integer,Integer> m = merge(nums);
    	   
    	   for(int i = 0x0; i < nums.length; i++){
    	   	int result = target - nums[i];
    	   	
    	   	if( (m.containsKey(result)) && (m.get(result) != i) )
    	   		return new int[]{i, m.get(result)};
    	   
    	   }
    	   throw new IllegalArgumentException("NO_POSSIBLE_SUMS");
    
    }
    
    private Map<Integer,Integer> merge(int[]nums){
    	Map<Integer,Integer> m = new HashMap<>();
    	
    	int i = 0x0;
    	for(; i < nums.length; i++){
    		m.put(nums[i], i);
    	}
    	return m;
    }
    
    
}
