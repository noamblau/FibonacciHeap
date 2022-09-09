//Noam Blau (noamblau) - 207145293
//Ido Schwartz (idoschwartz) - 208601781

/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap
{
	private HeapNode first;
	private HeapNode min;
	private int size;
	private int trees;
	private int marked;
	private static int links = 0;
	private static int cuts = 0;
	
	/**
	   * FibonacciHeap()
	   *
	   * constructor
	   * 
	   * time complexity: O(1)
	   *
	   */
	public FibonacciHeap() 
	{
		this.first = null;
		this.min = null;
		this.size = 0;
		this.trees = 0;
		this.marked = 0;
	}
	
	/**
	    * public HeapNode getFirst()
	    *
	    * Return the first node of the heap. 
	    * 
	    * time complexity: O(1)
	    *
	    */
	public HeapNode getFirst(){
		return this.first;
	}

   /**
    * public boolean isEmpty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    * 
    * time complexity: O(1)
    *   
    */
    public boolean isEmpty()
    {
    	return this.first == null;
    }
		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
    * 
    * Returns the new node created. 
    * 
    * time complexity: O(1)
    * 
    */
    public HeapNode insert(int key)
    {    
    	HeapNode node = new HeapNode(key);
    	node.setRank(0);
    	//heap is empty
    	if (this.isEmpty()) {
    		this.first = node;
    		this.min = this.first;
    		node.connect(node);
    	}
    	//heap is not empty
    	else {
    		this.first.getPrev().connect(node);
    		node.connect(this.first);
    		this.first = node;
    		//we need to compare between min and the new node and update min
    		if (this.min.getKey() > node.getKey()) 
    			this.min = node;
    	}
    	//updade sizes
    	this.size++;
    	this.trees++;
    	return node;
    }

   /**
    * public void deleteMin()
    *
    * Delete the node containing the minimum key.
    * 
    * time complexity: O(logn) amortized, O(n) worst case
    *
    */
    public void deleteMin()
    {
    	HeapNode node;
    	//heap is empty
    	if (this.isEmpty()) return;
    	//heap is not empty
    	this.size--;
    	this.trees--;
    	//min is marked
    	if (this.min.getMark())
    		this.marked--;
    	//min has no child
    	if (this.min.getRank() == 0) {
    		//after deletion heap is empty
    		if (trees == 0) {
    			this.first = null;
    			this.min = null;
    			return;
    		}
    		if (this.first == this.min) this.first = this.first.getNext();
    		node = this.min.getPrev();
    		this.min.getPrev().connect(this.min.getNext());
    		this.min = node;
    		
    	}
    	//min has child
    	else {
    		HeapNode firstNode = this.min.getChild();
    		node = firstNode;
    		//make the child's parent null
    		node.setParent(null);
			if (node.getMark()) {
				node.setMark(false);
				this.marked--;
			}
			node = node.next;
    		while(node != firstNode) {
    			//make the child's brothers' parent null
				node.setParent(null);
				if (node.getMark()) {
					node.setMark(false);
					this.marked--;
				}
				node = node.next;
			}
    		if (trees > 0) {
    			HeapNode prev = firstNode.getPrev();
    			this.min.getPrev().connect(firstNode);
    			prev.connect(this.min.getNext());
    		}
    		this.first  = firstNode;
    		this.min = this.first;
    	}	
    	
		this.consolidate();
     	
    }
    
    /**
     * public void consolidate()
     *
     * Make the heap structure similar to binomial heap
     * 
     * time complexity: O(logn) amortized, O(n) worst case
     *
     */
    private void consolidate() 
    {
    	//to buckets
    	int maxDeg = (int) (Math.ceil(Math.log(this.size) / Math.log(2)) + 1); //In a Fibonacci heap containing n items, all ranks are at most log1.618(n) ≤ 1.4404 log2(n)
		HeapNode[] buckets = new HeapNode[maxDeg];
		
		//link all the trees from the same rank
		HeapNode currNode, node = this.first;
		node.getPrev().setNext(null);
		while(node != null) {
			currNode = node;
			node = node.getNext();
			while (buckets[currNode.getRank()] != null) {
				currNode = this.link(currNode, buckets[currNode.getRank()]);
				buckets[currNode.getRank()-1] = null;
			}
			buckets[currNode.getRank()] = currNode;
		}
		
    	//from buckets
		node = null;
		this.trees = 0;
		for (int i = 0; i < buckets.length; i++) {
			//build the heap from the trees in the array
			if (buckets[i] != null) {
				this.trees++;
				if (node == null) {
					node = buckets[i];
					node.connect(node);
					this.first = node;
					this.min = this.first;
				}
				else {
					node = buckets[i];
					this.first.getPrev().connect(node);
					node.connect(this.first);
		    		this.first = node;
		    		//update minimum
		    		if (this.min.getKey() > node.getKey()) 
		    			this.min = node;
				}
			}
		}
    }
    
    /**
     * private HeapNode link(HeapNode node1, HeapNode node2)
     *
     * Linking two trees of the same rank.
     * 
     * time complexity: O(1)
     *
     */
    private HeapNode link(HeapNode node1, HeapNode node2) 
    {
    	links++;
    	//small is the tree that its root key is smaller
    	HeapNode small, large;
    	if (node1.getKey() < node2.getKey()) {
    		small = node1;
    		large = node2;
    	}
    	else {
    		small = node2;
    		large = node1;
    	}
    	//the rank of the trees is 0
    	if (small.getRank() == 0) {
    		small.setChild(large);
    		large.setParent(small);
    		small.connect(small);
    		large.connect(large);
    	}
    	//the rank of the trees is greater than 0
    	else {
    		large.setParent(small);
    		small.getChild().getPrev().connect(large);
    		large.connect(small.getChild());
    		small.setChild(large);
    	}
    	small.setParent(null);
		if (small.getMark()) {
			small.setMark(false);
			this.marked--;
		}
    	small.setNext(null);
		small.setPrev(null);
		small.setRank(small.getRank()+1);
    	return small;
    }

   /**
    * public HeapNode findMin()
    *
    * Return the node of the heap whose key is minimal. 
    * 
    * time complexity: O(1)
    *
    */
    public HeapNode findMin()
    {
    	return this.min;
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Meld the heap with heap2
    * 
    * time complexity: O(1)
    *
    */
    public void meld (FibonacciHeap heap2)
    {
		  HeapNode heap1First = this.getFirst();  
	      HeapNode heap1Last = heap1First.getPrev();
	      HeapNode heap2First = heap2.getFirst();
	      HeapNode heap2Last = heap2First.getPrev();
		  HeapNode heap1Min = this.findMin();
		  HeapNode heap2Min = heap2.findMin();
		  heap1Last.connect(heap2First);
		  heap2Last.connect(heap1First);
    	  if(heap1Min.getKey()<heap2Min.getKey())
    		  this.min = heap1Min;
    	  else
    		  this.min = heap2Min;
    	  this.trees += heap2.trees;
    	  this.size += heap2.size;
    	  this.marked += heap2.marked;
    }

   /**
    * public int size()
    *
    * Return the number of elements in the heap
    * 
    * time complexity: O(1)
    *   
    */
    public int size()
    {
    	return this.size;
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
    * 
    * time complexity: O(n)
    * 
    */
    public int[] countersRep()
    {
    	//heap is empty
    	if (this.isEmpty())
    		return new int[0];
    	
    	//heap is not empty
    	int maxDeg = (int) (Math.ceil(Math.log(this.size) / Math.log(2)) + 1); //In a Fibonacci heap containing n items, all ranks are at most log1.618(n) ≤ 1.4404 log2(n)
		int[] retArr = new int[maxDeg];

		retArr[this.min.rank]++;
		HeapNode next = this.min.getNext();
		while (next != this.min) {
			retArr[next.rank]++;
			next = next.getNext();							
		}
		return retArr; 
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap. 
    * 
    * time complexity: O(logn) amortized, O(n) worst case
    *
    */
    public void delete(HeapNode x) 
    {    
    	this.decreaseKey(x, x.getKey() - (this.min.getKey() - 1));
    	this.deleteMin();
    	return;
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this chage (for example, the cascading cuts procedure should be applied if needed).
    * 
    * time complexity: O(1) amortized, O(logn) worst case
    */
    public void decreaseKey(HeapNode x, int delta)
    {    
    	int newKey = x.getKey() - delta;  
    	x.setKey(newKey);
    	int currMinKey = this.findMin().getKey();
    	if((x.getParent()!=null)&&(newKey<x.getParent().getKey()))
    		this.cascadingCut(x, x.getParent());
    	if(newKey<currMinKey)
    		this.min = x;
    }
    
    /**
     * private void cut(HeapNode son, HeapNode parent)
     *
     * Receives two nodes, the first of them is the son of the second,
     *  disconnects the son from the parent and connects the son 
     *  as a new tree in the heap. 
     * 
     * time complexity: O(1)
     *
     */
    private void cut(HeapNode son, HeapNode parent){
    	son.setParent(null);
    	if(son.getMark()) {
    		son.setMark(false);
    		marked--;
    	}
    	parent.setRank(parent.getRank() - 1);
    	if(son.getNext() == son) //parent has single son
    		parent.setChild(null);
    	else //parent has more than one son
    	{
    		parent.setChild(son.getNext());
    		son.getPrev().connect(son.getNext());
    	}
    	HeapNode oldfirst = this.getFirst();
    	HeapNode last = oldfirst.getPrev();
    	this.first = son;
    	last.connect(son);
    	son.connect(oldfirst);
    	trees++;
    }
    
    /**
     * private void cascadingCut(HeapNode son, HeapNode parent)
     *
     * Receives two nodes, the first of them is the son of the second,
     *  disconnects the son from the parent and connects the son 
     *  as a new tree in the heap. 
     *  Continues the cutting recursively upwards 
     *  as long as the parent nodes are marked 
     * 
     * time complexity: O(logn) worst case, O(1) amortized
     *
     */
    private void cascadingCut(HeapNode son, HeapNode parent) {
    	this.cut(son, parent);
    	cuts++;
    	if(parent.getParent()!=null){ //parent is not a root
    		if(!parent.getMark()) //parent is not marked
    		{
    			parent.setMark(true);
    			this.marked++;
    		}
    		else { //parent is marked
    			parent.setMark(false);
    			this.marked--;
    			cascadingCut(parent, parent.getParent());
    		}	
    	}
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap. 
    * 
    * time complexity: O(1)
    * 
    */
    public int potential() 
    {    
    	return this.trees + 2*this.marked;
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the run-time of the program.
    * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
    * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
    * in its root.
    * 
    * time complexity: O(1)
    * 
    */
    public static int totalLinks()
    {    
    	return links;
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the run-time of the program.
    * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
    * 
    * time complexity: O(1)
    * 
    */
    public static int totalCuts()
    {    
    	return cuts;
    }

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k minimal elements in a binomial tree H.
    * The function should run in O(k*deg(H)). 
    * You are not allowed to change H.
    */
    public static int[] kMin(FibonacciHeap H, int k)
    {    
        int[] smallestArr = new int[k];
        FibonacciHeap h2 = new FibonacciHeap(); 
        //h2 is used to store all the candidates for the next smallest node
        HeapNode nextSmallestNode = H.getFirst();
        smallestArr[0] = nextSmallestNode.getKey();
        for(int i=1; i<k; i++) {
        	if(nextSmallestNode.getChild()!=null) {
        		HeapNode firstSon = nextSmallestNode.getChild(); 
                HeapNode son = firstSon;
                do {
                	HeapNode h2Node = h2.insert(son.getKey());
                	h2Node.setNodeRef(son);
                	son =son.getNext();
                }while(son!=firstSon);
        	}
            nextSmallestNode = h2.findMin().getNodeRef();
            smallestArr[i] = nextSmallestNode.getKey();
            h2.deleteMin();
        }
        return smallestArr;
        
    }
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in 
    * another file 
    *  
    */
    public class HeapNode{

	private int key;
	private int rank;
	private boolean mark;
	private HeapNode child;
	private HeapNode next;
	private HeapNode prev;
	private HeapNode parent;
	// NodeRef - Node Reference, helper field for kmin method (see documentation)
	private HeapNode nodeRef;

  	public HeapNode(int key) {
	    this.key = key;
      }
  	
  	public void setKey(int key) {
	    this.key = key;
      }
  	
  	public int getKey() {
	    return this.key;
      }
  	
  	public void setRank(int rank) {
	    this.rank = rank;
      }
  	
  	public int getRank() {
	    return this.rank;
      }
  	
  	public void setMark(boolean mark) {
	    this.mark = mark;
      }
  	
  	public boolean getMark() {
	    return this.mark;
      }
    
    public void setChild(HeapNode child) {
	    this.child = child;
      }
    
    public HeapNode getChild() {
	    return this.child;
      }
    
    public void setNext(HeapNode next) {
	    this.next = next;
      }
    
    public HeapNode getNext() {
	    return this.next;
      }
    
    public void setPrev(HeapNode prev) {
	    this.prev = prev;
      }
    
    public HeapNode getPrev() {
	    return this.prev;
      }
    
    public void setParent(HeapNode parent) {
	    this.parent = parent;
      }
    
    public HeapNode getParent() {
	    return this.parent;
      }
    
    public void setNodeRef(HeapNode nodeRef) {
	    this.nodeRef = nodeRef;
      }
  	
  	public HeapNode getNodeRef() {
	    return this.nodeRef;
      }
    
  	//make two nodes brothers, connect them
    public void connect(HeapNode node) {
    	this.setNext(node);
    	node.setPrev(this);
    	}
    }
}
