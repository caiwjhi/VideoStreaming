import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutput {
	private BufferedOutputStream bos;
	private String fileName;
	private long fileLength;
	private int maxBlockNum;
	private String fileDir;
	private MyThread receiveData;
	private Queue queue;
	private FEC encoder;
	
	public FileOutput(String dir) {
		fileDir = dir;
		queue = new Queue(UDPUtils.QUEUE_SIZE, UDPUtils.BUFFER_SIZE, 1);
		encoder = new FEC(UDPUtils.DATA_BLOCK, UDPUtils.ENCODED_BLOCK);
	}
	
	private void getBlockNum() {
		int blocks = (int) (fileLength / UDPUtils.BUFFER_SIZE);
		int groups = blocks / UDPUtils.DATA_BLOCK;
		if (blocks % UDPUtils.DATA_BLOCK != 0)
			groups += 1;
		maxBlockNum = groups * (UDPUtils.DATA_BLOCK+UDPUtils.ENCODED_BLOCK);
	}
	
	public boolean open(String name, long length) {
		if (bos != null) {
			close();
			queue.clear();
		}
		fileLength = length;
		getBlockNum();
		fileName = name;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(fileDir+fileName));
		} catch (FileNotFoundException e) {
			System.out.println(fileDir + fileName);
			e.printStackTrace();
		}
		if (bos != null) {
			receiveData = new MyThread(10, queue, encoder, bos, fileLength, maxBlockNum, fileDir+fileName);
			receiveData.start();
			return true;
		}
		return false;
	}
	
	public void receive(byte[] buf) {
		queue.insert(buf);
	}
	
	public int missing() {
		int tmp = receiveData.missing;
		if (tmp == 0 || tmp == -1)
			return tmp;
		if (tmp > maxBlockNum)
			tmp = -1;
		receiveData.missing = -1;
		return tmp;
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
	private int maxBlockNum;
	private int sleepTime = 1;
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
	public int missing;
	
	public MyThread(int time, Queue q, FEC e, BufferedOutputStream b, long len, int maxNum, String name){
		maxBlockNum = maxNum;
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
		missing = -1;
		data = new byte[dataBlocks][UDPUtils.BUFFER_SIZE];
		encoded = new byte[encodedBlocks][UDPUtils.BUFFER_SIZE];
	}
	
	public void writeData(byte[][] buf, long len) {
		long count = 0;
		int dataLen = buf[0].length;
		for (int i = 0; i < buf.length; i++) {
			if (len - count < dataLen) {
				try {
					bos.write(buf[i], 0, (int) (len - count));
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
	}
	
	public void write(int[] ready) {
		queue.output(data, encoded, ready, dataBlocks, encodedBlocks);
		encoder.decode(data, encoded, ready, UDPUtils.BUFFER_SIZE);
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
			num = queue.ready(dataBlocks, encodedBlocks, 0);
			if (num >= dataBlocks) {
				write(ready);
				counter = 0;
			} else {
				if (queue.expected > maxBlockNum)
					break;
				if (num > 0)
					counter++;
				if (counter > 10) {
					missing = queue.getFirstMissing(dataBlocks, encodedBlocks);
					counter = 0;
					System.out.println("receive fail, missing:"+missing);
//					queue.printQueue(30);
//					break;
				}
				try {
					sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		missing = 0;
		System.out.println("length:"+length+", groups:"+tmpcounter);
		System.out.println("filename:"+fileName+", md5:"+UDPUtils.getMD5(fileName));
		System.out.println("Thread stop");
	}
}

