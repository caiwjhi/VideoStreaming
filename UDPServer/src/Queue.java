import java.util.Arrays;

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
	
	public boolean insert(byte[] d) {
		byte num = d[0];
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
		System.arraycopy(d, 1, data[now], 0, dataLen);
		return true;
	}
	
	public void output(byte[][] D, byte[][] C, int[] ready, int M, int N) {
		for (int i = 0; i < M; i++) {
			if (queue[pointer] != -1) {
				queue[pointer] = -1;
				System.arraycopy(data[pointer], 0, D[i], 0, dataLen);
				ready[i] = 1;
			} else {
				ready[i] = 0;
			}
			pointer = (pointer + 1) % len;
		}
		for (int i = 0; i < N; i++) {
			if (queue[pointer] != -1) {
				queue[pointer] = -1;
				System.arraycopy(data[pointer], 0, C[i], 0, dataLen);
				ready[i] = 1;
			} else {
				ready[i] = 0;
			}
			pointer = (pointer + 1) % len;
		}
		expected = (expected + M + N) % maxn;
	}
	
	public boolean ready(int M, int N) {
		int count = 0;
		for (int i = 0; i < M+N; i++) {
			if (queue[(pointer+i)%len] != -1) {
				count ++;
			}
		}
		if (count >= M) {
			return true;
		} else {
			return false;
		}
	}
	
	public void clear() {
		Arrays.fill(queue, -1);
		pointer = 0;
		expected = 0;
	}
	
}
