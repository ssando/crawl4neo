package org.scrumbucket.crawler4neo.services;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Processes a single page in a thread.  By design this class does not
 * talk back to the manager, such as checking for previous URLs.  We are
 * seeking the cleanest, simpliest design.
 */
public class GrabPage implements Callable<GrabPage> {
	static final int TIMEOUT = 60000;   // one minute

	private URL url;
	private int depth;
	private Set<URL> urlList = new HashSet<>();

	public GrabPage(URL url, int depth) {
		this.url = url;
		this.depth = depth;
	}

	@Override
	public GrabPage call() throws Exception {
		Document document = null;
		System.out.println("Visiting (" + depth + "): " + url.toString());
		document = Jsoup.parse(url, TIMEOUT);
		processLinks(document.select("a[href]"));
		return this;
	}

	private void processLinks(Elements links) {
		links.stream().
				filter(link ->
				{
					String href = link.attr("href");
					return (StringUtils.isNotBlank(href)) && !href.startsWith("#");
				}).
				forEach(link -> {
					try {
						String href = link.attr("href");
						URL nextUrl = new URL(url, href);
						urlList.add(nextUrl);
					} catch (MalformedURLException e) { // ignore bad urls
					}
				});
	}

	public Set<URL> getUrlList() {
		return urlList;
	}

	public int getDepth() {
		return depth;
	}
}
