package cn.suneony.gatherserver.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;


import cn.suneony.gatherserver.config.Config;
import cn.suneony.gatherserver.twitter.StreamListener;


public class CommandReceiver {
	private static int PORT=0;
	private HashMap<String, StreamListener> ListenerMap=null;
	static{
		PORT=Integer.valueOf(Config.getProperty("COMMAND_PORT"));
	}
	
	public CommandReceiver(){
		ListenerMap=new HashMap<String,StreamListener>();
		
	}
//	private void restore(String commandLine){
//		String[] blocks=commandLine.split("[|]");
//		commandType=blocks[0];
//		commandMode=blocks[1];
//		consumerKey=blocks[2];
//		consumerSecret=blocks[3];
//		accessToken=blocks[4];
//		accessTokenSecret=blocks[5];
//		user=blocks[6];
//		project=blocks[7];
//		for(int i=8;i<blocks.length;i++){
//			trackingKeys.add(blocks[i]);
//		}
//		
//		
//	}
	private void receiver(Socket client){
		BufferedReader commandBufferedReader=null;
		BufferedReader tasksReader=null;
		BufferedWriter tasksWriter=null;
		BufferedWriter socketWriter=null;
		String receiveMsgString=null;
		int receiveMsgSize=0;
		byte[] receiveMsgBuff=new byte[4096];
		try {
			commandBufferedReader=new BufferedReader(new InputStreamReader(client.getInputStream(),"UTF-8"));
			Command receivedCommand=new Command(commandBufferedReader.readLine());
			if(receivedCommand.getType().equals(Command.TYPE_START)){
				StreamListener listener=new StreamListener(receivedCommand);
				listener.listener();
				ListenerMap.put(receivedCommand.getTaskId(), listener);
				tasksWriter=new BufferedWriter(new FileWriter(new File(Config.getProperty("TASKS_DIR")),true));
				tasksWriter.write(receivedCommand.toString());
				tasksWriter.newLine();
				tasksWriter.flush();
				tasksWriter.close();
				socketOutputStream.write("success");
			}
			//根据user+project删除一个任务。
			if(receivedCommand.getType().equals(Command.TYPE_STOP)){
				ArrayList<Command> commandList=new ArrayList<>();
				StreamListener listener=ListenerMap.get(receivedCommand.getTaskId());
				listener.stop();
				tasksReader=new BufferedReader(new FileReader(new File(Config.getProperty("TASKS_DIR"))));
				String existCommandString=null;
				while((existCommandString=tasksReader.readLine())!=null){
					Command existCommand=new Command(existCommandString);
					if(existCommand.getTaskId().equals(receivedCommand.getTaskId())){
						continue;
					}
					commandList.add(existCommand);
				}
				tasksWriter=new BufferedWriter(new FileWriter(new File(Config.getProperty("TASKS_DIR"))));
				for(Command item:commandList){
					tasksWriter.write(item.toString());
					tasksWriter.newLine();
				}
				tasksWriter.flush();
				tasksWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	@SuppressWarnings("resource")
	public void listener(){
		System.out.println("TCP Server Start");
		ServerSocket commandSocket=null;
		try {
			commandSocket=new ServerSocket(CommandReceiver.PORT);
		} catch (IOException e) {
			e.printStackTrace(); 
		}
		System.out.println("Listening Port: "+CommandReceiver.PORT);
		while(true){
			try {
				Socket client=commandSocket.accept();
				System.out.println("A client entering, the IP is: " + client.getInetAddress() + " and the Port is: "
						+ client.getPort());
				receiver(client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args){
		CommandReceiver receiver=new CommandReceiver();
		receiver.listener();
	}
}
