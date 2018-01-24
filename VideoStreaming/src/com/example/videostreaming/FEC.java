package com.example.videostreaming;

public class FEC {
	public int M; // data blocks
	public int N; // encoded blocks
	
	public FEC(int dataBlockNum, int encodedBlockNum) {
		M = dataBlockNum;
		N = encodedBlockNum;
		// generateGF();
		// generatePolynomial();
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
	}
	
	public void print(byte[][] x) {
		for (int i = 0; i < x.length; i++){
			for (int j = 0; j < x[0].length; j++){
				System.out.print(x[i][j]);
				System.out.print(' ');
			}
		}
		System.out.print('\n');
	}

//  public static void main(String[] args) {
//      FEC encoder = new FEC(5,1);
//      byte[][] D = new byte[5][1];
//      for (int i = 0; i < 5; i++){
//          D[i][0] = (byte) (100+i);
//          if (i % 2 == 0) {
//              D[i][0] = (byte) (0-i);
//          }
//      }
//      byte[][] C = new byte[1][1];
//      encoder.encode(D, C, 1);
//      encoder.print(D);
//      encoder.print(C);
//      D[2][0] = 0;
//      encoder.decode(D, C, 1, 2);
//      encoder.print(D);
//  }

}