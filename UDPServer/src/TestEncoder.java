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
	public String fileName = "test.mp3";
	public int blockNum;
	public int maxn = 128;
	
	public void run(){
		File f = new File(fileName);
		System.out.println("filename:"+fileName+",length:"+f.length());
		System.out.println("groups:"+f.length()/UDPUtils.BUFFER_SIZE/5+1);
		output = new FileOutput();
		output.open("output/"+fileName, f.length());
		blockNum = 0;
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
		int M = 5;
		int N = 1;
		byte D[][] = new byte[M][bufSize+1];
		byte C[][] = new byte[N][bufSize+1];
		int count = 0;
		encoder = new FEC(M, N);
		System.out.println("begin");
		try {
			while ((readSize = is.read(buf, 0, bufSize)) != -1) {
				System.arraycopy(buf, 0, D[count], 1, readSize);
				D[count][0] = (byte) blockNum;
				blockNum = (blockNum + 1) % maxn;
				count ++;
				if (count == M) {
					encoder.encode(D, C, bufSize, 1);
					C[0][0] = (byte) blockNum;
					blockNum = (blockNum + 1) % maxn;
					for (int i = 0; i < M; i++) {
						output.receive(D[i]);
					}
					for (int i = 0; i < N; i++) {
						output.receive(C[i]);
					}
					count = 0;
					tmpcounter++;
					System.out.println("send "+tmpcounter+" groups");
					sleep(10);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (count > 0) {
			for (int i = count; i < M; i++) {
				Arrays.fill(D[i], (byte)0);
				D[i][0] = (byte) blockNum;
				blockNum = (blockNum + 1) % maxn;
			}
			encoder.encode(D, C, bufSize, 1);
			C[0][0] = (byte) blockNum;
			blockNum = (blockNum + 1) % maxn;
			for (int i = 0; i < M; i++) {
				output.receive(D[i]);
			}
			for (int i = 0; i < N; i++) {
				output.receive(C[i]);
			}
			tmpcounter++;
			System.out.println("send "+tmpcounter+" groups");
		}
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		TestEncoder test = new TestEncoder();
		test.start();
	}
}
