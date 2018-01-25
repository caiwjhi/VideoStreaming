import java.util.Arrays;

public class FEC {
	public int M; // data blocks
	public int N; // encoded blocks
	private int[][] V; // vandermonde matrix
	
	public FEC(int dataBlockNum, int encodedBlockNum) {
		M = dataBlockNum;
		N = encodedBlockNum;
		generateGF();
//		 generatePolynomial();
	}
	
	private void generateGF() {
		V = new int[N][M];
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				V[j][i] = ((int) Math.pow(j+1, i)) % 256;
			}
		}
//		printInt(V);
	}
	
	private short unsigned(byte x){
		return (short) (x & 0xFF);
	}
	
	public void encode(byte[][] D, byte[][] C, int len, int offset) {
		short tmp;
		for (int i = offset; i < len+offset; i++){
			tmp = 0;
			for (int j = 0; j < M; j++){
				 tmp += unsigned(D[j][i]);
			}
			C[0][i] = (byte)tmp;
		}
//		int tmp;
//		for (int digit = offset; digit < len+offset; digit++){
//			for (int i = 0; i < N; i++) {
//				tmp = 0;
//				for (int j = 0; j < M; j++){
//					 tmp += unsigned(D[j][digit]) * V[i][j];
//					 tmp %= 256;
//				}
//				C[i][digit] = (byte)tmp;
//			}
//		}
	}

	public void decode(byte[][] D, byte[][] C, int[] ready, int len) {
		int num = -1;
		for (int i = 0; i < M; i++) {
			if (ready[i] == 0) {
				num = i;
			}
		}
		if (num == -1) {
			return;
		}
		short tmp;
		for (int i = 0; i < len; i++){
			tmp = unsigned(C[0][i]);
			for (int j = 0; j < M; j++){
				if (j != num){
					tmp -= unsigned(D[j][i]);
				}
			}
			D[num][i] = (byte)tmp;
		}
//		int num = 0;
//		for (int i = 0; i < M; i++) {
//			if (ready[i] != 0) {
//				num ++;
//			}
//		}
//		if (num == M) // no need to decode
//			return;
//		
//		// get encoded from existing data blocks
//		int n = M - num;
//		byte[][] R = new byte[n][len];
//		int tmp = 0;
//		int pos = 0;
//		for (int digit = 0; digit < len; digit++){
//			for (int i = 0; i < N; i++) {
//				if (ready[M+i] == 0)
//					continue;
//				tmp = 0;
//				for (int j = 0; j < M; j++){
//					if (ready[j] != 0) {
//						tmp += unsigned(D[j][digit]) * V[pos][j];
//						tmp %= 256;
//					}
//				}
//				R[pos][digit] = (byte) ((unsigned(C[pos][digit]) - tmp + 256) % 256);
//				pos ++;
//			}
//		}
//		
//		// solve matrix V' * D' = R
//		for (int i = 0; i < M; i++) {
//			if (ready[i] == 0) {
//				System.arraycopy(R[0], 0, D[i], 0, len);
//				break;
//			}
//		}
	}
	
	public void print(byte[][] x) {
		for (int i = 0; i < x.length; i++){
			for (int j = 0; j < x[0].length; j++){
				System.out.print(x[i][j]);
				System.out.print(' ');
			}
		}
		System.out.println();
	}
	
	public void printInt(int[][] x) {
		for (int i = 0; i < x.length; i++){
			for (int j = 0; j < x[0].length; j++){
				System.out.print(x[i][j]);
				System.out.print(' ');
			}
		}
		System.out.println();
	}

//	public static void main(String[] args) {
//		FEC encoder = new FEC(5,2);
//		byte[][] D = new byte[5][1];
//		for (int i = 0; i < 5; i++){
//			D[i][0] = (byte) (100+i);
//			if (i % 2 == 0) {
//				D[i][0] = (byte) (0-i);
//			}
//		}
//		byte[][] C = new byte[2][1];
//		encoder.encode(D, C, 1, 0);
//		encoder.print(D);
//		encoder.print(C);
//		D[2][0] = 0;
//		int[] ready = new int[5];
//		Arrays.fill(ready, 1);
//		ready[2] = 0;
//		encoder.decode(D, C, ready, 1);
//		encoder.print(D);
//  }

}