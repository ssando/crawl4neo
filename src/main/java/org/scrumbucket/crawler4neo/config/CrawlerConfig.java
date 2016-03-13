package org.scrumbucket.crawler4neo.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.scrumbucket.crawler4neo.Crawler4Neo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;

@Configuration
@EnableNeo4jRepositories(basePackageClasses = Crawler4Neo.class)
public class CrawlerConfig extends Neo4jConfiguration {

    CrawlerConfig() {
        setBasePackage("org.scrumbucket.crawler4neo");
    }

    @Bean
    GraphDatabaseService graphDatabaseService() {
        return new GraphDatabaseFactory().newEmbeddedDatabase("crawler4neo.db");
    }


}
