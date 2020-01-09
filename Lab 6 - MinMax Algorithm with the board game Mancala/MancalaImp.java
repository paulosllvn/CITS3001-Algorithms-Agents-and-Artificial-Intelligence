import java.util.List;
import java.util.LinkedList;

/**
 * An AI for a game of Mancala implemented using a minimax search algorithm.
 * @author Abrar Amin (21518928@student.uwa.edu.au)
 */
public class MancalaImp implements MancalaAgent
{

    private static final int MAXPLAYER = 1;
    private static final int MINPLAYER = 2;

    private class MoveScorePair
    {
        int move;
        int score;
        MoveScorePair(int inMove, int inScore)
        {
            this.move = inMove;
            this.score = inScore;
        }
    }

    private String agentName;
    private int depth;
    List<MoveScorePair> bestMoveList;

    //constructors.
    public MancalaImp()
    {
        this.depth = 12;
        this.agentName = "Paul";
        this.bestMoveList = new LinkedList<>();
    }

//    public MancalaImp(int inDepth, String inAgentName)
//    {
//        if(inDepth > 0)
//            this.depth = inDepth;
//        else
//        {
//            this.depth = 7;
//        }
//
//        this.agentName = inAgentName;
//        this.bestMoveList = new LinkedList<MoveScorePair>();
//    }


    /**
     * Evaluate a score for player incase maximum depth has been reached.
     */
    private int evalScore(int[] b)
    {
        int score = 0;

        score+=2*(b[6] - b[13]); //give some weight to having max. number of seeds in store.

        int seedsPlayerOne = 0;
        int seedsPlayerTwo = 0;

        //eval func accounts for whether there exists more capturable positions.
        for(int i = 0; i < 6; i++)
        {
            if(b[i] == 0){
                score+=2*b[12-i]; //number of seeds that could potentially be captured.
            }
            seedsPlayerOne+=b[i];
        }

        for(int j = 7; j < 13; j++)
        {
            if(b[j] == 0){
                score-=3*b[12-j];
            }
            seedsPlayerTwo+=b[j];
        }

        //which player has more seeds on their board..
        score+=2*(seedsPlayerOne - seedsPlayerTwo);

        return score;
    }

    //test which player has more stones in their mancala
    private int getGameWinner(int[] b)
    {
        if(b[6] > b[13]) {return 1;}
        else if(b[13] > b[6]) {return 2;}
        else {return 0;}
    }

    /**
     * A method that looks to see if the game is currently over, if so return an integer that
     * represents the winner.
     * @param gameBoard showing the current state of the game.
     * @return -1 if game is Active, 1 if player1 (this agent) wins, 2 if player2 (opponent) wins
     * and 0 if match is tied.
     */
    private int checkForWin(int[] gameBoard)
    {
        int gameResult = -1;
        boolean gameOver = true;
        //Check if player1 ended the game by exhausting his/her houses.
        for(int i = 0; i < 6; i++)
        {
            gameOver = (gameOver && (gameBoard[i] == 0));
        }
        if(gameOver)
        {
            for(int j = 7; j < 13; j++)
            {
                gameBoard[13]+=gameBoard[j];
                gameBoard[j] = 0;
            }
            gameResult = getGameWinner(gameBoard);
            return gameResult;
        }
        //check if player2 ended the game by exhausting his/her houses.
        gameOver = true;
        for(int j = 7; j < 13; j++)
        {
            gameOver = (gameOver && (gameBoard[j] == 0));
        }
        if(gameOver)
        {
            for(int i = 0; i < 6; i++)
            {
                gameBoard[6]+=gameBoard[i];
                gameBoard[i] = 0;
            }
            gameResult = getGameWinner(gameBoard);
            return gameResult;
        }
        return gameResult;
    }

