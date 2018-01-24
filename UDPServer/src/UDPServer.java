import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {  
	  
    private static final String SAVE_FILE_DIR = "D:/wenjing/teachingClass/saveFiles/";  
	private static String fileName = "default.mkv";  
	private static FileOutput output;
	private static int fileSize = 0;
	
	public static void main(String[] args) {  
		System.out.println("Streaming server..."); 
		byte[] buf = new byte[UDPUtils.BUFFER_SIZE + 2];  
		output = new FileOutput(SAVE_FILE_DIR);
		DatagramPacket receiveDpk = null;  //接收报文
		DatagramPacket sendDpk = null; // 发送的报文
		DatagramPacket sendMissDpk = null;//发送给客户端的标识缺失的报文
		byte[] missingData = new byte[1024];
		DatagramSocket dsk = null;
		int requireNum = -1;
		try {
			receiveDpk = new DatagramPacket(buf, buf.length);  
			dsk = new DatagramSocket(UDPUtils.PORT);  //服务器端ip和监听端口
			System.out.println("wait client ....");  
			System.out.println("server ip: " + InetAddress.getByName("localhost"));
			dsk.receive(receiveDpk);  
			System.out.println("after receive...");
			InetAddress receiveAddr = receiveDpk.getAddress();//返回接收或发送此数据报文的机器的 IP 地址。 
			int receivePort = receiveDpk.getPort();//返回接收或发送该数据报文的远程主机端口号。
			int readSize = 0;  
			int readCount = 0;  
			byte[] nums = new byte[2];//for resend , missing identity num
			sendDpk = new DatagramPacket(buf, buf.length, receiveAddr, receivePort);//发送报文，发送到指定地址的指定端口
			sendMissDpk = new DatagramPacket(missingData, missingData.length, receiveAddr, UDPUtils.CLIENT_PORT);
			while((readSize = receiveDpk.getLength()) != 0){  
				// validate client send exit flag    
				if(UDPUtils.isEqualsByteArray(UDPUtils.exitData, buf, readSize)){  
					System.out.println("server exit ...");  
					// send exit flag   
					sendDpk.setData(UDPUtils.exitData, 0, UDPUtils.exitData.length);  
					dsk.send(sendDpk);  
					break;  
				}
				if(UDPUtils.hasMark(UDPUtils.fileInfo, buf)){
					//get the file name
					System.out.println("get buf: " + new String(buf));
					fileName = UDPUtils.getFileName(buf, UDPUtils.fileInfo.length);
					System.out.println("get file name : " + fileName);
					fileSize = UDPUtils.getFileSize(buf);
					System.out.println("file size: " + fileSize);
					System.out.println("nums " + new String(UDPUtils.getFileNums(buf)));
					output.open(fileName, fileSize); // open thread to save data
					System.out.println("client ip and port: " + receiveAddr + " " + receivePort);
					sendDpk.setData(UDPUtils.successData, 0, UDPUtils.successData.length);
					dsk.send(sendDpk);
					System.out.println("after send success ");
					readCount = UDPUtils.bytes2Int(buf, 0, 2);
					System.out.println("receive count of "+ ( readCount ) +" !");  
					receiveDpk.setData(buf,0, buf.length);  
					dsk.receive(receiveDpk); 
					continue;
				}
				System.out.println("receive file content..");
				receiveAddr = receiveDpk.getAddress();//返回接收或发送此数据报文的机器的 IP 地址。 
				receivePort = receiveDpk.getPort();//返回接收或发送该数据报文的远程主机端口号。
				System.out.println("client ip and port: " + receiveAddr + " " + receivePort);
				//otherwise, get the file content  
				
				if ((requireNum = output.missing()) != -1) {
					//send ack
					System.out.println("missing and resend " + requireNum);
					nums = UDPUtils.int2Bytes(requireNum, 2);
					missingData = UDPUtils.byteMerger(UDPUtils.missingNum, nums);
					sendMissDpk.setData(missingData, 0, missingData.length);
					dsk.send(sendMissDpk);
					System.out.println("after send missing success ");
					int missingCount = 0; // 计算要求重发后有多少收到的不是想要的缺失数据。
					while(missingCount <= 5){
						readCount = UDPUtils.bytes2Int(buf, 0, 2);
						System.out.println("receive count of "+ ( readCount ) +" !");  
						if(readCount == requireNum){//缺失的已经补上，则继续接收其他包
							output.receive(buf); 
							break;
						}else{
							if(missingCount >= 5){
								System.out.println("!!!! resend  again !!!!");
								dsk.send(sendMissDpk);
								missingCount = 0;
								//break;
							}
						}
						output.receive(buf); //收到其他的包则正常处理；
						receiveDpk.setData(buf,0, buf.length);  
						dsk.receive(receiveDpk);
						missingCount++;
					}
				}
				readCount = UDPUtils.bytes2Int(buf, 0, 2);
				System.out.println("receive count of "+ ( readCount ) +" !");  
				//System.out.println("buf size " +buf.length);
				output.receive(buf); // save data to queue, in which buf[0~1] is the block number
				//sendDpk.setData(UDPUtils.successData, 0, UDPUtils.successData.length);  
				//dsk.send(sendDpk);  
				//System.out.println("after send success ");  
				receiveDpk.setData(buf,0, buf.length); 
				dsk.receive(receiveDpk);
			}
		} catch (Exception e) {  
			e.printStackTrace();  
		} finally{  
			try {
				if (output != null)
					output.close(); 
				if(dsk != null)  
					dsk.close();  
			} catch (Exception e) {  
				e.printStackTrace();  
			}  
		}
	}  
}  