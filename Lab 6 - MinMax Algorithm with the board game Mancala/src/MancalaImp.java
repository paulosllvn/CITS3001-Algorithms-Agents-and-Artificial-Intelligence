import java.util.*;

/**
 * An agent for playing the game "Mancala" implementing the min-max algorithm with alpha beta pruning and depth limit
 * search.
 * Paul O'Sullivan
 * 21492318
 */

public class MancalaImp implements MancalaAgent {

     String player_max = "Max";
     String player_min = "Min";


    private class playResultCombo {
        int play; int result;
        playResultCombo(int play1, int result1) {
            play = play1;
            result = result1;
        }
    }

    List<playResultCombo> potentialPlays;
    int deepnessLimit;
    String playerID;

    public MancalaImp() {
        deepnessLimit = 10;
        playerID = "Paul";
        potentialPlays = new LinkedList<>();
    }



    public int playerScore(int[] board) {
        int result = 0;
        result = result + board[6] - board[13];
        int playerOneStones = 0;
        int playerTwoStones = 0;

        // check for any empty slots on player side line up with opposition side.
        int i = 0;
        while(i <= 5) {
            if(board[i] != 0) {
                i++;
                continue;
            }
            else {
                result = board[12 - i] + result;
            }
            playerOneStones = playerOneStones + board[i];
            i++;
        }

        int j = 0;
        while(j<= 12) {
            if(board[j] != 0) {
                j++;
                continue;
            }
            else{
                result = result - board[12-j];
                j++;
            }
            playerTwoStones= playerTwoStones+board[j];
        }

        // test which player has more stones
        result = playerOneStones - playerTwoStones + result;
        return result;
    }


     int winTest(int[] board) {
        boolean isOver = true;
        int notOver = -1;
        //Check if player1 forfeits by using all the stones in their house.

        //Loops through each of player ones houses and tests to see if they are zero.
        int i = 0;
        while(i<=5){
            boolean exhausted = false;
            if(board[i] == 0) {
                exhausted = true;
            }
            isOver = (isOver && exhausted);
            i++;
        }
        //If player one has forfeited, sum up each of player two's houses and add them to their respective store.
        if(isOver) {
            int j = 0;
            while (j<= 12)
            {
                board[13]= board[13] + board[j];
                board[j] = 0;
                j++;
            }
            if(mancalaResult(board) != -1){
                return mancalaResult(board);
            }
            else{
                return notOver;
            }
        }

        int k = 0;
        while(k<=12) {
            boolean exhausted = false;
            if(board[k]==0){
                exhausted = true;
            }
            isOver = (isOver && exhausted);
            k++;
        }
        if(isOver) {
            int g = 0;
            while(g <= 5)
            {
                board[6]= board[6]+board[g];
                board[g] = 0;
                g++;
            }
            int winner = mancalaResult(board);
            if(winner != -1){
                return winner;
            }
            else{
                return notOver;
            }
        }
        return notOver;
    }


    int minmaxAB(int boardState[], String playerType, int deepness, int minusInf, int inf) {
        int big = 2147483647;
        int small = -2147483647;

        int opponantwin = 0;
        int playerwin = 1;

        int winner = winTest(boardState);

        int active = -1;

        if(winner != active) {
            if(winner == playerwin) {
                return big;
            }
            else if(winner == opponantwin) {
                return small;
            }
            else {
                return 0;
            }
        }

        int newboardState[];
        int minMaxBestChoice = 0;
        boolean extraTurn = false;

        //best score at lowest depth
        if(deepness == 0) {

            int score = playerScore(boardState);

            return score;
        }

        if(playerType == player_max) {
            minMaxBestChoice = small;
            int i = 0;
            while(i <= 5) {
                if(boardState[i] == 0) {
                    i++;
                    continue;
                }
                newboardState = Arrays.copyOf(boardState,boardState.length);
                
                extraTurn = agentMove(newboardState, i);
                
                if(!extraTurn){
                    minMaxBestChoice = Math.max(minmaxAB(newboardState, player_min, deepness-1, minusInf, inf), minMaxBestChoice);

                }
                else{
                    minMaxBestChoice = Math.max(minmaxAB(newboardState, player_max, deepness-1, minusInf, inf), minMaxBestChoice);
                }

                minusInf = Math.max(minMaxBestChoice, minusInf);

                if(deepness != deepnessLimit){
                    i++;
                    continue;
                }
                else {
                    potentialPlays.add(new playResultCombo(i, minMaxBestChoice));
                }

                if(inf<=minusInf){
                    break;
                }
                i++;
            }
        }
        else if(playerType == player_min) {
            minMaxBestChoice = big;
            for(int i = 7; i < 13; i++)
            {
                //nothing to move.
                if(boardState[i] == 0) {
                    continue;
                }
                newboardState = Arrays.copyOf(boardState,boardState.length);

                extraTurn = oppositionMove(newboardState, i);
                if(extraTurn)
                    minMaxBestChoice = Math.min(minmaxAB(newboardState, player_min, deepness-1, minusInf, inf), minMaxBestChoice);
                else
                    minMaxBestChoice = Math.min(minmaxAB(newboardState, player_max, deepness-1, minusInf, inf), minMaxBestChoice);

                inf = Math.min(minMaxBestChoice, inf);
                if(inf <= minusInf) {
                    break;
                }
            }
        }

        return minMaxBestChoice;
    }

    //test which player has more stones in their mancala
    public int mancalaResult(int[] board) {
        int playerStore = board[6];
        int opponantStore = board[13];
        int winner = 0;
        //player one has more stones in their mancala than player two, return 1.
        if(playerStore > opponantStore) {
            winner = 1;
            return winner;
        }
        // player two has more stones in their mancala than player one, return 0.
        else if(opponantStore > playerStore) {
            winner = 0;
            return winner;
        }
        //a draw, return 0
        else {
            winner = -1;
            return winner;
        }
    }

    public int move(int[] board) {

        int big = 2147483647;
        int small = -2147483647;
        potentialPlays = new LinkedList<>();
        int strongestPlay = 0;
        int strongestScore = minmaxAB(board, player_max, deepnessLimit, small, big);
        int length = potentialPlays.size()-1;

        int j =0;
        while(j<=length){
            int result = potentialPlays.get(j).result;
            int play = potentialPlays.get(j).play;;
            if(result != strongestScore){
                j++;
                continue;
            }
            else {
                strongestPlay = play;
            }
            j++;
        }

        return strongestPlay;
    }

    //taken from mancala game
    boolean oppositionMove(int[] board, int mv) {
        int i = mv;
        while(board[mv]>0){
            i=i==5?7:i==13?0:i+1;
            board[i]++; board[mv]--;
        }
        if(i<13 && i>6 && board[i]==1 && board[12-i]>0){
            board[13]+=board[12-i];
            board[12-i]=0;
            board[13]+=board[i];
            board[i] = 0;
        }
        if(i!=13){
            return false;
        }
        return true;
    }


    //taken from mancala game
     boolean agentMove(int[] board, int mv) {
        int i = mv;
        while(board[mv]>0){
            i=i==12?0:i+1;
            board[i]++; board[mv]--;
        }
        if(i<6 && board[i]==1 && board[12-i]>0){
            board[6]+=board[12-i];
            board[12-i]=0;
            board[6]+=board[i];
            board[i]=0;
        }
        if(i!=6)
            return false;
        return true;
    }







    public String name()
    {
        String a = playerID;
        return a;
    }


    public void reset()
    {
        potentialPlays = new LinkedList<>();
    }


}