/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.mastermind;

import java.util.*;
import java.sql.*;

/**
 *
 * @author mlongton
 */
public class MMArrayDictionary extends MMDictionary {
    
    /**
     * The local copy of the Dictionary to be searched for words.
     * The outermost list is indexed by the length of the word.
     * The next list has length equal to its position in the first list and each index represents a different character position.
     * The third list has length 26 and indicates which character is in that position.
     * words[length][i][c] is a Collection of words where the ith character is c.
     */
    private List<List<List<Collection<String>>>> words;
    
    public MMArrayDictionary() {
        words = new ArrayList<>(14);
        words.add(0,null);
        words.add(1, null);
    }
    
    public MMArrayDictionary(int length) {
        this();
        activateLength(length);
    }
    
    public void activateLength(int length) {
        if (words.size() <= length || words.get(length) == null) {
            while (words.size() <= length) words.add(words.size(),null);
            List<List<Collection<String>>> listForLength = new ArrayList<>(length);
            words.set(length, listForLength);
            while (listForLength.size() < length) {
                List<Collection<String>> latestList = new ArrayList<>(26);
                listForLength.add(latestList);
                for (int i = 0; i < 26; i++) {
                    latestList.add(new LinkedList<>());
                }
            }
            try {
                Statement stmt = makeConnection();
                String query = "select word from words where length = "+length;
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String word = rs.getString(1);
                    for (int i = 0; i < length; i++) {
                        words.get(length).get(i).get(word.charAt(i)-Possibility.OFFSET).add(word);
                    }
                }
                stmt.close();
            }
            catch (SQLException f) {
                System.err.println("Failed to copy dictionary to memory: "+f);
            }
        }
    }
    
    public List<String> lookup(Possibility p) {
        if (p.wordCache != null) {
            return p.wordCache;
        }
        activateLength(p.length);
        int firstIndex = 0;
        while (firstIndex < p.length && p.letters(firstIndex) == Possibility.BLANK) firstIndex++;
        LinkedList<String> allowedWords;
        if (firstIndex < p.length) {
            allowedWords = new LinkedList<>(words.get(p.length).get(firstIndex).get(p.letters(firstIndex)-Possibility.OFFSET));
        }
        else {
            allowedWords = new LinkedList<>();
            for (char c = 'a'; c <= 'z'; c++) {
                if (p.getNoMore(c)) continue;
                allowedWords.addAll(words.get(p.length).get(0).get(c-Possibility.OFFSET));
            }
        }
        for (int i = firstIndex+1; i < p.length; i++) {
            if (p.letters(i) != Possibility.BLANK) {
                Iterator<String> it = allowedWords.iterator();
                while (it.hasNext()) {
                    String currentWord = it.next();
                    if (currentWord.charAt(i) != p.letters(i)) {
                        it.remove();
                    }
                }
            }
        }
        for (int i = 0; i < p.length; i++) { //switch order of loops to create just one iterator?
            if (p.letters(i) == Possibility.BLANK) {
                Iterator<String> it = allowedWords.iterator();
                while (it.hasNext()) {
                    String currentWord = it.next();
                    if (p.getNoMore(currentWord.charAt(i))) it.remove();
                }
            }
        }
        p.wordCache = allowedWords;
        return allowedWords;
    }
    
    public int roughLookupCount(Possibility p) {
        return preciseLookupCount(p);
    }
    
    public int preciseLookupCount(Possibility p) {
        return lookup(p).size();
    }
}
