package startingPoint.KG_DBLP;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseOperation {
	// database name
    private static String DATABASE_NAME = "";
    private static String TABLE_USER = "";
    private static String TABLE_SearchHitory = "";
    private static String TABLE_FOLLOWER = "";
    private static String TABLE_PUBLICATION = "";
    private static String TABLE_NOTIFICATION = "";
    private Connection connection;
    private Statement statement;
    

    public DatabaseOperation() throws SQLException {
    	
    	DATABASE_NAME = "test";
    	TABLE_USER = "user";
    	TABLE_SearchHitory = "searchHistory";
    	TABLE_FOLLOWER = "follower";
    	TABLE_PUBLICATION = "publication";
    	TABLE_NOTIFICATION = "notification";
    	
    	try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		String url = "jdbc:mysql://localhost:3306/" + DATABASE_NAME;
		String username = "root";
		String password = "";

		connection =  DriverManager.getConnection(url, username, password);
		statement = connection.createStatement();
    }
    
    public void createTable() throws SQLException, IOException {
		
		statement.executeQuery("use test");
		
		String createQuery = "create table if not exists " + TABLE_USER +
				" (id INT NOT NULL AUTO_INCREMENT, name char(25), password char(25), PRIMARY KEY (id))";
		statement.executeUpdate(createQuery); // execute the query
		
		createQuery = "create table if not exists " + TABLE_SearchHitory +
				"(id INT NOT NULL, history char(25), FOREIGN KEY (id) REFERENCES user(id) ON UPDATE CASCADE ON DELETE RESTRICT)";
		statement.executeUpdate(createQuery); // execute the query
		
		createQuery = "create table if not exists " + TABLE_FOLLOWER +
				"(followee INT NOT NULL, follower INT NOT NULL, FOREIGN KEY (followee) REFERENCES user(id) ON UPDATE CASCADE ON DELETE RESTRICT, FOREIGN KEY (follower) REFERENCES user(id) ON UPDATE CASCADE ON DELETE RESTRICT)";
		statement.executeUpdate(createQuery); // execute the query
		
		createQuery = "create table if not exists " + TABLE_PUBLICATION +
				" (id INT NOT NULL, title char(100), FOREIGN KEY (id) REFERENCES user(id) ON UPDATE CASCADE ON DELETE RESTRICT)";
		statement.executeUpdate(createQuery); // execute the query
		
		createQuery = "create table if not exists " + TABLE_NOTIFICATION +
				" (id INT NOT NULL , username char(25), title char(100), FOREIGN KEY (id) REFERENCES user(id) ON UPDATE CASCADE ON DELETE RESTRICT, PRIMARY KEY (id))";
		statement.executeUpdate(createQuery); // execute the query
	}
    
    public void insertUser(String name, String pw) throws SQLException, IOException {
    	String insertQuery = "insert ignore into " + TABLE_USER + " values (NULL, '" + name + "', '" + pw +  "')";
    	statement.executeUpdate(insertQuery);
    }
    
    public void insertHistory(String userName, String authorName) throws SQLException {
    	String query = "select * from "+TABLE_USER + " where name ='" + userName + "'";
		ResultSet rs = statement.executeQuery(query);
		int id = 0;
		while(rs.next()) {
			id = rs.getInt(1);
		}
		System.out.println("username is " + userName);
		System.out.println("id is " + id);
		query = "insert ignore into " + TABLE_SearchHitory + " values (" + id + ", '" + authorName +  "')";
		statement.executeUpdate(query);
    }
    
    public void insertFollower(String followerName, String followeeName) throws SQLException {
    	String query = "select * from "+TABLE_USER + " where name ='" + followerName + "'";
		ResultSet rs = statement.executeQuery(query);
		int followerId = 0;
		
		while(rs.next()) {
			followerId = rs.getInt(1);
		}
    	query = "select * from "+TABLE_USER + " where name ='" + followeeName + "'";
		rs = statement.executeQuery(query);
		int followeeId = 0;
		while(rs.next()) {
			followeeId = rs.getInt(1);
		}
		
		query = "insert ignore into " + TABLE_FOLLOWER + " values (" + followeeId + ", " + followerId +  ")";
		statement.executeUpdate(query);
    }
    
    public void insertPublicationAndNotification(String userName, String pubName) throws SQLException {
    	String query = "select * from "+TABLE_USER + " where name ='" + userName + "'";
		ResultSet rs = statement.executeQuery(query);
		int id = 0;
		while(rs.next()) {
			id = rs.getInt(1);
		}
		
		query = "insert into " + TABLE_PUBLICATION + " values (" + id + ", '" + pubName +  "')";
		statement.executeUpdate(query);
		
		query = "replace into " + TABLE_NOTIFICATION + " values (" + id + ", '" + userName + "', '" + pubName + "')";
		statement.executeUpdate(query);
    }
    
    public boolean isUserExist(String name, String pw) throws SQLException{
    	String query = "select * from "+TABLE_USER + " where name ='" + name + "'";
		ResultSet rs = statement.executeQuery(query);
		String oldPW = "";
		while(rs.next()) {
			oldPW = rs.getString(3);
		}
		if(oldPW.equals(pw)) {
			return true;
		}
		else {
			return false;
		}
    }
    
    public String getUserSearchHistory(String name) throws SQLException {
    	StringBuilder history = new StringBuilder();
    	String query = "select * from "+TABLE_USER + " where name ='" + name + "'";
		ResultSet rs = statement.executeQuery(query);
		int id = 0;
		while(rs.next()) {
			id = rs.getInt(1);
		}
		query = "select * from "+TABLE_SearchHitory + " where id =" + id;
		rs = statement.executeQuery(query);
		while(rs.next()) {
			history.append(rs.getString(2));
			history.append("|");
		}
		if(history.length()>0) {
			history.deleteCharAt(history.length()-1);
		}
		return history.toString();
    }
    
    public String getFollowers(String name) throws SQLException {
    	StringBuilder followers = new StringBuilder();
    	String query = "select * from "+TABLE_USER + " where name ='" + name + "'";
		ResultSet rs = statement.executeQuery(query);
		int id = 0;

		while(rs.next()) {
			id = rs.getInt(1);
		}
		
		query = "select * from follower where followee =" + id;
		rs = statement.executeQuery(query);
		
		ArrayList<Integer> followerID = new ArrayList<>();
		while(rs.next()) {
			followerID.add(rs.getInt(2));
		}
		for(int followerid: followerID) {
			query = "select * from "+TABLE_USER + " where id =" + followerid;
			rs = statement.executeQuery(query);
			while(rs.next()) {
				followers.append(rs.getString(2));
				followers.append("|");
			}
		}
		if(followers.length()>0) {
			followers.deleteCharAt(followers.length()-1);
		}
		return followers.toString();
    }
    
    public String getNotification(String name) throws SQLException {
    	StringBuilder notifications = new StringBuilder();
    	String query = "select * from "+TABLE_USER + " where name ='" + name + "'";
		ResultSet rs = statement.executeQuery(query);
		int id = 0;

		while(rs.next()) {
			id = rs.getInt(1);
		}
		
		query = "select * from follower where follower =" + id;
		rs = statement.executeQuery(query);
		
		ArrayList<Integer> followeeID = new ArrayList<>();
		while(rs.next()) {
			followeeID.add(rs.getInt(1));
		}
		for(int followeeid: followeeID) {
			query = "select * from "+TABLE_NOTIFICATION + " where id =" + followeeid;
			rs = statement.executeQuery(query);
			while(rs.next()) {
				notifications.append(rs.getString(2));
				notifications.append(" recently published ");
				notifications.append(rs.getString(3));
				notifications.append("|");
			}
		}
		if(notifications.length()>0) {
			notifications.deleteCharAt(notifications.length()-1);
		}
		return notifications.toString();
    }
    
    public String getPublications(String name) throws SQLException {
    	StringBuilder publications = new StringBuilder();
    	String query = "select * from "+TABLE_USER + " where name ='" + name + "'";
		ResultSet rs = statement.executeQuery(query);
		int id = 0;
		while(rs.next()) {
			id = rs.getInt(1);
		}
		query = "select * from "+TABLE_PUBLICATION + " where id =" + id;
		rs = statement.executeQuery(query);
		while(rs.next()) {
			publications.append(rs.getString(2));
			publications.append("|");
		}
		if(publications.length()>0) {
			publications.deleteCharAt(publications.length()-1);
		}
		return publications.toString();
    }
    
    public String getAllUsers(String name) throws SQLException {
    	String query = "select * from "+TABLE_USER+ " where  name !='" + name + "'";
		ResultSet rs = statement.executeQuery(query);
		StringBuilder allUsers = new StringBuilder();
		while(rs.next()) {
			allUsers.append(rs.getString(2));
			allUsers.append("|");
		}
		if(allUsers.length()>0) {
			allUsers.deleteCharAt(allUsers.length()-1);
		}
		
		return allUsers.toString();
    }
}
