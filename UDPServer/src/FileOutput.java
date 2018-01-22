import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutput {
	private BufferedOutputStream bos;
	private String fileName;
	private static final String SAVE_FILE_DIR = "D:/wenjing/teachingClass/saveFiles/";
	private int flushSize;
	private MyThread receiveData;
	private Queue queue;
	private FEC encoder;
	
	public FileOutput() {
		queue = new Queue(10, UDPUtils.BUFFER_SIZE, 0, 0);
		encoder = new FEC(5, 1);
	}
	
	public boolean open(String name) {
		if (bos != null) {
			close();
			queue.clear();
		}
		fileName = name;
		flushSize = 0;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(SAVE_FILE_DIR + fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (bos != null) {
			
			receiveData = new MyThread(100, queue, encoder);
			receiveData.start();
	        return true;
		}
		return false;
	}
	
	public void receive(byte[] buf, int readSize) {
		queue.insert(buf);
	}
	
	public void write(byte[] buf, int readSize) {
		try {
			bos.write(buf, 0, readSize);
			if(++flushSize % 1000 == 0){
		        flushSize = 0;
		        bos.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		receiveData.stop();
	}

}

class MyThread extends Thread{
	private int waitTime;
	private Queue queue;
	private FEC encoder;
	private byte[][] data;
	private byte[][] encoded;
	private int dataBlocks;
	private int encodedBlocks;
	
    public MyThread(int time, Queue q, FEC e){
    		waitTime = time;
    		queue = q;
    		encoder = e;
    		dataBlocks = encoder.M;
    		encodedBlocks = encoder.N;
    		data = new byte[dataBlocks][UDPUtils.BUFFER_SIZE];
    		encoded = new byte[encodedBlocks][UDPUtils.BUFFER_SIZE];
    }
    
    public void write(int[] ready) {
    		queue.output(data, encoded, ready, dataBlocks, encodedBlocks);
    		encoder.decode(data, encoded, ready, UDPUtils.BUFFER_SIZE);
    }
    
    public void run() {
    		int counter = 0;
    		int[] ready = new int[dataBlocks+encodedBlocks];
    		while (true) {
    			if (queue.ready(dataBlocks, encodedBlocks)) {
    				write(ready);
    				counter = 0;
    			} else {
    				counter++;
    				if (counter >= 10) {
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
    }
}

