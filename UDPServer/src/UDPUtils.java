/** 
 * UDP transfer Utils   
 * 
 */  
public class UDPUtils {  
    private UDPUtils(){}  
      
    /** transfer file byte buffer **/  
    public static final int BUFFER_SIZE = 50 * 1024;  
      
    /** controller port  **/  
    public static final int PORT = 8889;  
      
    /** mark transfer success **/  
    public static final byte[] successData = "success data mark".getBytes();  
      
    /** mark transfer exit **/  
    public static final byte[] exitData = "exit data mark".getBytes();   
      
    /*mark the file name message*/
    public static final byte[] fileInfo = "file name mark".getBytes();
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
    
    /*get the file name from buf；仅用于处理收到的commond包，不用于处理文件内容*/
    public static String getFileName(byte[] buf, int begin){
    	/*String res = "";
    	System.out.println("bgin " + begin);
    	byte[] right = new byte[UDPUtils.BUFFER_SIZE];
    	if(buf.length <= begin)
    		return res;
    	for(int i = begin; i < buf.length; i++){
    		right[i-begin] = buf[i];
    	}    	
    	res = new String(right).trim();//去掉空格，所以，文件名不要有空格
    	System.out.println(res.length());
    	return res;*/
    	String string = new String(buf).trim();
    	String[] strings = string.split("  ");
    	return strings[3];
    }
    //根据收到的buf计算文件大小，返回int值，单位是Byte,字节，仅用于处理收到的commond包，不用于处理文件内容
    public static int getFileSize(byte[] buf){
    	String string = new String(buf).trim();
    	System.out.println("string len " + string + " " +string.length());
    	String[] strings = string.split("  ");
    	return Integer.valueOf(strings[1]);
    }
    //得到buf的编号，目前仅用于处理commond数据buf
    public static byte[] getFileNums(byte[] buf){
    	byte[] ans = new byte[2];
    	ans[0] = buf[0];
    	ans[1] = buf[1];
    	return ans;
    }
    
}  