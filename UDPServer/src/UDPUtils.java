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
    
    /*get the file name from buf*/
    public static String getFileName(byte[] buf, int begin){
    	String res = "";
    	System.out.println("bgin " + begin);
    	byte[] right = new byte[UDPUtils.BUFFER_SIZE];
    	if(buf.length <= begin)
    		return res;
    	for(int i = begin; i < buf.length; i++){
    		right[i-begin] = buf[i];
    	}    	
    	res = new String(right).trim();//去掉空格，所以，文件名不要有空格
    	System.out.println(res.length());
    	return res;
    }
}  