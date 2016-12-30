package cn.suneony.gatherserver.filestorage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import cn.suneony.gatherserver.config.Config;
import twitter4j.JSONObject;
import twitter4j.Status;
import twitter4j.json.DataObjectFactory;

@SuppressWarnings("deprecation")
public class FileSystemStorage {
	private String lastDataString = null;
	private GZIPOutputStream statusGZIPOutputStream = null;
	private String userString=null;
	private String projectString=null;
	//private MySQLRecord statusRecord=null;
	public FileSystemStorage(String user,String project) {
		//记录上一个小时数据的时间
		lastDataString = "2000-01-01-01";
		userString=user;
		projectString=project;
		//Instance the MySQLRecord
		//statusRecord=new MySQLRecord();
	}
	private String getURLType(String longURL){
		//该链接是一个pdf文件
		if(longURL.endsWith(".pdf")){
			return "PDF";
		}
		//该链接是一个youtube视频
		if(longURL.startsWith("https://www.youtube.com")){
			return "YOUTUBE";
		}
		//该链接是一个网页
		if(longURL.endsWith(".html")){
			return "HTML";
		}
		//未知链接
		return "UNKNOWN";
	}
	private String getLongURL(String shortURL){
		HttpURLConnection conn=null;
		try {
			conn = (HttpURLConnection) new URL(shortURL)  
			        .openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  
        conn.setInstanceFollowRedirects(false);  
        conn.setConnectTimeout(5000); 
        conn.setReadTimeout(5000);
        return conn.getHeaderField("Location"); 
	}
	/**
	 * 对原始JSONObject进行更新，通过短链接获取长链接，并解析链接类型：PDF|HTML|YOUTUBE|UNKNOWN
	 * @param stringOfStatus 原始的JSONObject字符串表示
	 * @return 更新后的JSONObject字符串表示
	 * */
	private String updateJSONObject(String stringOfStatus){
		net.sf.json.JSONObject statusObject=net.sf.json.JSONObject.fromObject(stringOfStatus);
		net.sf.json.JSONObject entityObject=statusObject.getJSONObject("entities");
		net.sf.json.JSONArray urlArray=entityObject.getJSONArray("urls");
		if(urlArray.size()>0){
			for(int i=0;i<urlArray.size();i++){
				String shortURL=urlArray.getJSONObject(i).getString("expanded_url");
				String longURL=this.getLongURL(shortURL);
				if(longURL==null){
					longURL=shortURL;
				}
				String urlType=getURLType(longURL);
				urlArray.getJSONObject(i).put("absolute_url", longURL);
				urlArray.getJSONObject(i).put("url_type", urlType);
			}
			entityObject.put("urls", urlArray);
			statusObject.put("entities", entityObject);
			return statusObject.toString();
		}
		return stringOfStatus;
	}
	public void save(Status status) {
		String currentDateString = (new SimpleDateFormat("yyyy-MM-dd-HH")).format(status.getCreatedAt());
		String stringOfStatus=DataObjectFactory.getRawJSON(status);
		stringOfStatus=this.updateJSONObject(stringOfStatus);
		//防止出现时间错乱，因为TwitterSteam并不完全是按照时间流的，有可能出现后一条的时间早于前一条
		if (!currentDateString.equals(lastDataString)&&(currentDateString.compareTo(lastDataString)>0)) {
			if (statusGZIPOutputStream != null) {
				try {
					statusGZIPOutputStream.flush();
					statusGZIPOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//将刚刚保存的文件的文件路径及其文件名存入到数据库中
				//statusRecord.addRecord(userString,projectString,lastDataString);
			}
			lastDataString=currentDateString;
			//将博文发布时间按照年，月，日进行拆分，并依据此生成文件存放路径
			String[] timeBlock = currentDateString.split("-");
			String path = Config.getProperty("PACKAGE_DIR") + File.separator + this.userString+ File.separator +this.projectString+ File.separator +timeBlock[0] + File.separator + timeBlock[1] + File.separator + timeBlock[2] + File.separator;
			File pathFile=new File(path);
			if(!pathFile.exists())
				pathFile.mkdirs();
			try {
				//创建新的ZIP文件，开始写入博文数据
				statusGZIPOutputStream = new GZIPOutputStream(new BufferedOutputStream(
						new FileOutputStream(path + "statuses." + currentDateString + ".zip", true)));
				statusGZIPOutputStream.write(stringOfStatus.getBytes());
				statusGZIPOutputStream.write("\r\n".getBytes());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			try {
				//向当前文件继续写入
				statusGZIPOutputStream.write(stringOfStatus.getBytes());
				this.statusGZIPOutputStream.write("\r\n".getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
