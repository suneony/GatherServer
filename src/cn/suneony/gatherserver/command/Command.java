package cn.suneony.gatherserver.command;

import java.util.ArrayList;

import org.apache.commons.lang.ObjectUtils.Null;

public class Command {
	public static final String TYPE_START = "start";
	public static final String TYPE_STOP = "stop";
	public static final String MODE_SAMPLE = "sample";
	public static final String MODE_TRACK = "track";
	public static final String MODE_SEARCH = "search";
	private String type = null;
	private String mode = null;
	private String user = null;
	private String project = null;
	private String consumerKey = null;
	private String consumerSecret = null;
	private String accessToken = null;
	private String accessTokenSecret = null;
	private ArrayList<String> keywords = null;

	public String getType() {
		return type;
	}

	public String getMode() {
		return mode;
	}

	public String getUser() {
		return user;
	}

	public String getProject() {
		return project;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public ArrayList<String> getKeywords() {
		return keywords;
	}
	public String getTaskId(){
		return this.user+"+"+this.project;
	}
	public Command(String line) {
		String[] commandBlocks = line.split("[|]");
		this.type = commandBlocks[0];
		this.user = commandBlocks[1];
		this.project = commandBlocks[2];
		if (this.type.equals(Command.TYPE_START)) {
			this.mode = commandBlocks[3];
			this.consumerKey = commandBlocks[4];
			this.consumerSecret = commandBlocks[5];
			this.accessToken = commandBlocks[6];
			this.accessTokenSecret = commandBlocks[7];
			// 如果是track任务，还需要加载关键词
			if (this.mode.equals(Command.MODE_TRACK)) {
				this.keywords = new ArrayList<String>();
				for (int i = 0; i < commandBlocks.length; i++) {
					this.keywords.add(commandBlocks[i]);
				}
			}
		}
	}

	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(this.type).append("|").append(this.user).append("|").append(this.project);
		if(this.type.equals(Command.TYPE_STOP)){
			return sb.toString();
		}
		sb.append("|").append(this.mode).append("|").append(this.consumerKey).append("|").append(this.consumerSecret).append("|").append(this.accessToken).append("|").append(this.accessTokenSecret);
		if(this.type.equals(Command.TYPE_START)&&this.mode.equals(Command.MODE_SAMPLE)){
			return sb.toString();
		}
		if(this.type.equals(Command.TYPE_START)&&(this.mode.equals(Command.MODE_TRACK)||this.mode.equals(Command.MODE_SEARCH))){
			for(int i=0;i<this.keywords.size();i++){
				sb.append("|").append(this.keywords.get(i));
			}
			return sb.toString();
		}
		return null;
	}
}
