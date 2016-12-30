package cn.suneony.gatherserver.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	private static Properties properties=null;
	private static InputStream configStream=null;
	static{
		properties=new Properties();
		try {
			configStream=new FileInputStream("global.config");
			properties.load(configStream);
		} catch (FileNotFoundException e) {
			System.err.println("config file error");
		} catch (IOException e) {
			System.err.println("load config error");
		}finally {
			try {
				configStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static String getProperty(String key){
		return properties.getProperty(key);
	}
}
