import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class TestEncoder extends Thread{
	public int bufSize = UDPUtils.BUFFER_SIZE;
	public FEC encoder;
	public FileOutput output;
	public String fileName = "test.wav";
	public int blockNum;
	public int offset = 2;
	
	public void run(){
		System.out.println("filename:"+fileName+", md5:"+UDPUtils.getMD5(fileName));
		File f = new File(fileName);
		System.out.println("length:"+f.length()+", groups:"+(f.length()/(UDPUtils.BUFFER_SIZE*UDPUtils.DATA_BLOCK)+1));
		output = new FileOutput("output/");
		output.open(fileName, f.length());
		blockNum = 1;
		InputStream is;
		int tmpcounter = 0;
		try {
			is = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		byte buf[] = new byte[bufSize];
		int readSize;
		int M = 10;
		int N = 2;
		byte D[][] = new byte[M][bufSize+offset];
		byte C[][] = new byte[N][bufSize+offset];
		int count = 0;
		encoder = new FEC(M, N);
		System.out.println("begin");
//		long startTime = (new Date()).getTime();
		try {
			while ((readSize = is.read(buf, 0, bufSize)) != -1) {
				System.arraycopy(buf, 0, D[count], offset, readSize);
				System.arraycopy(UDPUtils.int2Bytes(blockNum, offset), 0, D[count], 0, offset);
				blockNum ++;
				count ++;
				if (count == M) {
					encoder.encode(D, C, bufSize, 2);
					for (int i = 0; i < N; i++) {
						System.arraycopy(UDPUtils.int2Bytes(blockNum, offset), 0, C[i], 0, offset);
						blockNum ++;
					}
//					for (int i = 0; i < M; i++) {
//						if (i == 2 || i == 3)
//							continue;
					for (int i = M-1; i >= 0; i--) {
						output.receive(D[i]);
					}
					for (int i = 0; i < N; i++) {
						output.receive(C[i]);
					}
					count = 0;
					tmpcounter++;
					if (tmpcounter % 5 == 0)
						sleep(20);
//					System.out.println("send "+tmpcounter+" groups");
//					sleep(10);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (count > 0) {
			for (int i = count; i < M; i++) {
				Arrays.fill(D[i], (byte)0);
				System.arraycopy(UDPUtils.int2Bytes(blockNum, offset), 0, D[i], 0, offset);
				blockNum ++;
			}
			encoder.encode(D, C, bufSize, 1);
			for (int i = 0; i < N; i++) {
				System.arraycopy(UDPUtils.int2Bytes(blockNum, offset), 0, C[i], 0, offset);
				blockNum ++;
			}
			for (int i = 0; i < M; i++) {
				output.receive(D[i]);
			}
			for (int i = 0; i < N; i++) {
				output.receive(C[i]);
			}
//			tmpcounter++;
//			System.out.println("send "+tmpcounter+" groups");
		}
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		long now = (new Date()).getTime();
//		System.out.println("total time:"+(now-startTime));
	}
	
	public static void main(String[] args) throws IOException {
		TestEncoder test = new TestEncoder();
		test.start();
	}
}
