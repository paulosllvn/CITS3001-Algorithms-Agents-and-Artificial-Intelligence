package agents;

/**
 * class to store state of each individual player
 * retains information of the card that was played and the previous card played by each player 
 */
public class Player {

    private final int name;
    private int target;
    private int guess;
    private int card;
    private boolean success = false;
    private double[] probability;
    private int prevCard;
    private int prevTarget;

    //constructor
    public Player(int name){
        this.name = name;
    }

    /** method to get probability array 
     * @return array of card probabilities for player 
     */
    public double[] getProbability(){
        return probability;

    }

    /** method to set probability array 
     * @param newprobability sets the card probabilities for the player
     */
    public void setProbability(double[] newprobability){
        probability = newprobability;
    }

    /** method to get name of player
     * @return int representing name of player
     */
    public int getName() {
        return name;
    }

    /** method to return players target
     * @return player target
     */
    public int getTarget() {
        return target;
    }

    /** method to set players target
     * @param newtarget sets target to this value
     */
    public void setTarget(int newtarget){
        prevTarget = target;
        target = newtarget;
    }

    /** method to return players previous card
     * @return previous card 
     */
    public int getPrevCard(){
        return prevCard;
    }

    /** method to return players previous target
     * @return previous target
     */
    public int getPrevTarget(){
        return prevTarget;
    }

    /** method to see if player was successful
     * @return boolean success
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * method to set success boolean to true
     */
    public void setSuccess() {
        success = true;
    }

    /** method to see players guess
     * @return guess of player
     */
    public int getGuess() {
        return guess;
    }

    /** method to set players guess
     * @param newguess sets guess to this 
     */
    public void setGuess(int newguess){
        guess = newguess;
    }

    /** method to see value of players card
     * @return value of players card
     */
    public int getCard() {
        return card;
    }

    /** method to set players card value 
     * @param newcard sets card value to this
     */
    public void setCard(int newcard){
        prevCard = card;
        card = newcard;
    }
}

