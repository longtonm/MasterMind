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
public class BW {
    public int blacks, whites;
    
    public BW(int b, int w) {
        blacks = b;
        whites = w;
    }
    
    public BW() {
        this(0,0);
    }
}
