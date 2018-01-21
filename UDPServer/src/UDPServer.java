import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.swing.filechooser.FileNameExtensionFilter;
 
/** 
 * Test file transfer of Server  
 * @author Bill  QQ:593890231 
 * @since v1.0 2014/09/21 
 *  
 */  
public class UDPServer {  
      
    private static final String SAVE_FILE_DIR = "D:/wenjing/teachingClass/saveFiles/";  
    private static String fileName = "default.mkv";  
    public static void main(String[] args) {  
    	System.out.println("Streaming server..."); 
        byte[] buf = new byte[UDPUtils.BUFFER_SIZE];  
         
        DatagramPacket receiveDpk = null;  //接收报文
        DatagramPacket sendDpk = null; // 发送的报文
        DatagramSocket dsk = null;  
        BufferedOutputStream bos = null;  
        try {  
              
            receiveDpk = new DatagramPacket(buf, buf.length);  
            dsk = new DatagramSocket(UDPUtils.PORT);  //服务器端ip和监听端口
            bos = new BufferedOutputStream(new FileOutputStream(SAVE_FILE_DIR + fileName));  
            System.out.println("wait client ....");  
            System.out.println("server ip: " + InetAddress.getByName("localhost"));
            dsk.receive(receiveDpk);  
            System.out.println("after receive...");
            InetAddress receiveAddr = receiveDpk.getAddress();//返回接收或发送此数据报文的机器的 IP 地址。 
            int receivePort = receiveDpk.getPort();//返回接收或发送该数据报文的远程主机端口号。
            int readSize = 0;  
            int readCount = 0;  
            int flushSize = 0;  
            sendDpk = new DatagramPacket(buf, buf.length, receiveAddr, receivePort);//发送报文，发送到指定地址的指定端口
            while((readSize = receiveDpk.getLength()) != 0){  
                // validate client send exit flag    
                if(UDPUtils.isEqualsByteArray(UDPUtils.exitData, buf, readSize)){  
                    System.out.println("server exit ...");  
                    // send exit flag   
                    sendDpk.setData(UDPUtils.exitData, 0, UDPUtils.exitData.length);  
                    dsk.send(sendDpk);  
                    break;  
                }
                if(UDPUtils.isEqualsByteArray(UDPUtils.fileInfo, buf, UDPUtils.fileInfo.length)){
                	//get the file name
                	System.out.println("get buf: " + new String(buf));
                	fileName = UDPUtils.getFileName(buf, UDPUtils.fileInfo.length);
                	System.out.println("get file name : " + fileName);
                	System.out.println("client ip and port: " + receiveAddr + " " + receivePort);
                	sendDpk.setData(UDPUtils.successData, 0, UDPUtils.successData.length);
                	dsk.send(sendDpk);
                	bos = new BufferedOutputStream(new FileOutputStream(SAVE_FILE_DIR + fileName)); //更新文件名
                	receiveDpk.setData(buf,0, buf.length);  
                    System.out.println("receive count of "+ ( ++readCount ) +" !");  
                    dsk.receive(receiveDpk); 
                	continue;
                }
                System.out.println("receive file content..");
                //otherwise, get the file content  
                bos.write(buf, 0, readSize);  
                if(++flushSize % 1000 == 0){   
                    flushSize = 0;  
                    bos.flush();  
                }  
                sendDpk.setData(UDPUtils.successData, 0, UDPUtils.successData.length);  
                dsk.send(sendDpk);  
                  
                receiveDpk.setData(buf,0, buf.length);  
                System.out.println("receive count of "+ ( ++readCount ) +" !");  
                dsk.receive(receiveDpk);  
            }  
              
            // last flush   
            bos.flush();  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally{  
            try {  
                if(bos != null)  
                    bos.close();  
                if(dsk != null)  
                    dsk.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
          
          
    }  
}  