    /**
     * Allows the agent to nominate the house the agent would like to move seeds from.
     * The agent will allways have control of houses 0-5 with store at 6.
     * Any move other than 0-5 will result in a forfeit.
     * An move from an empty house will result in a forfeit.
     * A legal move will always be available.
     * Assume your agent has 0.5 seconds to make a move.
     * @param board the current state of the game.
     * The board is an int array of length 14, indicating the 12 houses and 2 stores.
     * The agent's house are 0-5 and their store is 6.
     * The opponents houses are 7-12 and their store is 13. Board[i] is the number of seeds in house (store) i.
     * board[(i+1}%14] is the next house (store) anticlockwise from board[i].
     * This will be consistent between moves of a normal game so the agent can maintain a strategy space.
     * @return the house the agent would like to move the seeds from this turn.
     */
    public int move(int[] board)
    {
        bestMoveList.clear();
        int bestScore = alphaBetaMiniMax(board, MAXPLAYER, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
        int bestMove = 0;
        for(int i = 0; i < bestMoveList.size(); i++)
        {
            if(bestMoveList.get(i).score == bestScore)
                bestMove = bestMoveList.get(i).move;
        }
        return bestMove;
    }


    /**
     * An implementation of a mini-max algorithm that uses alpha-Beta pruning and a
     * depth limited searh in combination with an evaluation function to
     * @param board the state of the game board when calling minimax
     * @param player the player whose turn it is, MIN player or MAX player.
     * @param d the depth of the depth limited search to be terminated at.
     * @param a alpha
     * @param b beta
     * @param bestMoveList a list of move-score pairs, to be sorted later.
     * @return the best move which maximises score for the player.
     */
    int alphaBetaMiniMax(int board[], int player, int d, int a, int b)
    {
        int winner = checkForWin(board); //-1 if no one, 1 if player, 2 if opp. 3 if tie.
        //if game state is inactive.
        if(winner != -1)
        {
            if(winner == 1) {return Integer.MAX_VALUE;}
            else if(winner == 2) {return Integer.MIN_VALUE;}
            else {return 0;}
        }
        int bestVal = 0; int boardCpy[];
        //max depth reached, but game is still active.. Return the best move now.
        if(d == 0) {return evalScore(board);}

        if(player == MAXPLAYER)
        {
            bestVal = Integer.MIN_VALUE;
            for(int i = 0; i < 6; i++)
            {
                //nothing to move.
                if(board[i] == 0) {continue;}
                boardCpy = board.clone();
                boolean secondMove = playerMove(boardCpy, i); //MAXPLAYER gets another move.
                if(secondMove)
                    bestVal = Math.max(alphaBetaMiniMax(boardCpy, MAXPLAYER, d-1, a, b), bestVal);
                else
                    bestVal = Math.max(alphaBetaMiniMax(boardCpy, MINPLAYER, d-1, a, b), bestVal);

                a = Math.max(bestVal, a);

                if(d == this.depth)
                    this.bestMoveList.add(new MoveScorePair(i, bestVal));

                if(b <= a){break;} //beta-cutoff.

            }
        }
        else if(player == MINPLAYER)
        {
            bestVal = Integer.MAX_VALUE;
            for(int i = 7; i < 13; i++)
            {
                //nothing to move.
                if(board[i] == 0) {continue;}
                boardCpy = board.clone();
                boolean secondMove = opponentMove(boardCpy, i); //MAXPLAYER gets another move.
                if(secondMove)
                    bestVal = Math.min(alphaBetaMiniMax(boardCpy, MINPLAYER, d-1, a, b), bestVal);
                else
                    bestVal = Math.min(alphaBetaMiniMax(boardCpy, MAXPLAYER, d-1, a, b), bestVal);

                b = Math.min(bestVal, b);
                if(b <= a) {break;} //alpha cutoff.
            }
        }

        return bestVal;
    }


    /**
     * Update the gameboard accordingly after player has moved.
     * @param boardCpy copy of the game board.
     * @param i the movement selected by the player.
     */
    private boolean playerMove(int[] board, int mv)
    {
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


    /**
     * Update the gameboard accordingly after opponent has moved.
     * @param boardCpy copy of the game board.
     * @param i the movement selected by the player.
     */
    private boolean opponentMove(int[] board, int mv) {
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
        if(i!=13){return false;}
        return true;
    }




    /**
     * The agents name.
     * @return a hardcoded string, the name of the agent.
     */
    public String name()
    {
        return this.agentName;
    }

    /**
     * A method to reset the agent for a new game.
     */
    public void reset()
    {
        bestMoveList.clear();
    }


}