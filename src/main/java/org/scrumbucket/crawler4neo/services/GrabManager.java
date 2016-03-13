package org.scrumbucket.crawler4neo.services;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiPredicate;

public class GrabManager {
	private static final int THREAD_COUNT = 1;
	private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
	private static final long PAUSE_TIME = 1000;
	private final Set<URL> masterList = new HashSet<>();
	private final List<Future<GrabPage>> futures = new ArrayList<>();
	private final int maxUrls;
	private final BiPredicate<URL, Integer> shouldVisit;

	public GrabManager(int maxUrls, BiPredicate<URL, Integer> shouldVisit) {
		this.maxUrls = maxUrls;
		this.shouldVisit = shouldVisit;
	}

	public void go(URL start) throws IOException, InterruptedException {
		submitNewURL(start, 0);
		while (checkPageGrabs())
			;
		executorService.shutdown();
	}

	/**
	 * This method is charged with checking the status of all the threads
	 * and collecting their work effort.
	 *
	 * @return false = all the threads are done.
	 * @throws InterruptedException
	 */
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
				} catch (ExecutionException e) {
				}
			}
		}

		for (GrabPage grabPage : pageSet) {
			addNewURLs(grabPage);
		}

		return (futures.size() > 0);
	}

	/**
	 * Get the URLs from the grab page object.
	 * remove any anchor references
	 * save the url into the to-do list.
	 *
	 * @param grabPage object containing the URL list
	 */
	private void addNewURLs(GrabPage grabPage) {
		for (URL url : grabPage.getUrlList()) {
			if (url.toString().contains("#")) {
				try {
					url = new URL(StringUtils.substringBefore(url.toString(), "#"));
				} catch (MalformedURLException e) {
				}
			}

			testAndSubmitNewURL(url, grabPage.getDepth() + 1);
		}
	}

	/**
	 * Check if the URL passes muster and add it to the work list
	 *
	 * @param url
	 * @param depth
	 */
	private void testAndSubmitNewURL(URL url, int depth) {
		if (internalShouldVisit(url)
				&& shouldVisit.test(url, depth)) {  // ask the BiPredicate
			submitNewURL(url, depth);
		}
	}

	/**
	 * Do the work of actually adding a work item.
	 *
	 * @param url
	 * @param depth
	 */
	private void submitNewURL(URL url, int depth) {
		masterList.add(url);

		GrabPage grabPage = new GrabPage(url, depth);
		Future<GrabPage> future = executorService.submit(grabPage);
		futures.add(future);
	}


	/**
	 * Some things we need to control inside the manager itself.
	 * Like, do not visit the same page twice and stay within
	 * the maximum.
	 *
	 * @param url
	 * @return
	 */
	private boolean internalShouldVisit(URL url) {
		if (masterList.contains(url)) {
			return false;
		}
		if (masterList.size() >= maxUrls) {
			return false;
		}
		return true;
	}

	public Set<URL> getMasterList() {
		return masterList;
	}

}
