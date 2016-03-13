package org.scrumbucket.crawler4neo.data;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepository extends GraphRepository<Page> {
}
