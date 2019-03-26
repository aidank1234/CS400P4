//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           HashTable
// Files:           HashTable.java, HashTableADT.java
// Course:          Computer Science 400, Spring 2019
//
// Author:          Aidan Kaiser
// Email:           apkaiser@wisc.edu
// Lecturer's Name: Debra Deppler
// Due:             03/15/2019 by 10pm
//
///////////////////////////// CREDIT OUTSIDE HELP /////////////////////////////
//
// Students who get help from sources other than their partner must fully 
// acknowledge and credit those sources of help here.  Instructors and TAs do 
// not need to be credited here, but tutors, friends, relatives, room mates, 
// strangers, and others do.  If you received no outside help from either type
//  of source, then please explicitly indicate NONE.
//
// Persons:         None
// Online Sources:  None
//

import java.util.ArrayList;

/**
 * This class represents a HashTable, an extremely fast data structure. I implemented
 * my HashTable with open addressing, specifically linear probing. In each occupied array index,
 * there is a key/value pair. I used an ArrayList to keep a running track of all indicies
 * occupied in the HashTable in order to keep rehashing O(N).
 * @author Aidan Kaiser
 */
public class HashTable<K extends Comparable<K>, V> implements HashTableADT<K, V> {
	
  //Class fields
	private int tableSize;
	private double elements = 0.0;
	private double loadFactorThreshold;
	private ArrayList<Integer> usedIndicies;
  private ArrayList<Integer> usedIndiciesTemp;
  private KeyValue[] pairs;
	
  
  /**
   * This class represents a key/value pair in the HashTable, with simple getter methods
   * @author Aidan Kaiser
   */
	private class KeyValue {
	  private K key;
	  private V value;
	  
	   /**
     * Creates a new key/value pair to be added to the HashTable
     * @param key is the key of the key/value pair
     * @param value is the value of the key/value pair
     */
	  private KeyValue(K key, V value) {
	    this.key = key;
	    this.value = value;
	  }
	  
	  /**
	   * @return the key of the key/value pair
	   */
	  private K getKey() {
	    return this.key;
	  }
	   /**
     * @return the value of the key/value pair
     */
	  private V getValue() {
	    return this.value;
	  }
	  
	}
	
		
  /**
   * Initializes a HashTable with a default capacity of ten and a load
   * factor threshold of 0.75
   */
	@SuppressWarnings("unchecked")
	public HashTable() {
	  this.tableSize = 10;
	  this.loadFactorThreshold = 0.75;
	  this.pairs = new HashTable.KeyValue[tableSize];
	  this.usedIndicies = new ArrayList<Integer>();
	  this.usedIndiciesTemp = new ArrayList<Integer>();
	}
	
  /**
   * Initializes a HashTable with a user specified initial capacity and
   * load factor threshold
   * @param initialCapacity is the inital tableSize of the HashTable
   * @param loadFactorThreshold is the load factor that triggers a rehash
   */
	@SuppressWarnings("unchecked")
	public HashTable(int initialCapacity, double loadFactorThreshold) {
	  this.tableSize = initialCapacity;
	  this.loadFactorThreshold = loadFactorThreshold;
	  this.pairs = new HashTable.KeyValue[tableSize];
	  this.usedIndicies = new ArrayList<Integer>();
	  this.usedIndiciesTemp = new ArrayList<Integer>();
	}
	
  /**
   * Inserts a new key value pair into the HashTable, performing a reshash
   * if the load factor is exceeded. Avoids collisions with linear probing
   * 
   * @param key is the key of the key/value pair being added
   * @param value is the value of the key/value pair being added
   * 
   * @throws IllegalNullKeyException if param key is null
   * @throws DuplicateKeyException if param key has already been added
   */
  @Override
  public void insert(K key, V value) throws IllegalNullKeyException, DuplicateKeyException {
    if(key == null)
      throw new IllegalNullKeyException();
    
    //Take absolute value of hashCode in the case of negative numbers
    int hashCode = Math.abs(key.hashCode());
    int hashIndex = hashCode % tableSize;
    KeyValue newPair = new KeyValue(key, value);
    
    //If hashIndex is empty, add pair
    if(pairs[hashIndex] == null) {
      pairs[hashIndex] = newPair;
      //Add to tracked indicies
      usedIndicies.add(hashIndex);
      elements = elements + 1;
    }
    else {
      //Same key
      if(pairs[hashIndex].getKey().compareTo(key) == 0)
        throw new DuplicateKeyException();
      
      boolean insertSuccess = false;
      int probe = 1;
      //Keep incrementing the index by one until an empty index is found to add to
      while(insertSuccess == false) {
        int tempIndex = hashIndex;
        tempIndex = (tempIndex + probe) % tableSize;
        //Found empty index
        if(pairs[tempIndex] == null) {
          insertSuccess = true;
          pairs[tempIndex] = newPair;
          //Add to tracked indicies
          usedIndicies.add(tempIndex);
          elements = elements + 1;
        }
        else {
          //Same key
          if(pairs[tempIndex].getKey().compareTo(key) == 0)
            throw new DuplicateKeyException();
          probe++;
        }
      }
    }
    //Table size needs to be larger according to threshold
    if(getLoadFactor() > loadFactorThreshold) {
      rehash();
    }
  }
  
