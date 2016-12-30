package cn.suneony.gatherserver.twitter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import cn.suneony.gatherserver.command.Command;
import cn.suneony.gatherserver.filestorage.FileSystemStorage;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class StreamListener {
	// Twitter API auth
	private TwitterStream twitterStream = null;
	// status listener and processer
	private MyListener myListener = null;

	private Command command=null;

	public StreamListener(Command command) {
		this.command=command;
		ConfigurationBuilder twitterConfigurationBuilder = null;
		twitterConfigurationBuilder = new ConfigurationBuilder();
		twitterConfigurationBuilder.setDebugEnabled(true).setOAuthConsumerKey(this.command.getConsumerKey())
				.setOAuthConsumerSecret(this.command.getConsumerSecret()).setOAuthAccessToken(this.command.getAccessToken())
				.setOAuthAccessTokenSecret(this.command.getAccessTokenSecret());
		TwitterStreamFactory twitterStreamFactory = null;
		twitterStreamFactory = new TwitterStreamFactory(twitterConfigurationBuilder.build());
		twitterStream = twitterStreamFactory.getInstance();
		// Instance the status listener
		myListener = new MyListener();
	}
	/**
	 * listening the status and filtering through the keywords
	 * 
	 * @param keywords
	 *            the keywords used by filtering
	 * @return void
	 */
	public void listener() {
		if (this.command.getMode().equals(Command.MODE_TRACK)) {
			FilterQuery filterQuery = new FilterQuery();
			filterQuery.track(this.command.getKeywords().toArray(new String[this.command.getKeywords().size()]));
			twitterStream.addListener(myListener);
			twitterStream.filter(filterQuery);
		} else if (this.command.getMode().equals(Command.MODE_SAMPLE)) {
			twitterStream.addListener(myListener);
			twitterStream.sample();
		} else {
			// do search
		}
	}
	public void stop() {
		twitterStream.cleanUp();
		twitterStream.shutdown();
	}
	/**
	 * Inner class for status listening
	 */
	class MyListener implements StatusListener {
		private FileSystemStorage fileSystemStorage = null;
		public MyListener() {
			// Instance the FileSystemStorage
			fileSystemStorage = new FileSystemStorage(command.getUser(), command.getProject());
		}

		@Override
		public void onException(Exception arg0) {
		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice arg0) {
		}

		@Override
		public void onScrubGeo(long arg0, long arg1) {
		}

		@Override
		public void onStallWarning(StallWarning arg0) {
		}

		@Override
		public void onStatus(Status status) {
			fileSystemStorage.save(status);
		}
		@Override
		public void onTrackLimitationNotice(int arg0) {
		}
	}
}
