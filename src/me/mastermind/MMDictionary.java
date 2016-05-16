/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.mastermind;

import java.sql.*;
import java.util.*;

/**
 *
 * @author mlongton
 */
public class MMDictionary {

    public Connection sqliteConnection;//, localConnection;
    //public int localWordLength;

    public List<String> lookup(Possibility p) {
        System.err.println("If this is getting called, something is wrong and the game will run slow.");
        String query = "";
        try {
            Statement statement = makeConnection();
            query = "select word from words";
            LinkedList<Character> ignoredNoMore = new LinkedList<>(); //store duplicate letters that should be removed later if present
            query += makeFilters(p, ignoredNoMore);
//            System.err.println(query); //temporary
            ResultSet rs = statement.executeQuery(query);
            ArrayList<String> resultWords = new ArrayList<>();
            while (rs.next()) {
                String aWord = rs.getString(1);
                boolean skipWord = false;
                for (char c : ignoredNoMore) {
                    for (int i = 0; i < p.length; i++) {
                        if (aWord.charAt(i) == c && p.letters(i) != c) skipWord = true;
                    }
                }
                if (!skipWord) resultWords.add(aWord);
            }
            statement.close();
//            System.err.println(resultWords); //temporary
            return resultWords;
        }
        catch (SQLException e) {
            System.err.println(e);
            System.err.println(query);
            return null;
        }
    }
    
    public int roughLookupCount(Possibility p) {
        String query = "";
        try {
            Statement statement = makeConnection();
            query = "select count(*) n from words";
            query += makeFilters(p, null);
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            int n = rs.getInt(1);
            statement.close();
            return n;
        }
        catch (SQLException e) {
            System.err.println(e);
            System.err.println(query);
            return -1;
        }
    }

    private static String makeFilters(Possibility p, LinkedList<Character> ignoredNoMore) {
        String where = " where length = " + p.length + " and word like '" + p.letters() + "'";
        noMoreLoop: for (char i = 'a'; i <= 'z'; i++) {
            if (p.getNoMore(i)) {
                for (int j = 0; j < p.length; j++) {
                    if (p.letters(j) == i) {
                        if (ignoredNoMore != null) {
                            ignoredNoMore.add(p.letters(j));
                        }
                        continue noMoreLoop;
                    }
                }
                where += " and word not like '%" + i + "%'";
            }
        }
        return where;
    }
    
    public boolean validWord(int length, String word) {
        try {
            Statement statement = makeConnection();
            String query = "select count(*) from words where length = "+length+" and word = '"+word+"'";
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            int n = rs.getInt(1);
            statement.close();
            return n > 0;
        }
        catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }
    
    public String randomWord(int length) {
        try {
            Statement statement = makeConnection();
            String query = "select word from words where length = "+length+" order by random() limit 1";
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            String rWord = rs.getString(1);
            statement.close();
            return rWord;
        }
        catch (SQLException e) {
            System.err.println(e);
            return null;
        }
    }
    
    public Statement makeConnection() throws SQLException {
        if (sqliteConnection == null) {
            sqliteConnection = DriverManager.getConnection("jdbc:sqlite::resource:"+this.getClass().getClassLoader().getResource("data/dictionary.sqlite3"));
        }
        Statement statement = sqliteConnection.createStatement();
        statement.setQueryTimeout(60);
        return statement;
    }
    
    public void addCache(Possibility p, List<String> wrds) {
        p.wordCache = wrds;
    }
    
    public void addCache(Possibility p) {
        addCache(p,lookup(p)); //this will be an arraylist, linkedlist might be better
    }
}
