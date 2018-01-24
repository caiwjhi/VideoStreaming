import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutput {
	private BufferedOutputStream bos;
	private String fileName;
	private long fileLength;
	private String fileDir;
	private MyThread receiveData;
	private Queue queue;
	private FEC encoder;
	
	public FileOutput(String dir) {
		fileDir = dir;
		queue = new Queue(UDPUtils.QUEUE_SIZE, UDPUtils.BUFFER_SIZE, 0);
		encoder = new FEC(5, 1);
	}
	
	public boolean open(String name, long length) {
		if (bos != null) {
			close();
			queue.clear();
		}
		fileLength = length;
		fileName = name;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(fileDir+fileName));
		} catch (FileNotFoundException e) {
			System.out.println(fileDir + fileName);
			e.printStackTrace();
		}
		if (bos != null) {
			receiveData = new MyThread(10, queue, encoder, bos, fileLength, fileDir+fileName);
			receiveData.start();
			return true;
		}
		return false;
	}
	
	public void receive(byte[] buf) {
//		System.out.print("insert:"+UDPUtils.bytes2Int(buf, 0, 2));
//		System.out.println(", data:"+buf[2]+','+buf[3]);
		queue.insert(buf);
	}
	
	public void close() {
		if(bos != null) {
			try {
				bos.flush();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		receiveData.stop = true;
	}

}

class MyThread extends Thread{
	private int waitTime;
	private int counter;
	private long fileLength;
	private long length;
	private Queue queue;
	private FEC encoder;
	private byte[][] data;
	private byte[][] encoded;
	private int dataBlocks;
	private int encodedBlocks;
	private BufferedOutputStream bos;
	public boolean stop;
	public int tmpcounter;
	public String fileName;
	
	public MyThread(int time, Queue q, FEC e, BufferedOutputStream b, long len, String name){
		waitTime = time;
		queue = q;
		encoder = e;
		dataBlocks = encoder.M;
		encodedBlocks = encoder.N;
		bos = b;
		fileLength = len;
		length = 0;
		stop = false;
		tmpcounter = 0;
		fileName = name;
		data = new byte[dataBlocks][UDPUtils.BUFFER_SIZE];
		encoded = new byte[encodedBlocks][UDPUtils.BUFFER_SIZE];
	}
	
	public void writeData(byte[][] buf, long len) {
//		System.out.print("write:");
		long count = 0;
		int dataLen = buf[0].length;
		for (int i = 0; i < buf.length; i++) {
			if (len - count < dataLen) {
				try {
					bos.write(buf[i], 0, (int) (len - count));
//					System.out.print(buf[i][0]+","+buf[i][1]+". ");
					bos.flush();
					bos.close();
					stop = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
			try {
				bos.write(buf[i], 0, dataLen);
//				System.out.print(buf[i][0]+","+buf[i][1]+". ");
			} catch (IOException e) {
				e.printStackTrace();
			}
			count += dataLen;
		}
		if (bos != null) {
			try {
				bos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		System.out.println();
	}
	
	public void write(int[] ready) {
		queue.output(data, encoded, ready, dataBlocks, encodedBlocks);
		boolean flag = encoder.decode(data, encoded, ready, UDPUtils.BUFFER_SIZE);
		if (!flag) {
			System.out.println("decode fail");
		}
		long len = dataBlocks * UDPUtils.BUFFER_SIZE;
		if (fileLength-length < dataBlocks * UDPUtils.BUFFER_SIZE) {
			len = fileLength-length;
		}
		writeData(data, len);
		length += len;
		tmpcounter++;
//    		System.out.println("receive "+tmpcounter+" groups, length="+length+", fileLength="+fileLength);
	}
	
	public void run() {
		System.out.println("Thread start");
		counter = 0;
		int[] ready = new int[dataBlocks+encodedBlocks];
		int num = 0;
		while (!stop) {
			num = queue.ready(dataBlocks, encodedBlocks);
			if (num >= dataBlocks) {
				write(ready);
				counter = 0;
			} else {
				if (num > 0)
					counter++;
				if (counter >= 10) {
					System.out.println("receive fail");
					write(ready);
					counter = 0;
					continue;
				}
				try {
					sleep(waitTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("length:"+length+", groups:"+tmpcounter);
		System.out.println("filename:"+fileName+", md5:"+UDPUtils.getMD5(fileName));
		System.out.println("Thread stop");
	}
}

