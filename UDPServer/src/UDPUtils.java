import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/** 
 * UDP transfer Utils   
 * 
 */
public class UDPUtils {
	private UDPUtils(){}
	
	/** transfer file byte buffer **/  
	public static final int BUFFER_SIZE = 50 * 1024;
	
	public static final int QUEUE_SIZE = 128;
	
	public static final int DATA_BLOCK = 10;
	
	public static final int ENCODED_BLOCK = 2;
	
	/** controller port  **/  
	public static final int PORT = 8889;  
	/*client port*/
    public static final int CLIENT_PORT = 8181;
	  
	/** mark transfer success **/  
	public static final byte[] successData = "success data mark".getBytes();  
	  
	/** mark transfer exit **/  
	public static final byte[] exitData = "exit data mark".getBytes();   
	  
	/*mark the file name message*/
	public static final byte[] fileInfo = "file name mark".getBytes();
	
	/*mark missing data */
	public static final byte[] missingNum = "missing mark".getBytes();
	/*
	public static void main(String[] args) {  
		byte[] b = new byte[]{1};  
		System.out.println(isEqualsByteArray(successData,b));  
	}*/  
	  
	/** 
	 * compare byteArray equest successData 
	 * @param compareBuf  
	 * @param buf 
	 * @return 
	 */  
	public static boolean isEqualsByteArray(byte[] compareBuf,byte[] buf){  
		if (buf == null || buf.length == 0)  
			return false;  
		  
		boolean flag = true;  
		if(buf.length == compareBuf.length){  
			for (int i = 0; i < buf.length; i++) {  
				if(buf[i] != compareBuf[i]){  
					flag = false;  
					break;  
				}  
			}  
		}else  
			return false;  
		return flag;  
	}  
	  
	/** 
	 * compare byteArray equest successData 
	 * @param compareBuf src 
	 * @param buf target 
	 * @return 
	 */  
	public static boolean isEqualsByteArray(byte[] compareBuf,byte[] buf,int len){  
		if (buf == null || buf.length == 0 || buf.length < len || compareBuf.length < len)  
			return false;  
		  
		boolean flag = true;  
		  
		int innerMinLen = Math.min(compareBuf.length, len);  
		//if(buf.length == compareBuf.length){  
			for (int i = 0; i < innerMinLen; i++) {    
				if(buf[i] != compareBuf[i]){  
					flag = false;  
					break;  
				}  
			}  
		//}else   
		//  return false;  
		return flag;  
	}  
	
	public static boolean hasMark(byte[] mark, byte[] buf) {
		String bufStr = new String(buf);
		String markStr = new String(mark);
		if(bufStr.contains(markStr))
			return true;
		else
			return false;
	}
	
	/*get the file name from buf 目前仅适用于处理commond命令数据包*/
	public static String getFileName(byte[] buf, int begin){
		/*String res = "";
		System.out.println("bgin " + begin);
		byte[] right = new byte[UDPUtils.BUFFER_SIZE];
		if(buf.length <= begin)
			return res;
		for(int i = begin; i < buf.length; i++){
			right[i-begin] = buf[i];
		}    	
		res = new String(right).trim();//鍘绘帀绌烘牸锛屾墍浠ワ紝鏂囦欢鍚嶄笉瑕佹湁绌烘牸
		System.out.println(res.length());
		return res;*/
		String string = new String(buf).trim();
		String[] strings = string.split("  ");
		return strings[1];
	}
	//get file size, 目前仅适用于处理commond命令数据包
	public static int getFileSize(byte[] buf){
		int ans = bytes2Int(buf, 2, 4);
		return ans;
	}
	//get the identity num of a buf,return a byte[] whose length is 2;
	public static int getFileNums(byte[] buf){
		int ans = bytes2Int(buf, 0, 2);
		return ans;
	}
	
	public static String getMD5(String fileName) {
		File f = null;
		FileInputStream fis = null;
		String value = null;
		try {
			f = new File(fileName);
			fis= new FileInputStream(f);
			MappedByteBuffer byteBuffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, f.length());  
			MessageDigest md5 = MessageDigest.getInstance("MD5");  
			md5.update(byteBuffer);  
			BigInteger bi = new BigInteger(1, md5.digest());  
			value = bi.toString(16);  
		} catch (Exception e) {  
			e.printStackTrace();  
		} finally {  
			if(fis != null) {  
				try {
					fis.close();  
				} catch (IOException e) {  
					e.printStackTrace();  
				}  
			}  
		}
		return value;
	}
	//get byte[] from int value, len is the length of byte[]
	public static byte[] int2Bytes(int value, int len) {  
        byte[] b = new byte[len];  
        for (int i = 0; i < len; i++) {  
            b[len - i - 1] = (byte)((value >> 8 * i) & 0xff);  
        }  
        return b;  
    }  
	//get int value from a byte[]
    public static int bytes2Int(byte[] b, int start, int len) {  
        int sum = 0;  
        int end = start + len;  
        for (int i = start; i < end; i++) {  
            int n = ((int)b[i]) & 0xff;  
            n <<= (--len) * 8;  
            sum += n;  
        }  
        return sum;  
    }  
  //java 合并两个byte数组  
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){  
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];  
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);  
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);  
        return byte_3;  
    }  
}  