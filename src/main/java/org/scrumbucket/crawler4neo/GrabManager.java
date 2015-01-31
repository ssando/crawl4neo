package org.scrumbucket.crawler4neo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GrabManager {
	public static final int THREAD_COUNT = 5;
	private static final long PAUSE_TIME = 1000;

	private Set<URL> masterList = new HashSet<>();
	private List<Future<GrabPage>> futures = new ArrayList<>();
	private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

	private String urlBase;

	private final int maxDepth;
	private final int maxUrls;

	public GrabManager(int maxDepth, int maxUrls) {
		this.maxDepth = maxDepth;
		this.maxUrls  = maxUrls;
	}

	public void go(URL start) throws IOException, InterruptedException {

		// stay within same site
		urlBase = start.toString().replaceAll("(.*//.*/).*", "$1");

		StopWatch stopWatch = new StopWatch();

		stopWatch.start();
		submitNewURL(start, 0);

		while (checkPageGrabs()) ;
		stopWatch.stop();

		System.out.println("Found " + masterList.size() + " urls");
		System.out.println("in " + stopWatch.getTime() / 1000 + " seconds");
	}

	private boolean checkPageGrabs() throws InterruptedException {
		Thread.sleep(PAUSE_TIME);
		Set<GrabPage> pageSet = new HashSet<>();
		Iterator<Future<GrabPage>> iterator = futures.iterator();

		while (iterator.hasNext()) {
			Future<GrabPage> future = iterator.next();
			if (future.isDone()) {
				iterator.remove();
				try {
					pageSet.add(future.get());
				} catch (InterruptedException e) {  // skip pages that load too slow
				} catch (ExecutionException e) {
				}
			}
		}

		for (GrabPage grabPage : pageSet) {
			addNewURLs(grabPage);
		}

		return (futures.size() > 0);
	}

	private void addNewURLs(GrabPage grabPage) {
		for (URL url : grabPage.getUrlList()) {
			if (url.toString().contains("#")) {
				try {
					url = new URL(StringUtils.substringBefore(url.toString(), "#"));
				} catch (MalformedURLException e) {
				}
			}

			submitNewURL(url, grabPage.getDepth() + 1);
		}
	}

	private void submitNewURL(URL url, int depth) {
		if (shouldVisit(url, depth)) {
			masterList.add(url);

			GrabPage grabPage = new GrabPage(url, depth);
			Future<GrabPage> future = executorService.submit(grabPage);
			futures.add(future);
		}
	}

	/**
	 * Redementary visitation filter.
	 */
	private boolean shouldVisit(URL url, int depth) {
		if (masterList.contains(url)) {
			return false;
		}
		if (!url.toString().startsWith(urlBase)) {
			return false;
		}
		if (url.toString().endsWith(".pdf")) {
			return false;
		}
		if (depth > maxDepth) {
			return false;
		}
		if (masterList.size() >= maxUrls) {
			return false;
		}
		return true;
	}

	public void write(String path) throws IOException {
		FileUtils.writeLines(new File(path), masterList);
	}
}
