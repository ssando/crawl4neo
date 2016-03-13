package org.scrumbucket.crawler4neo;

import org.neo4j.io.fs.FileUtils;
import org.scrumbucket.crawler4neo.services.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class Crawler4Neo implements CommandLineRunner {

    @Autowired
    private CrawlerService crawlerService;

    @Override
    public void run(String... strings) throws Exception {
        crawlerService.loadDatabase("http://news.yahoo.com", "trump");
    }

    public static void main(String[] args) throws Exception {
        FileUtils.deleteRecursively(new File("crawler4neo.db"));

        SpringApplication.run(Crawler4Neo.class, args);
    }
}
