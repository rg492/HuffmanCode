import java.io.*;
import java.util.*;

/**
 * Group Project Part 1: Compressing a file
 *
 * Write a program that compresses a file using a Huffman Tree.
 * 
 * @author Group 2: Matthew Chan, Raul Garcia, Tina Tran, Tanmay Vijaywargiya, Zhen Zeng (Annie)
 * @version July 20, 2021
 */

public class Compress_a_file {
	public static void main (String[] args) throws IOException {
		File sourceFile = new File(args[0]);
		if (!sourceFile.exists()) {
			System.out.println("File " + args[0] + " does not exist.");
			return;
		}
			
			DataInputStream inputSource = new DataInputStream(new BufferedInputStream(new FileInputStream(sourceFile)));
			byte[] byteArray = new byte[inputSource.available()];
			inputSource.read(byteArray);
			inputSource.close();
			String text = new String(byteArray);
			System.out.println(text);
			
			int[] charFrequency = getCharacterFrequency(text);
			Tree tree = getHuffmanTree(charFrequency);
			String[] huffmanCodes = getCode(tree.root);
			String result = "";
			
			for (int i = 0; i < text.length(); i ++) {
				result += huffmanCodes[text.charAt(i)];
			}
			
			
			
			File compressedFile = new File(args[1]);
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(compressedFile));
			output.writeObject(huffmanCodes);
			output.writeInt(result.length());
			output.close();
			
			BitOutputStream output1 = new BitOutputStream(compressedFile);
			output1.writeBit(result);
			output1.close();
		
		System.out.println("done");
	}
	
	/** Get the frequency of the characters */
	public static int[] getCharacterFrequency(String text) {
		int[] counts = new int[256]; // 256 ASCII characters
		for (int i = 0; i < text.length(); i++)
			counts[(int)text.charAt(i)]++; // Count the characters in text 
		return counts;
	}
	public static String[] getCode(Tree.Node root) {
		if (root == null)
			return null;
		String[] codes = new String[2 * 128];
		assignCode(root, codes);
		return codes;
	}
	
	private static void assignCode(Tree.Node root, String[] codes) {
		if (root.left != null) {
			root.left.code = root.code + "0";
			assignCode(root.left, codes);
			
			root.right.code = root.code + "1";
			assignCode(root.right, codes);
		}
		else {
			codes[(int) root.element] = root.code;
		}
	}
	
	/** Get a Huffman tree from the codes */
	public static Tree getHuffmanTree(int[] counts) {
		Heap<Tree> heap = new Heap<>(); 
		for (int i = 0; i < counts.length; i++) { 
			if (counts[i] > 0) 
				heap.add(new Tree(counts[i], (char)i)); // A leaf node tree
		} 
		
		while (heap.getSize() > 1) { 
			Tree t1 = heap.remove(); // Remove the smallest weight tree 
			Tree t2 = heap.remove(); // Remove the next smallest
			heap.add(new Tree(t1, t2)); // Combine two trees
		}
		return heap.remove(); // The final tree 
	}
	
	static class Heap<E extends Comparable<E>> {
		private ArrayList<E> list = new ArrayList<>();
		
		/** Default empty constructor. */
		public Heap() {
		
		}
		
		/** Constructor that takes in an array of objects and puts them in a heap. */
		public Heap(E[] objects) {
			for (int i = 0; i < objects.length; i ++) {
				add(objects[i]);
			}
		}
		
		/** Adds and reorganizes the elements in the heap. */
		public void add(E newObject) {
			list.add(newObject);
			int currentIndex = list.size() - 1;
			
			while(currentIndex > 0) {
				int parentIndex = (currentIndex - 1) / 2;
				
				if (list.get(currentIndex).compareTo(list.get(parentIndex)) > 0) {
					E temp = list.get(currentIndex);
					list.set(currentIndex, list.get(parentIndex));
					list.set(parentIndex, temp);
				}
				else {
					break;
				}
				currentIndex = parentIndex;
			}
		}
		
		public E remove() {
			if(list.size() == 0) {
				return null;
			}
			
			E removedObject = list.get(0);
			list.set(0, list.get(list.size() - 1));
			list.remove(list.size() - 1);
			
			int currentIndex = 0;
			while (currentIndex < list.size()) {
				int leftChild = 2 * currentIndex + 1;
				int rightChild = 2 * currentIndex + 2;
				
				if(leftChild >= list.size()) {
					break;
				}
				int maxIndex = leftChild;
				if(rightChild < list.size()) {
					if(list.get(maxIndex).compareTo(list.get(rightChild)) < 0) {
						maxIndex = rightChild;
					}
				}
				
				if (list.get(currentIndex).compareTo(list.get(maxIndex)) < 0) {
					E temp = list.get(maxIndex);
					list.set(maxIndex, list.get(currentIndex));
					list.set(currentIndex, temp);
					currentIndex = maxIndex;
				}
				else {
					break;
				}
			}
			return removedObject;
		}
		
		public int getSize() {
			return list.size();
		}
	}
}

/** Define a Huffman coding tree */
class Tree implements Comparable<Tree> { 
	Node root; // The root of the tree
	
	/** Create a tree with two subtrees */
	public Tree(Tree t1, Tree t2) {
		root = new Node();
		root.left = t1.root; 
		root.right = t2.root;
		root.weight = t1.root.weight + t2.root.weight; 
	}
	
	/** Create a tree containing a leaf node */
	public Tree(int weight, char element) {
		root = new Node(weight, element); 
	}
	
	public int compareTo(Tree t) {
		if (root.weight < t.root.weight) // Purposely reverse the order 
			return 1; 
		else if (root.weight == t.root.weight)
			return 0;
		else 
			return -1; 
	}
	
	public class Node { 
		char element; // Stores the character for a leaf node
		int weight; // weight of the subtree rooted at this node
		Node left; // Reference to the left subtree
		Node right; // Reference to the right subtree
		String code = ""; // The code of this node from the root
		
		/** Create an empty node */
		public Node() {
			
		}
		
		/** Create a node with the specified weight and character */
		public Node(int weight, char element) {
			this.weight = weight;
			this.element = element;  
		}
	}
}

class BitOutputStream {
	private FileOutputStream output;
	private int bits;
	private int bitPosition;

	public static final int BYTE = 8; //8 bits is 1 byte

	/** Constructor takes in the file that is passed in a creates a FileOutputStream object with it. */
	public BitOutputStream(File file) throws IOException {
		output = new FileOutputStream(file, true);
	}
	
	/** writeBit accepts a string of bits and passes it into an overloaded writeBit for every character in the string. */
	public void writeBit(String bitString) throws IOException {
		for (int i = 0; i < bitString.length(); i ++) 
			writeBit(bitString.charAt(i));
	}
	
	/** Overloaded writeBit so that it goes character by character. */
	public void writeBit(char bit) throws IOException {
		bits = bits << 1;
		bitPosition ++;

		if (bit == '1')
			bits = bits | 1;

		if (bitPosition == BYTE) {
			output.write(bits);
			bitPosition = 0;
		}
	}

	public void close() throws IOException {
		bits = bits << BYTE - bitPosition;
		output.write(bits);

		output.close();
	}
}