
import java.util.Random;

public class Queue {
	private int length;
	private int[] queue;
	private int pointer;
	private int expected;
	private int maxn;
	
	public Queue(int len, int m, int e) {
		length = len;
		maxn = m;
		queue = new int[len];
		for (int i = 0; i < len; i++) {
			queue[i] = -1;
		}
		pointer = 0;
		expected = e;
	}
	
	public boolean insert(int num) {
		if (expected == -1) {
			queue[pointer] = num;
			expected = num;
			return true;
		}
		int tmp = (num + maxn - expected) % maxn;
		if (tmp > length) {
			return false;
		}
		int now = (pointer + tmp) % length;
		if (queue[now] != -1) {
			return false;
		}
		queue[now] = num;
		return true;
	}
	
	public int output() {
		int tmp = queue[pointer];
		if (tmp != -1) {
			queue[pointer] = -1;
			pointer = (pointer + 1) % length;
			expected = (expected + 1) % maxn;
		}
		//System.out.println("output"+tmp);
		return tmp;
	}
	
	/*public static void main(String[] args) {  
	      Queue q = new Queue(10, 255, 0);
	      Random r = new Random();
	      for (int i = 0; i < 20; i++) {
	    	  int tmp = r.nextInt(10);
	    	  System.out.println("insert"+tmp);
	    	  q.insert(tmp);
	    	  q.output();
	      }
	}*/
}
