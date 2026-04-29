package com.byers.toetictac;
import java.util.LinkedList;

public class GameState {

    public LinkedList<int[]> moveHistory = new LinkedList<>();
    public int boardSize;
    public String[][] board;
    public boolean xTurn = true;
    public int xScore = 0;
    public int oScore = 0;
    public int turnCount = 0;
    public String lastMovePlayer;

    //UltimateTTT
    public String[][][][] ultimateBoards;
    public String[][] ultimateMetaBoard; // result of each mini board
    public int activeRow = -1;
    public int activeCol = -1;
}