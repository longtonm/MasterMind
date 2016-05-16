/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.mastermind;

import java.util.*;

/**
 *
 * @author mlongton
 */
public class Possibility {
    
    public static final int OFFSET = 97;
    public static final char BLANK = '_';
    
    private char[] letters;
    private boolean[] noMore;
    public final int length;
    
    public List<String> wordCache;
    
    public Possibility(int len) {
        length = len;
        letters = new char[length];
        for (int i = 0; i < len; i++) {
            letters[i] = BLANK;
        }
        noMore = new boolean[26];
    }
    
    public Possibility(String word) {
        length = word.length();
        letters = word.toCharArray();
        noMore = new boolean[26];
        for (int i = 0; i < 26; i++) {
            noMore[i] = true;
        }
    }
    
    public boolean getNoMore(char a) {
        char b = Character.toLowerCase(a);
        return noMore[b-OFFSET];
    }
    
    public void setNoMore(char a, boolean set) {
        char b = Character.toLowerCase(a);
        noMore[b-OFFSET] = set;
        wordCache = null;
    }
    
    public char letters(int i) {
        return letters[i];
    }
    
    public String letters() {
        return String.valueOf(letters);
    }
    
    public void setLetter(int i, char c) {
        letters[i] = c;
        wordCache = null;
    }
    
    public Possibility pclone() {
        Possibility newP = new Possibility(length);
        newP.letters = (char[]) letters.clone();
        newP.noMore = (boolean[]) noMore.clone();
        return newP;
    }
    
    public Possibility and(Possibility other) {
        if (other.length != length) return null;
        Possibility anded = new Possibility(length);
        for (int i = 0; i < length; i++) {
            if (letters(i) == BLANK && other.letters(i) != BLANK) {
                if (getNoMore(other.letters(i))) return null;
                anded.setLetter(i,other.letters(i));
            }
            else if (letters(i) != BLANK && other.letters(i) == BLANK) {
                if (other.getNoMore(letters(i))) return null;
                anded.setLetter(i,letters(i));
            }
            else if (letters(i) != BLANK && other.letters(i) != BLANK) {
                if (letters(i) != other.letters(i)) return null;
                anded.setLetter(i,letters(i));
            }
        }
        for (int i = 0; i < noMore.length; i++) {
            anded.noMore[i] = noMore[i] || other.noMore[i];
        }
        return anded;
    }
    
    @Override
    public boolean equals(Object oth) {
        if (oth.getClass() != this.getClass()) return false;
        Possibility other = (Possibility)oth;
        if (length != other.length) return false;
        for (int i = 0; i < length; i++) {
            if (letters(i) != other.letters(i)) return false;
        }
        for (int i = 0; i < noMore.length; i++) {
            if (noMore[i] != other.noMore[i]) return false;
        }
        return true;
    }
    
    public static Possibility forWords(List<String> words) {
        if (words.isEmpty()) return null;
        int length = words.get(0).length();
        Possibility p = new Possibility(words.get(0));
        for (String w : words) {
            if (w.length() != length) return null;
            for (int i = 0; i < length; i++) {
                if (p.letters(i) == Possibility.BLANK) {
                    p.setNoMore(w.charAt(i), false);
                }
                else {
                    if (w.charAt(i) != p.letters(i)) {
                        p.setNoMore(p.letters(i),false);
                        p.setNoMore(w.charAt(i), false);
                        p.setLetter(i,Possibility.BLANK);
                    }
                }
            }
        }
        return p;
    }
    
    public String toString() {
        return this.letters();
    }
}
