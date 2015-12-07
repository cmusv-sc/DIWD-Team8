package neo4j.repositories;

import neo4j.domain.Paper;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public interface PaperRepository extends GraphRepository<Paper> {
    Paper findByTitle(@Param("title") String title);

    @Query("MATCH (p:Paper) WHERE p.title =~ ('(?i).*'+{title}+'.*') RETURN p")
    Collection<Paper> findByTitleContaining(@Param("title") String title);

    @Query("MATCH (p:Paper)<-[:PUBLISH]-(a:Author) RETURN p.title as paper, collect(a.name) as cast LIMIT {limit}")
    List<Map<String, Object>> graph(@Param("limit") int limit);

    @Query("MATCH (p:Author {name:{name}})-[:CO]-(c:Author) RETURN distinct c.name as coAuthor, collect(p.name) as author")
    List<Map<String, Object>> findCoAuthor(@Param("name") String name);
    
    @Query("MATCH (a:Author {name:{name1}})-[:CO]-(Author),(Author)-[:CO]-(coAuthors) RETURN coAuthors.name as paper, collect(Author.name) as cast")
    List<Map<String, Object>> findCoAuthorCoAuthor(@Param("name1") String name);
    
    @Query("MATCH (p:Paper), (a:Author) WHERE a.name IN {name} AND p.year > {startYear} AND p.year<{endYear} AND a- [:PUBLISH] -> (p) RETURN p.year as year, p.title as pubTitle")
    List<Map<String, Object>> getPubOnTimelineAndAuthor(@Param("name") String[] name, @Param("startYear") int startYear, @Param("endYear") int endYear);
    
    @Query("MATCH (p:Paper), (a:Author)  WHERE p.title =~ ('(?i).*'+{key}+'.*') AND a - [:PUBLISH] -> (p) RETURN a.name as year, p.title as pubTitle LIMIT {topK}")
    List<Map<String, Object>> getPubAuthorByKey(@Param("key") String key, @Param("topK") int topK);
    
    @Query("MATCH (n:Paper {journal: {journal} })<- [:PUBLISH]-(a) return n.volume as pubTitle, COLLECT(DISTINCT a.name) as author")
    List<Map<String, Object>> showAuthorToVolumeonJournal(@Param("journal") String journal);
    
    @Query("MATCH (n:Paper) where n.year > {startYear} AND n.year < {endYear} return n.year as pubTitle, COLLECT(n.title) as author")
    List<Map<String, Object>> categorizePaper(@Param("startYear") int startYear, @Param("endYear") int endYear);
    
    @Query("MATCH p =(:Author { name: {author1} })-[:PUBLISH*0..6]-(:Author { name: {author2} }) with nodes(p) as paths unwind paths as path match path-[:PUBLISH]->(a:Paper) return path.name as author, COLLECT(a.title) as pub")
    List<Map<String, Object>> authorConnectAuthor(@Param("author1") String author1, @Param("author2") String author2);
}
