package org.scrumbucket.crawler4neo.data;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class Page {
    @GraphId
    private Long   id;

    private String url;

    public Page() { }

    public Page(String url) {
        this.url = url;
    }
}
