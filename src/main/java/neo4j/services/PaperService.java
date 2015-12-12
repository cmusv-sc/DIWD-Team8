package neo4j.services;

import neo4j.repositories.PaperRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class PaperService {

    @Autowired PaperRepository paperRepository;

    private Map<String, Object> toD3Format(Iterator<Map<String, Object>> result) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> rels = new ArrayList<Map<String,Object>>();
        int i = 0;
        int target = 0;
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            nodes.add(map("name",row.get("paper"),"label","paper"));
            i++;
            target = i;
            for (Object name : (Collection) row.get("cast")) {
                Map<String, Object> author = map("name", name,"label","author");
                int source = nodes.indexOf(author);
                if (source == -1) {
                    nodes.add(author);
                    source = i++;
                }
                rels.add(map("source",source,"target",target));
            }
        }
        return map("nodes", nodes, "links", rels);
    }
    
    private Map<String, Object> toAlcFormat(Iterator<Map<String, Object>> result, String relationship) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String,Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 1;
        int target = 0;
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            nodes.add(map6("id", i, "title",row.get("pubTitle"),"label", row.get("pubTitle"), "cluster", "1", "value", 2, "group", "paper"));
            System.out.println("volume is " + row.get("pubTitle"));
            target = i++;
            for (Object name : (Collection) row.get("author")) {
            	System.out.println("name is " + name);
                Map<String, Object> author = map5("title", 
                		name,"label", name, "cluster", "2", "value", 1, "group", "author");
                int source = 0;
                for (int j = 0; j < nodes.size(); j++) {
                	if (nodes.get(j).get("title").equals(name)) {
                		source = (int) nodes.get(j).get("id");
                		break;
                	} 
                }
                if (source == 0) {
                	author.put("id", i);
                    source = i;
                    i++;
                    nodes.add(author);
                }

                rels.add(map3("from", source, "to", target, "title", relationship));
            }
        }
        System.out.println("nodes are " + nodes);
        System.out.println("edges are " + rels);
        return map("nodes", nodes, "edges", rels);
    }
    
    private Map<String, Object> getCoauthorCoAuthortoAlcFormat(Iterator<Map<String, Object>> coAuthor, Iterator<Map<String, Object>> result) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String,Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 1;
        int target = 0;
        while (coAuthor.hasNext()) {
            Map<String, Object> row = coAuthor.next();
            nodes.add(map6("id", i, "title",row.get("coAuthor"),"label", row.get("coAuthor"), "cluster", "1", "value", 2, "group", "paper"));
            target = i++;
            for (Object name : (Collection) row.get("author")) {
                Map<String, Object> author = map5("title", 
                		name,"label", name, "cluster", "2", "value", 1, "group", "author");
                int source = 0;
                for (int j = 0; j < nodes.size(); j++) {
                	if (nodes.get(j).get("title").equals(name)) {
                		source = (int) nodes.get(j).get("id");
                		break;
                	} 
                }
                if (source == 0) {
                	author.put("id", i);
                    source = i;
                    i++;
                    nodes.add(author);
                }

                rels.add(map3("from", source, "to", target, "title", "PUBLISH"));
            }
        }
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            nodes.add(map6("id", i, "title",row.get("paper"),"label", row.get("paper"), "cluster", "1", "value", 1, "group", "author"));
            target = i++;
            for (Object name : (Collection) row.get("cast")) {
                Map<String, Object> author = map5("title", 
                		name,"label", name, "cluster", "2", "value", 1, "group", "author");
                int source = 0;
                for (int j = 0; j < nodes.size(); j++) {
                	if (nodes.get(j).get("title").equals(name)) {
                		source = (int) nodes.get(j).get("id");
                		break;
                	} 
                }
                if (source == 0) {
                	author.put("id", i);
                    source = i;
                    i++;
                    nodes.add(author);
                }

                rels.add(map3("from", source, "to", target, "title", "PUBLISH"));
            }
        }
        return map("nodes", nodes, "edges", rels);
    }
    
    private Map<String, Object> getCoAuthortoAlcFormat(Iterator<Map<String, Object>> result) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String,Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 1;
        int target = 0;
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            //nodes.add(map5("title", row.get("paper"),"label", "author", "cluster", "2", "value", 1, "group", "paper"));
            nodes.add(map6("id", i, "title",row.get("coAuthor"),"label", row.get("coAuthor"), "cluster", "1", "value", 1, "group", "coAuthor"));
            System.out.println("row.getPaper is " + row.get("paper"));
            target = i++;
            for (Object name : (Collection) row.get("author")) {
            	System.out.println("row.getAuthor is " + row.get("author"));
                Map<String, Object> author = map5("title", 
                		name,"label", name, "cluster", "2", "value", 1, "group", "author");
                int source = 0;
                for (int j = 0; j < nodes.size(); j++) {
                	if (nodes.get(j).get("title").equals(name)) {
                		System.out.println("title is " + nodes.get(j).get("title"));
                		System.out.println("name is " + name);
                		source = (int) nodes.get(j).get("id");
                		break;
                	} 
                }
                if (source == 0) {
                	author.put("id", i);
                    source = i;
                    i++;
                    nodes.add(author);
                }

                rels.add(map3("from", source, "to", target, "title", "COAUTHOR"));
            }
        }
        return map("nodes", nodes, "edges", rels);
    }
    
    private Map<String, Object> getPubOnTimelineAndAuthortoAlcFormat(Iterator<Map<String, Object>> result) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String,Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 1;
        int target = 0;
        String lastYear = "";
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            if(!lastYear.equals(row.get("year").toString()) || lastYear.equals("")) {
            	nodes.add(map6("id", i, "title",row.get("year"),"label", row.get("year"), "cluster", "1", "value", 1, "group", "year"));
                lastYear = row.get("year").toString();
            	target = i++;
            }
            Object name = row.get("pubTitle");
            Map<String, Object> author = map5("title", 
            		name,"label", name, "cluster", "2", "value", 1, "group", "pubTitle");
            int source = 0;
            for (int j = 0; j < nodes.size(); j++) {
            	if (nodes.get(j).get("title").equals(name)) {
            		source = (int) nodes.get(j).get("id");
            		break;
            	} 
            }
            if (source == 0) {
            	author.put("id", i);
                source = i;
                i++;
                nodes.add(author);
            }

            rels.add(map3("from", source, "to", target, "title", "PUBLISH"));
        }
        return map("nodes", nodes, "edges", rels);
    }
    
    private Map<String, Object> authorConnectAuthortoAlcFormat(Iterator<Map<String, Object>> result) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String,Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 1;
        ArrayList<Set<Object>> pubLists = new ArrayList<>();
        ArrayList<Object> authorList = new ArrayList<>();
        int count = 0;
        while(result.hasNext()) {
        	Map<String, Object> row = result.next();
        	Object name = row.get("author");
        	nodes.add(map6("id", i, "title",name,"label", name, "cluster", "1", "value", 1, "group", "author"));
        	i++;
        	authorList.add(name);
        	pubLists.add(new HashSet<>());
        	for (Object pub : (Collection) row.get("pub")) {
        		pubLists.get(count).add(pub);
        	}
        	count++;
        }
        
        int target = i;
        for(int j=0; j<pubLists.size(); j++) {
        	Set<Object> cur = pubLists.get(j);
        	for(int t=j+1; t<pubLists.size(); t++) {
        		Set<Object> cmp = pubLists.get(t);
        		Set<Object> tmp = new HashSet<>(cmp);
        		if(tmp.retainAll(cur)) {
        			Iterator<Object> tmpIter = tmp.iterator();
        			while(tmpIter.hasNext()) {
        				Object pubName = tmpIter.next();
        				int des = target;
        				for (int x = 0; x < nodes.size(); x++) {
        	            	if (nodes.get(x).get("title").equals(pubName)) {
        	            		des = (int) nodes.get(x).get("id");
        	            		break;
        	            	} 
        	            }
        				if(des == target) {
        					nodes.add(map6("id", des, "title",pubName,"label", pubName, "cluster", "1", "value", 1, "group", "pubTitle"));
        					target++;
        				}
            			rels.add(map3("from", j+1, "to", des, "title", "PUBLISH"));
                    	rels.add(map3("from", t+1, "to", des, "title", "PUBLISH"));
                    	target++;
        			}
        		}
        	}
        }
        return map("nodes", nodes, "edges", rels);
    }
    
    private Map<String, Object> paperInterestedToAlcFormat(Iterator<Map<String, Object>> result) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String,Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 1;
        int target = 0;
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            nodes.add(map6("id", i, "title",row.get("title"),"label", row.get("title"), "cluster", "1", "value", 1, "group", "publication"));
        	target = i++;
        }
        return map("nodes", nodes, "edges", rels);
    }
    
    private Map<String, Object> showCardToAlcFormat(Iterator<Map<String, Object>> result) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String,Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 1;
        int target = 0;
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            nodes.add(map6("id", i, "title",row.get("author"),"label", row.get("pubTitle"), "cluster", "1", "value", 1, "group", "publication"));
        	target = i++;
        }
        return map("nodes", nodes, "edges", rels);
    }
    
    private Map<String, Object> showPubCardToAlcFormat(Iterator<Map<String, Object>> result) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String,Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 1;
        int target = 0;
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            StringBuilder pubInfo = new StringBuilder();
            pubInfo.append("Journal is: " + row.get("journal") + " ");
            pubInfo.append("Volume is: " + row.get("volume") + " ");
            pubInfo.append("Year is: " + row.get("year"));
            nodes.add(map6("id", i, "title",pubInfo,"label", row.get("pubTitle"), "cluster", "1", "value", 1, "group", "publication"));
        	target = i++;
        	
        	Map<String, Object> citation = map5("title", 
            		row.get("citation"),"label", "citation", "cluster", "2", "value", 1, "group", "pubTitle");
        	citation.put("id", i);
        	int source = i;
            i++;
            nodes.add(citation);
            rels.add(map3("from", source, "to", target, "title", "CITE"));
        }
        return map("nodes", nodes, "edges", rels);
    }
    
    private Map<String, Object> authorOnGoogleMaptoAlcFormat(Iterator<Map<String, Object>> result) {
    	Map<String, Object> map = new HashMap<>();
    	Object[] value = new Object[10];
    	int i=0;
    	while(result.hasNext()) {
    		Map<String, Object> row = result.next();
    		if(row.get("dist") == null || Double.parseDouble(row.get("dist").toString())>500) {
    			continue;
    		}
    		double[] loc = { Double.parseDouble(row.get("latitude").toString()), Double.parseDouble(row.get("longitude").toString())};
    		value[i] = loc;
    		i++;
    	}
    	map.put("loc", value);
    	return map;
    }

    private Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> result = new HashMap<String,Object>(2);
        result.put(key1,value1);
        result.put(key2,value2);
        return result;
    }
    
    private Map<String, Object> map3(String key1, Object value1, String key2, Object value2, 
    		String key3, Object value3) {
        Map<String, Object> result = new HashMap<String,Object>(3);
        result.put(key1,value1);
        result.put(key2,value2);
        result.put(key3, value3);
        return result;
    }
    
    private Map<String, Object> map5(String key1, Object value1, String key2, Object value2, 
    		String key3, Object value3, String key4, Object value4, String key5, Object value5) {
        Map<String, Object> result = new HashMap<String,Object>(5);
        result.put(key1,value1);
        result.put(key2,value2);
        result.put(key3, value3);
        result.put(key4, value4);
        result.put(key5, value5);
        return result;
    }
    
    private Map<String, Object> map6(String key1, Object value1, String key2, Object value2, 
    		String key3, Object value3, String key4, Object value4, String key5, Object value5,
    		String key6, Object value6) {
        Map<String, Object> result = new HashMap<String,Object>(6);
        result.put(key1,value1);
        result.put(key2,value2);
        result.put(key3, value3);
        result.put(key4, value4);
        result.put(key5, value5);
        result.put(key6, value6);
        return result;
    }

    public Map<String, Object> graph(int limit) {
        Iterator<Map<String, Object>> result = paperRepository.graph(limit).iterator();
        return toD3Format(result);
    }
    
    public Map<String, Object> graphAlc(int limit) {
        Iterator<Map<String, Object>> result = paperRepository.graph(limit).iterator();
        return toAlcFormat(result, "PUBLISH");
    }
    
    public Map<String, Object> getCoAuthorgraphAlcStr(String s) {
        Iterator<Map<String, Object>> result = paperRepository.findCoAuthor(s).iterator();
        return getCoAuthortoAlcFormat(result);
    }
    
    public Map<String, Object> getCoAuthorCoAuthorgraphAlcStr(String s) {
    	Iterator<Map<String, Object>> coAuthor = paperRepository.findCoAuthor(s).iterator();
        Iterator<Map<String, Object>> result = paperRepository.findCoAuthorCoAuthor(s).iterator();
        return getCoauthorCoAuthortoAlcFormat(coAuthor, result);
    }
    
    public Map<String, Object> getPubOnTimelineAndAuthor(String[] s, int startYear, int endYear) {
        Iterator<Map<String, Object>> result = paperRepository.getPubOnTimelineAndAuthor(s, startYear, endYear).iterator();
        return getPubOnTimelineAndAuthortoAlcFormat(result);
    }
    
    public Map<String, Object> getPubAuthorByKey(String key, int topK) {
        Iterator<Map<String, Object>> result = paperRepository.getPubAuthorByKey(key, topK).iterator();
        return getPubOnTimelineAndAuthortoAlcFormat(result);
    }
    
    public Map<String, Object> showAuthorToVolumeonJournal(String journal) {
        Iterator<Map<String, Object>> result = paperRepository.showAuthorToVolumeonJournal(journal).iterator();
//        System.out.println("before");
//        while(result.hasNext()) {
//        	for (Map.Entry<String, Object> entry : result.next().entrySet())
//        	{
//        	    System.out.println(entry.getKey() + "/" + entry.getValue());
//        	}
//        }
        return toAlcFormat(result, "PUBLISH");
    }
    
    public Map<String, Object> catergorizePaper(int startYear, int endYear) {
        Iterator<Map<String, Object>> result = paperRepository.categorizePaper(startYear, endYear).iterator();
        return toAlcFormat(result, "PUBLISH");
    }
    
    public Map<String, Object> authorConnectAuthor(String author1, String author2) {
        Iterator<Map<String, Object>> result = paperRepository.authorConnectAuthor(author1, author2).iterator();
        return authorConnectAuthortoAlcFormat(result);
    }
    
    public Map<String, Object> findExpertsByKey(String key, int topK) {
        Iterator<Map<String, Object>> result = paperRepository.findExpertsByKey(key, topK).iterator();
        return getPubOnTimelineAndAuthortoAlcFormat(result);
    }
    
    public Map<String, Object> paperInterested(String key1, String key2) {
        Iterator<Map<String, Object>> result = paperRepository.paperInterested(key1, key2).iterator();
        return paperInterestedToAlcFormat(result);
    }
    
    public Map<String, Object> showPaperRelationshipOnKey(String key) {
        Iterator<Map<String, Object>> result = paperRepository.showPaperRelationshipOnKey(key).iterator();
        return toAlcFormat(result, "CITE");
    }
    
    public Map<String, Object> paperToPaper(String key) {
        Iterator<Map<String, Object>> result = paperRepository.showPaperRelationshipOnKey(key).iterator();
        return toAlcFormat(result, "CITE");
    }
    
    public Map<String, Object> showPaperToPaper() {
        Iterator<Map<String, Object>> result = paperRepository.showPaperToPaper().iterator();
        return toAlcFormat(result, "CITE");
    }
    
    public Map<String, Object> showPersonToPerson() {
        Iterator<Map<String, Object>> result = paperRepository.showPersonToPerson().iterator();
        return toAlcFormat(result, "CO");
    }
    
    public Map<String, Object> showPaperToPerson() {
        Iterator<Map<String, Object>> result = paperRepository.showPaperToPerson().iterator();
        return toAlcFormat(result, "PUBLISH");
    }
    
    public Map<String, Object> catergorizePaperVersion2(int startYear, int endYear, String journal, String key) {
        Iterator<Map<String, Object>> result = paperRepository.catergorizePaperVersion2(startYear, endYear, journal, key).iterator();
        return toAlcFormat(result, "PUBLISH");
    }
    
    public Map<String, Object> showPastPubCard() {
        Iterator<Map<String, Object>> result = paperRepository.showPastPubCard().iterator();
        return showCardToAlcFormat(result);
    }
    
    public Map<String, Object> showPubInfoCard() {
        Iterator<Map<String, Object>> result = paperRepository.showPubInfoCard().iterator();
        return showPubCardToAlcFormat(result);
    }
    
    public Map<String, Object> authorOfCollabrator(String key1, String key2) {
        Iterator<Map<String, Object>> result = paperRepository.authorOfCollabrator(key1, key2).iterator();
        return paperInterestedToAlcFormat(result);
    }
    
    public Map<String, Object> authorOnGoogleMap(String key, double latitude, double longitude) {
        Iterator<Map<String, Object>> result = paperRepository.authorOnGoogleMap(key, latitude, longitude).iterator();
        return authorOnGoogleMaptoAlcFormat(result);
    }
    
    public Map<String, Object> pubOnGoogleMap(String key, int startYear, int endYear) {
        Iterator<Map<String, Object>> result = paperRepository.pubOnGoogleMap(key, startYear, endYear).iterator();
        return authorOnGoogleMaptoAlcFormat(result);
    }
}