  /**
   * Private helper method for insert. When load factor is exceeded, this method
   * increases the table size by 2n+1 and rehashes all added elements in the HashTable
   * according to the new table size.
   * 
   * @throws DuplicateKeyException when insert param key has already been added
   * @throws IllegalNullKeyException when insert param key is null
   */
  @SuppressWarnings("unchecked")
  private void rehash() throws DuplicateKeyException, IllegalNullKeyException {
    //Create actual copy of arrayList (not just copy of reference)
    ArrayList<Integer> indicies = new ArrayList<Integer>(usedIndicies);
    //Clear the tracked indicies for next time
    usedIndicies.clear();
    KeyValue[] tempPairs = pairs;
    //Empty pairs with new table size after copying
    pairs = new HashTable.KeyValue[tableSize * 2 + 1];
    tableSize = tableSize * 2 + 1;
    elements = 0;
    //Loop through all tracked indicies and re-insert into HashTable with new capacity
    for(int i=0; i<indicies.size(); i++) {
      insert(tempPairs[indicies.get(i)].getKey(), tempPairs[indicies.get(i)].getValue());
    }
  }

  /**
   * Searches for and removes the specified key from the HashTable, searching using
   * linear probing.
   * 
   * @param key is the key to remove from the HashTable
   * 
   * @throws IllegalNullKeyException if param key is null
   * @return true if remove was successful
   */
  @Override
  public boolean remove(K key) throws IllegalNullKeyException {
    if(key == null)
      throw new IllegalNullKeyException();
    int hashCode = Math.abs(key.hashCode());
    int hashIndex = hashCode % tableSize;
    int probe = 1;
    //Search for the key while following the exact insert algorithm
    //Until it is impossible for the key to be in the HashTable
    if(hashIndex < pairs.length - 1 && pairs[hashIndex] != null) {
      //Key found
      while(pairs[hashIndex].getKey().compareTo(key) != 0) {
        hashCode = hashCode + (probe);
        hashIndex = hashCode % tableSize;
        //End of table
        if(hashIndex > pairs.length - 1 || pairs[hashIndex] == null)
          return false;
        probe++;
      }
      //Delete value from HashTable and tracked indicies
      pairs[hashIndex] = null;
      elements--;
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Searches for and returns the value of a key/value pair in the HashTable
   * according to the specified key
   * 
   * @param key is the key to get from the HashTable
   * 
   * @throws IllegalNullKeyException if param key is null
   * @throws KeyNotFoundException if the provided key is not found
   * @return value of removed key/value pair
   */
  @Override
  public V get(K key) throws IllegalNullKeyException, KeyNotFoundException {
    if(key == null)
      throw new IllegalNullKeyException();
    int hashCode = Math.abs(key.hashCode());
    int hashIndex = hashCode % tableSize;
    int probe = 1;
    //Go through HashTable with linear probing
    if(pairs[hashIndex] != null) {
      while(pairs[hashIndex].getKey().compareTo(key) != 0) {
        hashCode = hashCode + (probe);
        hashIndex = hashCode % tableSize;
        //Impossible to find element, throw exception
        if(hashIndex > pairs.length - 1 || pairs[hashIndex] == null)
          throw new KeyNotFoundException();
        probe++;
      }
      //Key has been found, return it
      return pairs[hashIndex].getValue();
    }
    else {
      throw new KeyNotFoundException();
    }
  }

  /**
   * @return number of key/value pairs in the HashTable
   */
  @Override
  public int numKeys() {
    return (int) elements;
  }

  /**
   * @return the load factor threshold of the HashTable
   */
  @Override
  public double getLoadFactorThreshold() {
    return loadFactorThreshold;
  }

  /**
   * @return the current load factor of the HashTable
   */
  @Override
  public double getLoadFactor() {
    return elements/tableSize;
  }

  /**
   * @return the current capacity of the HashTable
   */
  @Override
  public int getCapacity() {
    return tableSize;
  }

  /**
   * @return an integer representing linear probing
   */
  @Override
  public int getCollisionResolution() {
    return 1;
  }




		
}
