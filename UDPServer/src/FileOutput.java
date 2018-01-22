import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutput {
	private BufferedOutputStream bos;
	private String fileName;
	private static final String SAVE_FILE_DIR = "D:/wenjing/teachingClass/saveFiles/";
	private int flushSize;
	
	public FileOutput(String name) {
		fileName = name;
		flushSize = 0;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(SAVE_FILE_DIR + fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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

}
