package org.scrumbucket.crawler4neo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestGrabManager {

	static final int SIZE = 5;
	@MockitoAnnotations.Mock ExecutorService mockExecutor;
	@InjectMocks GrabManager grabManager = new GrabManager(2, SIZE);

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@Ignore
	public void happy() throws IOException, InterruptedException {
		GrabManager grabManager = new GrabManager(2, 15);
		grabManager.go(new URL("http://news.yahoo.com"));
		grabManager.write("target/testoutput/urllist.txt");
	}

	@Test
	public void testShouldVisit() throws IOException, InterruptedException, ExecutionException {
		Set<URL> urls = new HashSet<>();
		urls.add(new URL("http://ignore.com"));
		urls.add(new URL("http://example.com/normal"));
		urls.add(new URL("http://example.com/without#anchor"));
		urls.add(new URL("http://example.com/nopdfs.pdf"));
		urls.add(new URL("http://example.com/extra1"));
		urls.add(new URL("http://example.com/extra2"));
		urls.add(new URL("http://example.com/extra3"));

		// mock out grab page so we can control urls returned
		GrabPage mockGrab = mock(GrabPage.class);
		when(mockGrab.getUrlList()).thenReturn(urls);

		// mock a future object to return our grabber
		Future<GrabPage> mockFuture = mock(Future.class);
		when(mockFuture.isDone()).thenReturn(true);
		when(mockFuture.get()).thenReturn(mockGrab);

		// return our future
		when(mockExecutor.submit(any(Callable.class))).thenReturn(mockFuture);

		grabManager.go(new URL("http://example.com"));

		Set<URL> visited = grabManager.getMasterList();
		assertEquals(SIZE, visited.size());
		assertTrue(visited.contains(new URL("http://example.com/normal")));
		assertTrue(visited.contains(new URL("http://example.com/without")));
		assertFalse(visited.contains(new URL("http://example.com/without#anchor")));
		assertFalse(visited.contains(new URL("http://example.com/nopdfs.pdf")));
	}

}
