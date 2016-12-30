package cn.suneony.gatherserver.packageutils;

public class PackageRecord{
	private String userNameString;
	private String projectString;
	private String yearString;
	private String monthString;
	private String dayString;
	private String packageNameString;
	/**
	 * 初始化函数
	 * @param
	 * */
	public PackageRecord(String pUserNameString,String pProjectString,String pYearString,String pMonthString,String pDayString,String pPackageNameString){
		this.userNameString=pUserNameString;
		this.projectString=pProjectString;
		this.yearString=pYearString;
		this.monthString=pMonthString;
		this.dayString=pDayString;
		this.packageNameString=pPackageNameString;
		
	}
	/**
	 * @param 持久化存储字符串
	 * */
	public PackageRecord(String recordString){
		String[] recordBlocks=recordString.split("\t");
		this.userNameString=recordBlocks[0];
		this.projectString=recordBlocks[1];
		this.yearString=recordBlocks[2];
		this.monthString=recordBlocks[3];
		this.dayString=recordBlocks[4];
		this.packageNameString=recordBlocks[5];
	}
	/**
	 * 将对象转换为字符串
	 * */
	public String toString(){
		return userNameString+"\t"+projectString+"\t"+yearString+"\t"+monthString+"\t"+dayString+"\t"+packageNameString;
	}
}
