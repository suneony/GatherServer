package cn.suneony.gatherserver.packageutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;

import cn.suneony.gatherserver.config.Config;

public class PackageQueue{
	public static void push(PackageRecord record){
		FileOutputStream packageQueueOutputStream=null;
		BufferedWriter packageQueueWriter=null;
		//文件锁
		FileLock packageQueueLock=null;
		try {
			packageQueueOutputStream=new FileOutputStream(Config.getProperty("PACKAGE_QUEUE"),true);
			//获取文件锁，如果无法获取文件锁，则阻塞当前进程
			packageQueueLock=packageQueueOutputStream.getChannel().lock(0,Long.MAX_VALUE,false);
			//获取文件锁后的操作，将新的记录写入文件末尾
			packageQueueWriter=new BufferedWriter(new OutputStreamWriter(packageQueueOutputStream));
			packageQueueWriter.write(record.toString());
			packageQueueWriter.newLine();
			packageQueueWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(null!=packageQueueLock){
				try {
					//释放文件锁
					packageQueueLock.release();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(null!=packageQueueWriter){
				try {
					//关闭文件
					packageQueueWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static PackageRecord pop(){
		ArrayList<String> packageRecordStringList=new ArrayList<>();
		FileInputStream packageQueueInputStream=null;
		FileOutputStream packageQueueOutputStream=null;
		BufferedReader packageQueueReader=null;
		BufferedWriter packageQueueWriter=null;
		FileLock packageQueueLock=null;
		try {
			packageQueueInputStream=new FileInputStream(Config.getProperty("PACKAGE_QUEUE"));
			packageQueueLock=packageQueueInputStream.getChannel().lock(0,Long.MAX_VALUE,true);
			packageQueueReader=new BufferedReader(new InputStreamReader(packageQueueInputStream));
			String packageRecordString=null;
			try {
				while((packageRecordString=packageQueueReader.readLine())!=null){
					packageRecordStringList.add(packageRecordString);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch ( IOException e) {
			e.printStackTrace();
		}finally {
			if(null!=packageQueueLock){
				try {
					packageQueueLock.release();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(null!=packageQueueReader){
				try {
					packageQueueReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if(packageRecordStringList.size()==0)
			return null;
		//获取记录队列的第一个元素
		String firstPackageRecord=packageRecordStringList.get(0);
		//从队列中移除
		packageRecordStringList.remove(0);
		//将更新后队列写入原文件
		try {
			packageQueueOutputStream=new FileOutputStream("PackageQueue");
			packageQueueLock=packageQueueOutputStream.getChannel().lock(0,Long.MAX_VALUE,false);
			packageQueueWriter=new BufferedWriter(new OutputStreamWriter(packageQueueOutputStream));
			for(int i=0;i<packageRecordStringList.size();i++){
				try {
					packageQueueWriter.write(packageRecordStringList.get(i));
					packageQueueWriter.newLine();
					packageQueueWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(null!=packageQueueLock){
				try {
					packageQueueLock.release();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(null!=packageQueueWriter){
				try {
					packageQueueWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		PackageRecord packageRecordObj=new PackageRecord(firstPackageRecord);
		return packageRecordObj;
	}
	public static void main(String[] args){
		System.out.println("hello world");
	}
}
