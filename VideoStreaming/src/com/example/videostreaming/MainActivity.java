package com.example.videostreaming;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final int REC_REQUESTCODE = 1;
	
	/**服务器地址*/
    private String serverUrl="166.111.139.163";//"192.168.1.100";
    /**服务器端口*/
    private int serverPort=8889;
    /*是否选择文件*/
    private boolean selectedFile = false;
    private String filePath = "";
    private Button myBtn01, myBtn02;
    private TextView textShow;
    private String fileName;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		myBtn01=(Button)findViewById(R.id.button1);
		myBtn02 = (Button)findViewById(R.id.button2);
		Log.i("wenjing", "onCreate!!!");
		Log.v("", "test on create///");
		textShow =(TextView)findViewById(R.id.text);
		//开始选择文件
        myBtn01.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i("wenjing", "click button1!!!");
				//textShow.setText("on click button");
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("video/*;image/*");
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				startActivityForResult(intent, REC_REQUESTCODE);
				
			}
		});
        if(!selectedFile)
        	myBtn02.setEnabled(false);
        myBtn02.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, "开始传输视频", Toast.LENGTH_SHORT).show();
				myBtn01.setEnabled(false);
				UdpSendCommondThread th = new UdpSendCommondThread("file name mark " + fileName);
				new Thread(th).start();
				//不用线程，直接普通函数来发送
				//UdpSendCommond("file name mark " + fileName);
				//UdpSendFile();
			}
		});
        
	}
	//回调方法，从第二个页面回来的时候会执行这个方法
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.v("wenjing", "on activity result!!!");
    	//textShow.setText("on activity result!!!");
		if(resultCode == Activity.RESULT_OK && requestCode == REC_REQUESTCODE){
			Uri uri = intent.getData();
			String string = uri.toString();
			String path = "";
			File file = null;
			String a[]=new String[2];
			
			/*another method*/
			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
				path = getPath(this, uri);
				//textShow.setText(path);
				//Toast.makeText(this,path,Toast.LENGTH_SHORT).show();
			}else {//4.4以下下系统调用方法
				path = getRealPathFromURI(uri);
				//textShow.setText(path);
				//Toast.makeText(MainActivity.this, path+"222222", Toast.LENGTH_SHORT).show();
			}
			file = new File(path);
			if(file.exists()){
				selectedFile = true;
            	filePath = path;
            	//fileName = path.split("/")[-1];
            	fileName = path.substring(path.lastIndexOf('/')+1);
            	textShow.setText("selected file: " + fileName);
            	myBtn02.setEnabled(true);
			}else{
				myBtn02.setEnabled(true);
				Toast.makeText(MainActivity.this, "file not exist", Toast.LENGTH_SHORT).show();
			}
            //System.out.println("get file : " + fileSelected);
            Log.i("alertdialog","get file : " + filePath); 
               // i(TAG, "onActivityResult" + file.getAbsolutePath());
            //textShow.setText("selected file11:" + Environment.getExternalStorageDirectory() + "\n" + string +"\n" + Environment.getDataDirectory());
		}else{
			textShow.setText("respone not Ok " + requestCode + " " + resultCode);
		}
	}
	public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(null!=cursor&&cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }
	/**
	* 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
	*/
	@SuppressLint("NewApi")
	public String getPath(final Context context, final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	    // DocumentProvider
		if(isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				
				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{split[1]};
				
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}
	/**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

	public void myAlert(String mess){
	    new AlertDialog.Builder(this).setTitle("系统提示")//设置对话框标题   	  
	    .setMessage("test" + mess)//设置显示的内容  
	    .show();//在按键响应事件中显示此对话框  
	     
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	/**发送命令线程tcp*/
    class TcpSendCommondThread extends Thread{
    	private String commond;
    	public TcpSendCommondThread(String commond){
    		this.commond=commond;
    	}
    	public void run(){
    		//实例化Socket  
            try {
    			Socket socket=new Socket(serverUrl,serverPort);
    			PrintWriter out = new PrintWriter(socket.getOutputStream());
    			out.println(commond);
    			out.flush();
    		} catch (UnknownHostException e) {
    		} catch (IOException e) {
    		}  
    	}
    }
    
    /**发送文件线程,基于tcp*/
    class TcpSendFileThread extends Thread{	
    	private String username;
    	private String ipname;
    	private int port;
    	private byte byteBuffer[] = new byte[1024];
    	private OutputStream outsocket;	
    	private ByteArrayOutputStream myoutputstream;
    	
    	public TcpSendFileThread(ByteArrayOutputStream myoutputstream,String username,String ipname,int port){
    		this.myoutputstream = myoutputstream;
    		this.username=username;
    		this.ipname = ipname;
    		this.port=port;
            try {
    			myoutputstream.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	
        public void run() {
            try{
            	//将图像数据通过Socket发送出去
                Socket tempSocket = new Socket(ipname, port);
                outsocket = tempSocket.getOutputStream();
                //写入头部数据信息
            	String msg=java.net.URLEncoder.encode("PHONEVIDEO|"+username+"|","utf-8");
                byte[] buffer= msg.getBytes();
                outsocket.write(buffer);
                
                ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream.toByteArray());
                int amount;
                while ((amount = inputstream.read(byteBuffer)) != -1) {
                    outsocket.write(byteBuffer, 0, amount);
                }
                myoutputstream.flush();
                myoutputstream.close();
                tempSocket.close();                   
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class UdpSendCommondThread implements Runnable{
        private String commond;
        public UdpSendCommondThread(String commond){
        	this.commond = commond;
        	//Toast.makeText(MainActivity.this, "udp send commond thread", Toast.LENGTH_SHORT).show();
        }
        public void run(){
    		//实例化Socket  
        	//textShow.setText("run...");
            try {
            	Log.i("wenjing", "in udp send ..");
            	//Toast.makeText(MainActivity.this, commond, Toast.LENGTH_SHORT).show();
    			byte[] data = commond.getBytes();
    			//创建数据报
    	        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(serverUrl), serverPort);//发送报文到指定地址
    	        //创建DatagramSocket，实现数据发送和接收
    	        DatagramSocket socket = new DatagramSocket(46797);//this port ???
    	        //向服务器端发送数据报
    	        socket.send(packet);
    	        //Toast.makeText(MainActivity.this, "just after send commond", Toast.LENGTH_SHORT).show();
    	        //接收服务器响应数据
    	        Log.i("wenjing", "after send ..");
    	        byte[] data2 = new byte[1024];
    	        DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
    	        socket.receive(packet2);
    	        Log.i("wenjing", "after receive...");
    	        //Toast.makeText(MainActivity.this, "after receive", Toast.LENGTH_SHORT).show();
    	        String info = new String(data2, 0, packet2.getLength());
    	        System.out.println("我是客户端，服务器说："+info);
    	        //textShow.setText("发送命令后，服务器端回复：" + info);
    	        Log.i("wenjing", "发送命令后，服务器端回复22：" + info);
    	        socket.close();
    	        Log.i("wenjing", "before udp send file in this thread");
    	        UdpSendFile();
    		} catch (UnknownHostException e) {
    			Log.e("e", "exception");
    		} catch (IOException e) {
    			Log.e("e", "exception");
    		}  
    	}
    }
    public void UdpSendCommond(String commond){
    	
        try {
        	//Toast.makeText(MainActivity.this, commond, Toast.LENGTH_SHORT).show();
			byte[] data = commond.getBytes();
			//创建数据报
			
	        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(serverUrl), serverPort);//发送报文到指定地址
	        
	        //创建DatagramSocket，实现数据发送和接收
	        DatagramSocket socket = new DatagramSocket();
	       // textShow.setText("run...just before send");
	        //向服务器端发送数据报
	        socket.send(packet);
	      //  Toast.makeText(MainActivity.this, "just after send commond", Toast.LENGTH_SHORT).show();
	        //接收服务器响应数据
	        byte[] data2 = new byte[1024];
	        DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
	        socket.receive(packet2);
	      //  Toast.makeText(MainActivity.this, "after receive", Toast.LENGTH_SHORT).show();
	        String info = new String(data2, 0, packet2.getLength());
	        //System.out.println("我是客户端，服务器说："+info);
	       // textShow.setText("发送命令后，服务器端回复：" + info);
	        socket.close();
		} catch (UnknownHostException e) {
			Log.e("e", "exception");
		} catch (IOException e) {
			Log.e("e", "exception");
		}  
	
    }
    
    public void UdpSendFile(){
    	long startTime = System.currentTimeMillis();
    	//textShow.setText("in send file fun..");
        byte[] buf = new byte[UDPUtils.BUFFER_SIZE];
        byte[] receiveBuf = new byte[1];
        Log.i("wenjing", "in send file....");
        RandomAccessFile accessFile = null;
        DatagramPacket dpk = null;
        DatagramSocket dsk = null;
        DatagramPacket receiveDpk = null;//to receive data
        int readSize = -1;
        try {
            accessFile = new RandomAccessFile(filePath,"r");
//            dpk = new DatagramPacket(buf, buf.length,new InetSocketAddress(InetAddress.getByName("localhost"), UDPUtils.PORT + 1));
            dpk = new DatagramPacket(buf, buf.length,new InetSocketAddress(InetAddress.getByName(serverUrl), serverPort));
            dsk = new DatagramSocket(46797);//client listening port
            receiveDpk = new DatagramPacket(receiveBuf, receiveBuf.length);
            int sendCount = 0;
            while((readSize = accessFile.read(buf,0,buf.length)) != -1){
                System.out.println("readSize:"+readSize);
                dpk.setData(buf, 0, readSize);
                dsk.send(dpk);
                // wait server response
                {
                    while(true){
                        receiveDpk.setData(receiveBuf, 0, receiveBuf.length);
                        dsk.receive(receiveDpk);

                        // confirm server receive
                        if(!UDPUtils.isEqualsByteArray(UDPUtils.successData,receiveBuf,dpk.getLength())){
                            System.out.println("resend ...");
                            //textShow.setText("resend ...");
                            dpk.setData(buf, 0, readSize);
                            dsk.send(dpk);
                        }else
                            break;
                    }
                }

                System.out.println("send count of "+(++sendCount)+"!");
               // textShow.setText("send count of "+(++sendCount)+"!");
            }
            Toast.makeText(MainActivity.this, "finish send this file", Toast.LENGTH_SHORT).show();
         // send exit wait server response
            /*while(true){
                System.out.println("client send exit message ....");
                dpk.setData(UDPUtils.exitData,0,UDPUtils.exitData.length);
                dsk.send(dpk);

                receiveDpk.setData(receiveBuf,0,receiveBuf.length);
                dsk.receive(receiveDpk);
                // byte[] receiveData = dpk.getData();
                if(!UDPUtils.isEqualsByteArray(UDPUtils.exitData, receiveBuf, dpk.getLength())){
                    System.out.println("client Resend exit message ....");
                    dsk.send(dpk);
                }else
                    break;
            }*/
        }catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                if(accessFile != null)
                    accessFile.close();
                if(dsk != null)
                    dsk.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("time:"+(endTime - startTime));
    }
    
}
