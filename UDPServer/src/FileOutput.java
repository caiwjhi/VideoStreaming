import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutput {
	private BufferedOutputStream bos;
	private String fileName;
	private static final String SAVE_FILE_DIR = "D:/wenjing/teachingClass/saveFiles/";
	private int flushSize;
	
	public FileOutput() {
	}
	
	public boolean open(String name) {
		close();
		fileName = name;
		flushSize = 0;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(SAVE_FILE_DIR + fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (bos != null) {
			MyThread receiveData = new MyThread();
			receiveData.start();
	        return true;
		}
		return false;
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
	}
	
	public static void main(String[] args)  {
        MyThread thread = new MyThread();
        thread.start();
    }

}

class MyThread extends Thread{
    public MyThread(){
    }
    
    public void run() {
    	Queue q = new Queue(10, UDPUtils.BUFFER_SIZE, 0, 0);
    }
}

