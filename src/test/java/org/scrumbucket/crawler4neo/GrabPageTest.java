package org.scrumbucket.crawler4neo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.scrumbucket.crawler4neo.services.GrabPage;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest(Jsoup.class)
public class GrabPageTest {

	@Test
	public void testGrabbing() throws Exception {

		/// create a real jsoup document using a test file
		String simpleHTML = new String(Files.readAllBytes(Paths.get(getClass().getResource("/simple.html").toURI())));
		Document document = Jsoup.parse(simpleHTML);

		// This is our pretend url.
		URL fakeUrl = new URL("http://example.com/dir1/page1");

		// Invoke powermock to take care of our static Jsoup.parse
		mockStatic(Jsoup.class);
		PowerMockito.when(Jsoup.parse(eq(fakeUrl), anyInt())).thenReturn(document);

		// Run the code to be tested
		GrabPage grabPage = new GrabPage(fakeUrl, 1);
		grabPage.call();

		// check the results.
		Set<URL> urls = grabPage.getUrlList();
		assertEquals(3, urls.size());
		assertTrue(urls.contains(new URL("http://example.com/dir1/link1")));
		assertTrue(urls.contains(new URL("http://example.com/link2")));
		assertTrue(urls.contains(new URL("http://example.com/relative")));
	}
}
