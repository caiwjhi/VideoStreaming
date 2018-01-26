import java.util.Arrays;

public class Queue {
	private int len;
	private int dataLen;
	private int[] queue;
	private byte[][] data;
	public int pointer;
	public int expected;
	private int offset = 2;
	
	public Queue(int queueLength, int dataLength, int startNum) {
		len = queueLength;
		dataLen = dataLength;
		queue = new int[len];
		data = new byte[len][dataLen];
		for (int i = 0; i < len; i++) {
			queue[i] = -1;
		}
		pointer = 0;
		expected = startNum;
	}
	
	public boolean insert(byte[] d) {
		int num = UDPUtils.bytes2Int(d, 0, 2);
		if (expected == -1) {
			queue[pointer] = num;
			expected = num;
			return true;
		}
		int tmp = num - expected;
		if (tmp > len || tmp < 0) {
			System.out.println("num "+num+" out of queue with pointer "+ pointer+" and expected "+expected);
			return false;
		}
		int now = (pointer + tmp) % len;
		if (queue[now] != -1) {
			return false;
		}
		queue[now] = num;
		System.arraycopy(d, offset, data[now], 0, dataLen);
		return true;
	}
	
	public void output(byte[][] D, byte[][] C, int[] ready, int M, int N) {
//		printQueue(30);
		int tmpPointer = pointer;
		for (int i = 0; i < M; i++) {
			if (queue[tmpPointer] != -1) {
				queue[tmpPointer] = -1;
				System.arraycopy(data[tmpPointer], 0, D[i], 0, dataLen);
				ready[i] = 1;
			} else {
				ready[i] = 0;
			}
			tmpPointer = (tmpPointer + 1) % len;
		}
		for (int i = 0; i < N; i++) {
			if (queue[tmpPointer] != -1) {
				queue[tmpPointer] = -1;
				System.arraycopy(data[tmpPointer], 0, C[i], 0, dataLen);
				ready[M+i] = 1;
			} else {
				ready[M+i] = 0;
			}
			tmpPointer = (tmpPointer + 1) % len;
		}
		pointer = tmpPointer;
		expected = expected + M + N;
//		printQueue(30);
	}
	
	public int ready(int M, int N, int offset) {
		int count = 0;
		for (int i = offset; i < M+N+offset; i++) {
			if (queue[(pointer+i)%len] != -1) {
				count ++;
			}
		}
		return count;
	}
	
	public int getFirstMissing(int M, int N) {
		for (int i = 0; i < M+N; i++) {
			if (queue[(pointer+i)%len] == -1) {
				return expected+i;
			}
		}
		return -1;
	}
	
	public void printQueue(int n) {
		for (int i = 0; i < n; i++) {
			System.out.print(queue[(pointer+i)%len]+", ");
		}
		System.out.println();
	}
	
	public void clear() {
		Arrays.fill(queue, -1);
		pointer = 0;
		expected = 0;
	}
	
}
