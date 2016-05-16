/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.mastermind;

/**
 *
 * @author mlongton
 */
public class Word {
    private final char[] theWord;
    public final int length;
    private final boolean isKnown;
    
    public Word(String wrd) {
        theWord = wrd.toCharArray();
        length = theWord.length;
        isKnown = true;
    }
    
    public Word(int len) {
        length = len;
        theWord = null;
        isKnown = false;
    }
    
    public BW compareGuess(String str) {
        return compareGuess(str.toCharArray());
    }
    
    public BW compareGuess(char[] word) {
        if (word.length != length) return null;
        if (!isKnown) return null;
        int[] usedBy = new int[length];
        BW answer = new BW();
        
        for (int i = 0; i < length; i++) {
            if (word[i] == theWord[i]) {
                usedBy[i] = i;
                answer.blacks++;
            }
            else {
                usedBy[i] = -1;
            }
        }
        for (int i = 0; i < length; i++) {
            boolean isUsed = false;
            for (int k = 0; k < length; k++) {
                if (usedBy[k] == i) isUsed = true;
            }
            for (int j = 0; !isUsed && j < length; j++) {
                if (usedBy[j] < 0 && theWord[j] == word[i] && i != j) {
                    usedBy[j] = i;
                    isUsed = true;
                    answer.whites++;
                }
            }
        }
        return answer;
    }
}
