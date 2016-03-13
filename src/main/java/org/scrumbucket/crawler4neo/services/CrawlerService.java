package org.scrumbucket.crawler4neo.services;


import org.scrumbucket.crawler4neo.data.Page;
import org.scrumbucket.crawler4neo.data.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.function.BiPredicate;

@Service
public class CrawlerService {

    public static final int MAX_URLS = 15;
    public static final int MAX_DEPTH = 5;

    @Autowired
    private PageRepository pageRepository;


    public void loadDatabase(String startingUrl, String pathHas) throws IOException, InterruptedException {
        URL start = new URL(startingUrl);

        BiPredicate<URL, Integer>
                shouldVisit = (url, depth) -> url.getHost().equals(start.getHost());
        shouldVisit = shouldVisit.and( (url, depth) -> depth < MAX_DEPTH);
        shouldVisit = shouldVisit.and( (url, depth) -> url.getPath().contains(pathHas));

        GrabManager grabManager = new GrabManager(MAX_URLS, shouldVisit);

        grabManager.go(start);

        System.out.println("Found " + grabManager.getMasterList().size() + " urls");

        for( URL url : grabManager.getMasterList()) {
            pageRepository.save(new Page(url.getPath()));
        }

    }
}
