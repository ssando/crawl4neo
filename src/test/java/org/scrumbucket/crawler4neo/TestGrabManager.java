package org.scrumbucket.crawler4neo;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class TestGrabManager {
	@Test
	public void happy() throws IOException, InterruptedException {
		GrabManager grabManager = new GrabManager(2, 15);
		grabManager.go(new URL("http://news.yahoo.com"));
		grabManager.write("target/testoutput/urllist.txt");
	}

}
