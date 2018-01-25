import java.util.Arrays;

public class FEC {
	public int M; // data blocks
	public int N; // encoded blocks
	private byte[][] V; // vandermonde matrix
	private Calc calc;
	
	public FEC(int dataBlockNum, int encodedBlockNum) {
		M = dataBlockNum;
		N = encodedBlockNum;
		calc = new Calc();
		generateGF();
	}
	
	private void generateGF() {
		V = new byte[N][M];
		for (int j = 0; j < N; j++) {
			V[j][0] = 1;
			byte tmp = (byte) (j+1);
			for (int i = 1; i < M; i++) {
				V[j][i] = calc.mul(V[j][i-1], tmp);
			}
		}
//		print(V);
	}
	
//	private short unsigned(byte x){
//		return (short) (x & 0xFF);
//	}
	
	public void encode(byte[][] D, byte[][] C, int len, int offset) {
		for (int digit = offset; digit < len+offset; digit++){
			C[0][digit] = 0;
			for (int j = 0; j < M; j++){
				C[0][digit] ^= D[j][digit];
			}
			for (int i = 1; i < N; i++) {
				C[i][digit] = 0;
				for (int j = 0; j < M; j++){
					C[i][digit] ^= calc.mul(D[j][digit], V[i][j]);
				}
			}
		}
	}

	public void decode(byte[][] D, byte[][] C, int[] ready, int len) {
		int x = -1, y = -1;
		for (int i = 0; i < M; i++) {
			if (ready[i] == 0) {
				if (x == -1)
					x = i;
				else
					y = i;
			}
		}
		if (x == -1) { // no error
			return;
		}
		if (y == -1) { // 1 error
			if (ready[M] != 0) {
				for (int i = 0; i < len; i++){
					D[x][i] = C[0][i];
					for (int j = 0; j < M; j++){
						if (j != x){
							D[x][i] ^= D[j][i];
						}
					}
				}
			} else {
				for (int i = 0; i < len; i++){
					D[x][i] = C[1][i];
					for (int j = 0; j < M; j++){
						if (j != x){
							D[x][i] ^= calc.mul(D[j][i], V[1][j]);
						}
					}
					D[x][i] = calc.div(D[x][i], V[1][x]);
				}
			}
		} else { // 2 error
			// get encoded from existing data blocks
			byte[][] R = new byte[N][len];
			for (int digit = 0; digit < len; digit++) {
				for (int i = 0; i < N; i++) {
					R[i][digit] = C[i][digit];
					for (int j = 0; j < M; j++) {
						if (ready[j] != 0) {
							R[i][digit] ^= calc.mul(D[j][digit], V[i][j]);
						}
					}
				}
			}
			// solve matrix V' * D' = R
			byte tmp = (byte) (V[1][x] ^ V[1][y]);
			for (int digit = 0; digit < len; digit++) {
				D[x][digit] = calc.div((byte)(R[1][digit] ^ calc.mul(V[1][y], R[0][digit])), tmp);
				D[y][digit] = calc.div((byte)(R[1][digit] ^ calc.mul(V[1][x], R[0][digit])), tmp);
			}
		}
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

//	public static void main(String[] args) {
//		FEC encoder = new FEC(10,2);
//		byte[][] D = new byte[10][1];
//		for (int i = 0; i < 10; i++){
//			D[i][0] = (byte) (100+i);
//			if (i % 2 == 0) {
//				D[i][0] = (byte) (0-i);
//			}
//		}
//		byte[][] C = new byte[2][1];
//		encoder.encode(D, C, 1, 0);
//		encoder.print(D);
//		encoder.print(C);
//		D[0][0] = 0;
//		D[4][0] = 0;
//		int[] ready = new int[12];
//		Arrays.fill(ready, 1);
//		ready[0] = 0;
//		ready[4] = 0;
//		encoder.decode(D, C, ready, 1);
//		encoder.print(D);
//	}

}

class Calc {
	private int[] table;
	private int[] arcTable;
	private int[] inverseTable;
	
	Calc() {
		getTable();
		getArcTable();
		getInverseTable();
	}
	
	public byte mul(byte a, byte b) {
		int x = ((int)a) & 0xFF;
		int y = ((int)b) & 0xFF;
		if( x == 0 || y == 0 )
			return 0;
		return (byte)table[(arcTable[x] + arcTable[y]) % 255];
	}
	
	public byte div(byte a, byte b) {
		int x = ((int)a) & 0xFF;
		int y = inverseTable[((int)b) & 0xFF];
		if( x == 0 || y == 0 )
			return 0;
		return (byte)table[(arcTable[x] + arcTable[y]) % 255];
	}
	
	private void getTable() {
		table = new int[256];
		table[0] = 1; //g^0
		for(int i = 1; i < 255; ++i) { //生成元为x + 1
			//下面是m_table[i] = m_table[i-1] * (x + 1)的简写形式  
			table[i] = (table[i-1] << 1 ) ^ table[i-1];
			//最高指数已经到了8，需要模上m(x)
			if((table[i] & 0x100)!= 0) {
				table[i] ^= 0x11B;//用到了前面说到的乘法技巧
			}
		}
	}
	
	private void getArcTable() {
		arcTable = new int[256];
		for(int i = 0; i < 255; ++i)
			arcTable[ table[i] ] = i;
	}
	
	private void getInverseTable() {
		inverseTable = new int[256];
		for(int i = 1; i < 256; ++i) {//0没有逆元，所以从1开始
			int k = arcTable[i];
			k = 255 - k;
			k %= 255;//m_table的取值范围为 [0, 254]
			inverseTable[i] = table[k];
		}
	}
	
	
}