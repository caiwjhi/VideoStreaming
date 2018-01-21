
//import java.util.Random;

public class Queue {
	private int len;
	private int dataLen;
	private int[] queue;
	private byte[][] data;
	private int pointer;
	private int expected;
	private int maxn;
	
	public Queue(int l, int d, int m, int e) {
		len = l;
		dataLen = d;
		maxn = m;
		queue = new int[len];
		data = new byte[len][dataLen];
		for (int i = 0; i < len; i++) {
			queue[i] = -1;
		}
		pointer = 0;
		expected = e;
	}
	
	public boolean insert(int num, int[] d) {
		if (expected == -1) {
			queue[pointer] = (byte) num;
			expected = num;
			return true;
		}
		int tmp = (num + maxn - expected) % maxn;
		if (tmp > len) {
			return false;
		}
		int now = (pointer + tmp) % len;
		if (queue[now] != -1) {
			return false;
		}
		queue[now] = num;
		System.arraycopy(d, 0, data[now], 0, dataLen);
		return true;
	}
	
	public int output(byte[] d) {
		int tmp = queue[pointer];
		if (tmp != -1) {
			queue[pointer] = -1;
			pointer = (pointer + 1) % len;
			expected = (expected + 1) % maxn;
			System.arraycopy(data[pointer], 0, d, 0, dataLen);
		}
//		System.out.println("output"+tmp);
		return tmp;
	}
	
//	public static void main(String[] args) {  
//	      Queue q = new Queue(10, 255, 0);
//	      Random r = new Random();
//	      for (int i = 0; i < 20; i++) {
//	    	  int tmp = r.nextInt(10);
//	    	  System.out.println("insert"+tmp);
//	    	  q.insert(tmp);
//	    	  q.output();
//	      }
//	}
}
