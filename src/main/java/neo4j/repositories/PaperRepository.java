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
    
    @Query("MATCH (a:Author) - [:PUBLISH] ->(p:Paper) WHERE a.name IN {name} AND p.year > {startYear} AND p.year<{endYear} RETURN p.year as year, p.title as pubTitle")
    List<Map<String, Object>> getPubOnTimelineAndAuthor(@Param("name") String[] name, @Param("startYear") int startYear, @Param("endYear") int endYear);
    
    @Query("MATCH (a:Author) -[:PUBLISH]->(p:Paper) WHERE p.title =~ ('(?i).*'+{key}+'.*') RETURN a.name as year, p.title as pubTitle LIMIT {topK}")
    List<Map<String, Object>> getPubAuthorByKey(@Param("key") String key, @Param("topK") int topK);
    
    @Query("MATCH (n:Paper {journal: {journal} })<- [:PUBLISH]-(a) return n.volume as pubTitle, COLLECT(DISTINCT a.name) as author")
    List<Map<String, Object>> showAuthorToVolumeonJournal(@Param("journal") String journal);
    
    @Query("MATCH (n:Paper) where n.year > {startYear} AND n.year < {endYear} return n.year as pubTitle, COLLECT(n.title) as author")
    List<Map<String, Object>> categorizePaper(@Param("startYear") int startYear, @Param("endYear") int endYear);
    
    @Query("MATCH p =(:Author { name: {author1} })-[:PUBLISH*0..6]-(:Author { name: {author2} }) with nodes(p) as paths unwind paths as path match path-[:PUBLISH]->(a:Paper) return path.name as author, COLLECT(a.title) as pub")
    List<Map<String, Object>> authorConnectAuthor(@Param("author1") String author1, @Param("author2") String author2);
    
    @Query("MATCH (a:Author) -[:PUBLISH]->(p:Paper)  WHERE p.title =~ ('(?i).*'+{key}+'.*') return (a.name) as pubTitle, Length(COLLECT(p.title)) as year order by year desc LIMIT {topK}")
    List<Map<String, Object>> findExpertsByKey(@Param("key") String key, @Param("topK") int topK);
    
    @Query("MATCH (p:Paper) WHERE (p.title =~ ('(?i).*'+{key1}+'.*') OR p.title =~ ('(?i).*'+{key2}+'.*')) RETURN p.title as title LIMIT 10")
    List<Map<String, Object>> paperInterested(@Param("key1") String key1, @Param("key2") String key2);
    
    @Query("MATCH (p:Paper {journal: {key}})-[:CITE]->(p2:Paper) return p2.title as pubTitle, COLLECT(p.title) as author LIMIT 5")
    List<Map<String, Object>> showPaperRelationshipOnKey(@Param("key") String key);
    
    @Query("MATCH (p:Paper)-[:CITE]->(p2:Paper) return collect(Distinct p.title) as author, p2.title as pubTitle")
    List<Map<String, Object>> showPaperToPaper();
    
    @Query("MATCH (p1:Author)-[:CO]->(p2:Author) return collect(Distinct p1.name) as author, p2.name as pubTitle order by p2.name")
    List<Map<String, Object>> showPersonToPerson();
    
    @Query("MATCH (p:Author)-[:PUBLISH]->(p2:Paper) return collect(Distinct p.name) as author, p2.title as pubTitle LIMIT 30")
    List<Map<String, Object>> showPaperToPerson();
    
    @Query("MATCH (n:Paper) with n ORDER BY(n.year) where (TOINT(n.year) > {startYear} AND TOINT(n.year) < {endYear}) AND n.title =~ ('(?i).*'+{key}+'.*') AND n.journal = {journal} return n.year as pubTitle, COLLECT(n.title) as author")
    List<Map<String, Object>> catergorizePaperVersion2(@Param("startYear") int startYear, @Param("endYear") int endYear, @Param("journal") String journal, @Param("key") String key);
   
    @Query("MATCH (n:Author)-[:PUBLISH]-(p:Paper) RETURN Length(collect(p.title)) as l, collect([p.title, p.year]) as author, n.name as pubTitle order by l desc LIMIT 20")
    List<Map<String, Object>> showPastPubCard();
    
    @Query("MATCH (n:Paper)-[:CITE]-(p:Paper) RETURN Length(collect(n.title)) as l,collect(n.title) as citation, p.journal as journal, p.year as year, p.volume as volume, p.title as pubTitle order by l desc LIMIT 20")
    List<Map<String, Object>> showPubInfoCard();
   
    @Query("MATCH (a:Author)-[PUBLISH]-> (p:Paper) WHERE (p.title =~ ('(?i).*'+{key1}+'.*') OR p.title =~ ('(?i).*'+{key2}+'.*')) RETURN DISTINCT a.name as title , Length(COLLECT(p.title)) as count order by count desc LIMIT 10")
    List<Map<String, Object>> authorOfCollabrator(@Param("key1") String key1, @Param("key2") String key2);
    
    @Query("MATCH (p:Paper) where p.title =~ ('(?i).*'+{key}+'.*') RETURN 2 * 6371 * asin(sqrt(haversin(radians(TOFLOAT(p.latitude) - {latitude}))+ cos(radians(TOFLOAT(p.latitude)))*cos(radians({latitude}))* haversin(radians(TOFLOAT(p.longitude) - {longitude})))) AS dist , p.latitude as latitude, p.longitude as longitude order by dist limit 10")
    List<Map<String, Object>> authorOnGoogleMap(@Param("key") String key, @Param("latitude") double latitude, @Param("longitude") double longitude);

    @Query(" MATCH (p:Paper) where p.journal = {key} AND TOINT(p.year) >= {startYear} AND TOINT(p.year) <= {endYear} RETURN 2 * 6371 * asin(sqrt(haversin(radians(TOFLOAT(p.latitude) - 51.5))+ cos(radians(TOFLOAT(p.latitude)))*cos(radians(51.5))* haversin(radians(TOFLOAT(p.longitude) - -1.116)))) AS dist , p.latitude as latitude, p.longitude as longitude,p.year order by dist, latitude, longitude limit 10")
    List<Map<String, Object>> pubOnGoogleMap(@Param("key") String key, @Param("startYear") int startYear, @Param("endYear") int endYear);

}
