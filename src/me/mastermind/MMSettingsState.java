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
public class MMSettingsState {
    public boolean autoGuess;
    public int wordChooser;
    public final static int USER_WORD = 0, RANDOM_WORD = 1, SECRET_WORD = 2;
    public int length;
    
    public MMSettingsState(int len, int wc, boolean ag) {
        length = len;
        wordChooser = wc;
        autoGuess = ag;
    }
}
