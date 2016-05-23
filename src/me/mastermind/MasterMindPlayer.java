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
public class MasterMindPlayer {
    public final int length;
    public final Word wrd;
    public ArrayList<Possibility> knowledge;
    public MMDictionary dict;
    private Possibility certainMask;
    
    public MasterMindPlayer(MMDictionary dc, Word w) {
        wrd = w;
        length = w.length;
        knowledge = new ArrayList<>();
        knowledge.add(new Possibility(length));
        dict = dc;
    }
    
    public void interpret(String guess, BW got) {
        interpret(guess.toCharArray(),got);
    }
    
    public void interpret(char[] guess, BW got) {
        LinkedList<int[]> possibles = new LinkedList<>();
        LinkedList<Possibility> newKnowledge = new LinkedList<>();
        //The old version of this section (creating possibles), while very dense, might be better
        LinkedList<ArrayList<Integer>> possibleBlacks, possibleWhites;
        ArrayList<Integer> simpleNumbers = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            simpleNumbers.add(i);
        }
        possibleBlacks = choose(got.blacks,simpleNumbers);
        for (ArrayList<Integer> blackIndices : possibleBlacks) {
            simpleNumbers = new ArrayList<>(length-got.blacks);
            int[] blackHyp = new int[length];
            int i = 0;
            for (int j = 0; j < length; j++) {
                if (i < blackIndices.size() && blackIndices.get(i) == j) {
                    i++;
                    blackHyp[j] = 2;
                }
                else {
                    simpleNumbers.add(j);
                }
            }
            possibleWhites = choose(got.whites,simpleNumbers);
            for (ArrayList<Integer> whiteIndices : possibleWhites) {
                int[] hypothesis = blackHyp.clone();
                for (int j : whiteIndices) {
                    hypothesis[j] = 1;
                }
                possibles.add(hypothesis);
            }
        }
        //possibles now contains all ways of assigning blacks (2) and whites (1) to the letters of guess
        for (int[] hypothesis : possibles) {
            Possibility p = new Possibility(length);
            ArrayList<Integer> freeSpots = new ArrayList<>(length-got.blacks);
            int[] whiteLocations = new int[got.whites];
            int nW = 0;
            for (int i = 0; i < length; i++) {
                if (hypothesis[i] == 2) {
                    p.setLetter(i,guess[i]);
                }
                else {
                    freeSpots.add(i);
                    if (hypothesis[i] == 0) {
                        p.setNoMore(guess[i], true);
                    }
                    else if (hypothesis[i] == 1) {
                        whiteLocations[nW++] = i;
                    }
                }
            }
            if (got.whites == 0) newKnowledge.add(p);
            else { //there are whites to shift around
                whiteShifterLoop: for (ArrayList<Integer> whiteOrigins : pick(got.whites,freeSpots)) {
                    Possibility toAdd = p.pclone();
                    for (int i = 0; i < got.whites; i++) {
                        if (whiteLocations[i] == whiteOrigins.get(i)) continue whiteShifterLoop;
                        toAdd.setLetter(whiteOrigins.get(i),guess[whiteLocations[i]]);
                    }
                    newKnowledge.add(toAdd);
                }
            }
        }
        ArrayList<Possibility> combinedKnowledge = new ArrayList<>();
        for (Possibility p1 : knowledge) {
            for (Possibility p2 : newKnowledge) {
                Possibility p3 = p1.and(p2);
                if (p3 != null) {
                    List<String> p3Words = dict.lookup(p3);
                    if (p3Words.isEmpty()) p3 = null;
                    else {
                        /*Possibility p4 = new Possibility(p3Words.get(0));
                        for (String w : p3Words) {
                            for (int i = 0; i < length; i++) {
                                if (p4.letters[i] == Possibility.BLANK) {
                                    p4.setNoMore(w.charAt(i), false);
                                }
                                else {
                                    if (w.charAt(i) != p4.letters[i]) {
                                        p4.setNoMore(p4.letters[i],false);
                                        p4.setNoMore(w.charAt(i), false);
                                        p4.letters[i] = Possibility.BLANK;
                                    }
                                }
                            }
                        }*/
                        Possibility p4 = Possibility.forWords(p3Words);
                        if (p4 != null) p3 = p4;
                    }
                }
                if (p3 != null && !combinedKnowledge.contains(p3)) { //doesn't merge matching letters with differing noMore
                    combinedKnowledge.add(p3);
                }
            }
        }
        Iterator<Possibility> it = combinedKnowledge.iterator();
        while (it.hasNext()) {
            Possibility p = it.next();
            int pCount = dict.roughLookupCount(p);
            if (pCount == 0) it.remove();
            else if (pCount < 0) System.err.println("Dictionary count for "+p.letters()+" must have failed.");
        }
        knowledge = reduceKnowledge(combinedKnowledge);
        //knowledge = combinedKnowledge;
    }
    
    public String makeGuess() {
        if (certainMask == null) makeCertainPoss();
        String randomWordChoice;
        Possibility useToGuess;
        List<String> allGuesses = new ArrayList<>();
        for (Possibility p : knowledge) {
            useToGuess = p.pclone();
            for (int i = 0; i < length; i++) {
                if (certainMask.letters(i) != Possibility.BLANK) {
                    useToGuess.setLetter(i, Possibility.BLANK);
                }
            }
            List<String> forUTG = dict.lookup(useToGuess);
            if (forUTG.isEmpty()) {
                forUTG = dict.lookup(p);
            }
            allGuesses.addAll(forUTG);
        }
        randomWordChoice = allGuesses.get((int)(Math.random()*allGuesses.size()));
        return randomWordChoice;
    }
    
    /**
     * Find all ways of selecting k objects from a list, independent of order.
     *
     * @param   k The number of items to select.
     * @param   lst The list of objects to choose from.  Duplicates are allowed and will permit repeated items to be selected more than once.
     * @param   <E> The type of elements in lst.
     * @return A LinkedList, each item of which is an ArrayList of items from lst that have been selected.
     */
    public static <E> LinkedList<ArrayList<E>> choose(int k, ArrayList<E> lst) {
        LinkedList<ArrayList<E>> results = new LinkedList<>();
        int n = lst.size();
        if (k < 0 || k > n) return results;
        if (k == 0) {
            results.add(new ArrayList<>(k));
            return results;
        }
        int[] choices = new int[k];
        int whichChoice = 0, index = 0;
        while (whichChoice >= 0) {
            if (index <= n - (k - whichChoice)) { //this choice can be placed on this item
                choices[whichChoice] = index;
                if (whichChoice < k-1) { //haven't placed the last choice yet
                    index = choices[whichChoice++]+1;
                }
                else { //we have one completed combination
                    ArrayList<E> finishedOne = new ArrayList<>(k);
                    for (int i = 0; i < k; i++) {
                        finishedOne.add(lst.get(choices[i]));
                    }
                    results.add(finishedOne);
                    index++;
                }
            }
            else { //time to backtrack
                if (whichChoice > 0) index = choices[--whichChoice] + 1;
                else whichChoice--;
            }
        }
        return results;
    }
    
    public static <E> LinkedList<ArrayList<E>> pick(int k, ArrayList<E> lst) { //need to modify to include all orderings
        LinkedList<ArrayList<E>> results = new LinkedList<>();
        int n = lst.size();
        if (k < 0 || k > n) return results;
        if (k == 0) {
            results.add(new ArrayList<>(k));
            return results;
        }
        int[] choices = new int[k];
        int whichChoice = 0, index = 0;
        mainLoop: while (whichChoice >= 0) {
            if (index < n) {
                if (index < n) { //this choice can be placed on this item
                    for (int i = 0; i < whichChoice; i++) {
                        if (choices[i] == index) { //this item is already taken and can't be chosen again
                            index++;
                            continue mainLoop;
                        }
                    }
                    choices[whichChoice] = index;
                    if (whichChoice < k - 1) { //haven't placed the last choice yet
                        index = 0;
                        whichChoice++;
                    } else { //we have one completed combination
                        ArrayList<E> finishedOne = new ArrayList<>(k);
                        for (int i = 0; i < k; i++) {
                            finishedOne.add(lst.get(choices[i]));
                        }
                        results.add(finishedOne);
                        index++;
                    }
                }
            } else { //time to backtrack
                if (whichChoice > 0) index = choices[--whichChoice] + 1;
                else whichChoice--;
            }
        }
        return results;
    }
    
    public static final int EQUAL_WORDS = 0, SUPERSET_WORDS = 1, SUBSET_WORDS = -1, DISJOINT_WORDS = -2;
    public int comparePossByWords(Possibility first, Possibility second) {
        //if (first.equals(second)) return EQUAL_WORDS;
        List<String> firstWords = new LinkedList<>(dict.lookup(first)), secondWords = new LinkedList<>(dict.lookup(second));
        Iterator<String> it = firstWords.iterator();
        while (it.hasNext()) {
            String w1 = it.next();
            if (secondWords.remove(w1)) it.remove();
        }
        if (firstWords.size() > 0 && secondWords.size() > 0) return DISJOINT_WORDS;
        else if (firstWords.size() > 0) return SUPERSET_WORDS;
        else if (secondWords.size() > 0) return SUBSET_WORDS;
        else return EQUAL_WORDS;
    }
    
    public ArrayList<Possibility> reduceKnowledge(ArrayList<Possibility> kn) {
        for (int i = 0; i < kn.size(); i++) {
            for (int j = i+1; j < kn.size(); j++) {
                int cmp = comparePossByWords(kn.get(i),kn.get(j));
                if (cmp == EQUAL_WORDS || cmp == SUPERSET_WORDS) {
                    kn.remove(j--);
                }
                else if (cmp == SUBSET_WORDS) {
                    kn.remove(i--);
                    break;
                }
            }
        }
        makeCertainPoss();
        return kn;
    }
    
    public void makeCertainPoss() {
        Possibility mask = new Possibility(length);
        for (int i = 0; i < length; i++) {
            mask.setLetter(i,knowledge.get(0).letters(i));
        }
        for (Possibility p : knowledge) {
            for (int i = 0; i < length; i++) {
                if (mask.letters(i) != Possibility.BLANK && p.letters(i) != mask.letters(i)) {
                    mask.setLetter(i, Possibility.BLANK);
                }
            }
        }
        certainMask = mask;
    }
}
