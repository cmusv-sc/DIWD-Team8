package startingPoint.KG_DBLP;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import neo4j.domain.*;
import neo4j.repositories.*;
import neo4j.services.DatasetService;
import neo4j.services.PaperService;

@Configuration
@Import(App.class)
@RestController("/")
public class KnowledgeGraph extends WebMvcConfigurerAdapter {
	
	public static DatabaseOperation dbOperation;
	public static String user;
	public static HashMap<String, Map<String, Object>> cachedHistory = new HashMap<>();
	
    public static void main(String[] args) throws IOException, SQLException {
    	dbOperation = new DatabaseOperation();
        dbOperation.createTable();
        SpringApplication.run(KnowledgeGraph.class, args);
    }
    
    @Autowired
    PaperService paperService;
    @Autowired
    DatasetService datasetService;

    @Autowired PaperRepository paperRepository;
    @Autowired DatasetRepository datasetRepository;

    @RequestMapping("/graph")
    public Map<String, Object> graph(@RequestParam(value = "limit",required = false) Integer limit) {
    	return paperService.graph(limit == null ? 100 : limit);
    }
    
    @RequestMapping("/graphTest")
    public String graphTest(@RequestParam(value = "limit",required = false) Integer limit) {
    	Map<String, Object> map = paperService.graphAlc(limit == null ? 200 : limit);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    @RequestMapping("/userLogin")
    public String login(@RequestParam(value = "username",required = true) String userName, @RequestParam(value = "password",required = true) String pw) throws SQLException, IOException {
    	Map<String, Object> map = new HashMap<>();
    	user = userName;
    	boolean result = dbOperation.isUserExist(userName, pw);
    	if(result) {
    		map.put("username", userName);
    		String searchHistory = dbOperation.getUserSearchHistory(userName);
    		map.put("visited", searchHistory);
    		String followers = dbOperation.getFollowers(userName);
    		map.put("followers", followers);
    		String notification = dbOperation.getNotification(userName);
    		map.put("notification", notification);
    		String allUsers = dbOperation.getAllUsers(userName);
    		map.put("users", allUsers);
    	}
    	else {
    		map.put("username", "null");
    	}
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    @RequestMapping("/userSignup")
    public String signup(@RequestParam(value = "username",required = true) String userName, @RequestParam(value = "password",required = true) String pw) throws SQLException, IOException {
    	Map<String, Object> map = new HashMap<>();
    	dbOperation.insertUser(userName, pw);
    	map.put("success", true);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    @RequestMapping("/publishPaper")
    public String publishPaper(@RequestParam(value = "title",required = true) String title) throws SQLException, IOException {
    	Map<String, Object> map = new HashMap<>();
    	dbOperation.insertPublicationAndNotification(user, title);
    	map.put("success", true);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    @RequestMapping("/follow")
    public String followUser(@RequestParam(value = "name",required = true) String followee) throws SQLException, IOException {
    	Map<String, Object> map = new HashMap<>();
    	dbOperation.insertFollower(user, followee);
    	map.put("status", "success");
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    @RequestMapping("/profile")
    public String getProfile(@RequestParam(value = "name",required = true) String userName) throws SQLException, IOException {
    	Map<String, Object> map = new HashMap<>();
    	String followers = dbOperation.getFollowers(userName);
    	String publications = dbOperation.getPublications(userName);
    	map.put("name", userName);
    	map.put("follower", followers);
    	map.put("publication", publications);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //get author's coauthor's coauthor
    @RequestMapping("/Query1Test")
    public String query1(@RequestParam(value = "name1",required = false) String name) throws SQLException {
    	name = name.replaceAll("\\+", " ");
    	Map<String, Object> map;
    	if(cachedHistory.get("Q1"+name) != null) {
    		map = cachedHistory.get("Q1"+name);
    	}
    	else {
    		map = paperService.getCoAuthorCoAuthorgraphAlcStr(name == null ? "" : name);
    		cachedHistory.put("Q1"+name, map);
    	}
    	String json = "";
    	dbOperation.insertHistory(user, name);
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    @RequestMapping("/Query2Test")
    public String query2(@RequestParam(value = "name",required = false) String name) throws SQLException {
    	System.out.println("Query2Test");
    	dbOperation.insertHistory(user, name);
    	name = name.replaceAll("\\+", " ");
    	Map<String, Object> map = paperService.getCoAuthorgraphAlcStr(name == null ? "" : name);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //show publication on timeline and a set of author
    @RequestMapping("/Query3Test")
    public String query3(@RequestParam(value = "name",required = false) String name, @RequestParam(value = "startYear",required = false) Integer startYear, @RequestParam(value = "endYear",required = false) Integer endYear) {
    	System.out.println("Query3Test");
    	name = name.replaceAll("\\+", " ");
    	name = name.replaceAll("%2C+", ",");
    	String[] names = name.split(",");
    	Map<String, Object> map = paperService.getPubOnTimelineAndAuthor(names, startYear == null ? 2000 : startYear, endYear == null ? 2015:endYear );
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //given key word, list top k related publications together with author
    @RequestMapping("/Query4Test")
    public String query4(@RequestParam(value = "key",required = false) String key, @RequestParam(value = "top",required = false) Integer top) {
    	System.out.println("Query4Test");
    	Map<String, Object> map = paperService.getPubAuthorByKey(key, top);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //given journal, list all authors based on the volume
    @RequestMapping("/Query5Test")
    public String query5(@RequestParam(value = "journal",required = false) String journal) {
    	System.out.println("Query5Test");
    	journal = journal.replaceAll("\\+", " ");
    	System.out.println("journal is " + journal);
    	Map<String, Object> map = paperService.showAuthorToVolumeonJournal(journal);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //Categorize the research papers (given time period)
    @RequestMapping("/Query6Test")
    public String query6(@RequestParam(value = "startYear",required = false) Integer startYear, @RequestParam(value = "endYear",required = false) Integer endYear) {
    	System.out.println("Query6Test");
    	Map<String, Object> map = paperService.catergorizePaper(startYear, endYear);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //Author can connect to author
    @RequestMapping("/Query7Test")
    public String query7(@RequestParam(value = "author1",required = false) String author1, @RequestParam(value = "author2",required = false) String author2) throws SQLException {
    	System.out.println("Query7Test");
    	author1 = author1.replaceAll("\\+", " ");
    	author2 = author2.replaceAll("\\+", " ");
    	dbOperation.insertHistory(user, author1);
    	dbOperation.insertHistory(user, author2);
    	Map<String, Object> map = paperService.authorConnectAuthor(author1, author2);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //Given some keywords, search for researchers who are experts in the field.
    @RequestMapping("/Query8Test")
    public String query8(@RequestParam(value = "key",required = false) String key, @RequestParam(value = "top",required = false) Integer top) {
    	System.out.println("Query8Test");
    	Map<String, Object> map = paperService.findExpertsByKey(key, top);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //Counted as 2 requirements) Given some keywords, return a collection of papers that the reader may be interested in reading (sorted by relevance).
    @RequestMapping("/Query9Test")
    public String query9(@RequestParam(value = "key1",required = false) String key1, @RequestParam(value = "key2",required = false) String key2) {
    	System.out.println("Query9Test");
    	Map<String, Object> map = paperService.paperInterested(key1, key2);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //Given keyword, return a collection of papers, how a graph of their relationships (e.g., citation)
    @RequestMapping("/Query10Test")
    public String query10(@RequestParam(value = "key",required = false) String key) {
    	System.out.println("Query10Test");
    	key = key.replaceAll("\\+", " ");
    	System.out.println("journal is " + key);
    	Map<String, Object> map = paperService.showPaperRelationshipOnKey(key);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
  //Generate a Paper-Paper network, showing their citation relationships.
    @RequestMapping("/Query11Test")
    public String query11() {
    	System.out.println("Query11Test");
    	Map<String, Object> map = paperService.showPaperToPaper();
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
  //Generate a Person-Person network, showing their collaboration relationships.
    @RequestMapping("/Query12Test")
    public String query12() {
    	System.out.println("Query12Test");
    	Map<String, Object> map = paperService.showPersonToPerson();
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //Generate a Paper-Person network, showing their authoring relationships.
    @RequestMapping("/Query13Test")
    public String query13() {
    	System.out.println("Query13Test");
    	Map<String, Object> map = paperService.showPaperToPerson();
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
  //Categorize the research papers (given time period, publication channels, keywords)
    @RequestMapping("/Query14Test")
    public String query14(@RequestParam(value = "channel",required = false) String journal, @RequestParam(value = "key",required = false) String key,
    		@RequestParam(value = "startYear",required = false) Integer startYear, @RequestParam(value = "endYear",required = false) Integer endYear) {
    	System.out.println("Query14Test");
    	journal = journal.replaceAll("\\+", " ");
    	Map<String, Object> map = paperService.catergorizePaperVersion2(startYear, endYear,journal, key);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
  //In a publication network, click on an author, show a knowledge card summarizing her past publication status.
    @RequestMapping("/Query15Test")
    public String query15() {
    	System.out.println("Query15Test");
    	Map<String, Object> map = paperService.showPastPubCard();
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //In a publication network, click on a paper, show a knowledge card summarizing its publication information and citation data.
    @RequestMapping("/Query16Test")
    public String query16() {
    	System.out.println("Query16Test");
    	Map<String, Object> map = paperService.showPubInfoCard();
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //Given some keywords, search for researchers that may like to be your collaborators.
    @RequestMapping("/Query17Test")
    public String query17(@RequestParam(value = "key1",required = false) String key1, @RequestParam(value = "key2",required = false) String key2) {
    	System.out.println("Query17Test");
    	Map<String, Object> map = paperService.authorOfCollabrator(key1, key2);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
  //Given some geographical area (e.g., country) and some keywords, generate a graph on Google map the publications on the topic, whose authors come 
  //from the geographical area (at least one or more authors).
    @RequestMapping("/Query18Test")
    public String query18(@RequestParam(value = "key",required = false) String key, @RequestParam(value = "latitude",required = false) double latitude, @RequestParam(value = "longitude",required = false) double longitude) {
    	System.out.println("Query18Test");
    	Map<String, Object> map = paperService.authorOnGoogleMap(key, latitude, longitude);
    	map.put("latitude", latitude);
    	map.put("longitude", longitude);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    //Given a publication channel name (journal or conference) and a time frame, showcase in Google map the publications distribution.
    @RequestMapping("/Query19Test")
    public String query19(@RequestParam(value = "channel",required = false) String key, @RequestParam(value = "startYear",required = false) int startYear, @RequestParam(value = "endYear",required = false) int endYear) {
    	System.out.println("Query19Test");
    	key = key.replaceAll("\\+", " ");
    	Map<String, Object> map = paperService.pubOnGoogleMap(key, startYear, endYear);
    	map.put("latitude", 51.5);
    	map.put("longitude", -1.116);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    @RequestMapping("/graphUserDataset")
    public String graphUserDataset(@RequestParam(value = "limit",required = false) Integer limit) {
    	Map<String, Object> map = datasetService.graphAlc(limit == null ? 100 : limit);
    	String json = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		//convert map to JSON string
    		json = mapper.writeValueAsString(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    @RequestMapping("/getPapers")
    public Collection<Paper> getPapers(String title) {
    	return paperRepository.findByTitleContaining(title);
    	//return paperRepository.findByTitleLike(title);
    }
    
    @RequestMapping("/getPaper")
    public Paper getPaper(String title) {
    	//return movieRepository.findByTitleContaining(title);
    	return paperRepository.findByTitle(title);
    }

}
