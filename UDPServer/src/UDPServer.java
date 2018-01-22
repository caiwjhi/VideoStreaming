import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {  
      
//    private static final String SAVE_FILE_DIR = "D:/wenjing/teachingClass/saveFiles/";  
    private static String fileName = "default.mkv";  
    private static FileOutput output;
    
    public static void main(String[] args) {  
        System.out.println("Streaming server..."); 
        byte[] buf = new byte[UDPUtils.BUFFER_SIZE];  
        output = new FileOutput();
        DatagramPacket receiveDpk = null;  //���ձ���
        DatagramPacket sendDpk = null; // ���͵ı���
        DatagramSocket dsk = null;  
//        BufferedOutputStream bos = null;  
        try {  
              
            receiveDpk = new DatagramPacket(buf, buf.length);  
            dsk = new DatagramSocket(UDPUtils.PORT);  //��������ip�ͼ����˿�
//            bos = new BufferedOutputStream(new FileOutputStream(SAVE_FILE_DIR + fileName));  
            System.out.println("wait client ....");  
            System.out.println("server ip: " + InetAddress.getByName("localhost"));
            dsk.receive(receiveDpk);  
            System.out.println("after receive...");
            InetAddress receiveAddr = receiveDpk.getAddress();//���ؽ��ջ��ʹ����ݱ��ĵĻ����� IP ��ַ�� 
            int receivePort = receiveDpk.getPort();//���ؽ��ջ��͸����ݱ��ĵ�Զ�������˿ںš�
            int readSize = 0;  
            int readCount = 0;  
//            int flushSize = 0;  
            sendDpk = new DatagramPacket(buf, buf.length, receiveAddr, receivePort);//���ͱ��ģ����͵�ָ����ַ��ָ���˿�
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
//                  bos = new BufferedOutputStream(new FileOutputStream(SAVE_FILE_DIR + fileName)); //�����ļ���
                    output.open(fileName);
                    System.out.println("get file name : " + fileName);
                    System.out.println("client ip and port: " + receiveAddr + " " + receivePort);
                    sendDpk.setData(UDPUtils.successData, 0, UDPUtils.successData.length);
                    dsk.send(sendDpk);
                    System.out.println("after send success ");
                    receiveDpk.setData(buf,0, buf.length);  
                    System.out.println("receive count of "+ ( ++readCount ) +" !");  
                    dsk.receive(receiveDpk); 
                    continue;
                }
                System.out.println("receive file content..");
                receiveAddr = receiveDpk.getAddress();//���ؽ��ջ��ʹ����ݱ��ĵĻ����� IP ��ַ�� 
                receivePort = receiveDpk.getPort();//���ؽ��ջ��͸����ݱ��ĵ�Զ�������˿ںš�
                System.out.println("client ip and port: " + receiveAddr + " " + receivePort);
                //otherwise, get the file content  
//                bos.write(buf, 0, readSize);  
//                if(++flushSize % 1000 == 0){   
//                    flushSize = 0;  
//                    bos.flush();  
//                }
                output.receive(buf, readSize);
                //sendDpk.setData(UDPUtils.successData, 0, UDPUtils.successData.length);  
                //dsk.send(sendDpk);  
                //System.out.println("after send success ");  
                receiveDpk.setData(buf,0, buf.length);  
                System.out.println("receive count of "+ ( ++readCount ) +" !");  
                dsk.receive(receiveDpk);  
            }  
              
            // last flush   
//            bos.flush();  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally{  
            try {  
//                if(bos != null)  
//                    bos.close();  
            		output.close();
                if(dsk != null)  
                    dsk.close();  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
          
          
    }  
}  