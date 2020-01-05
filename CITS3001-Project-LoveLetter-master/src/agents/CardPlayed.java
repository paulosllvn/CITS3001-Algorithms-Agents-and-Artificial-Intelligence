package agents;

/**
 * class to store probability of outcome for choosing which card to play, and decides which is best
 */
public class CardPlayed {

    private double cardOutcome;
    private int card;
    private int name;
    private int guess;

    public double getCardOutcome() {
        return cardOutcome;
    }

    public void setCardOutcome(double newCardOutcome){
        cardOutcome = newCardOutcome;
    }

    /** method to get name of player
     * @return int representing name of player
     */
    public int getName() {
        return name;
    }

    /**
     *  method to set name of player
     * @param newName sets name
     */
    public void setName(int newName){
        name = newName;
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
    public void setGuess(int newGuess){
        guess = newGuess;
    }

    /**
     * method to return card in hand more likely to defeat opponent
     * @return card in hand agent should choose for given target
     */
    public int getCardAssociated() {
        return card;
    }

    /**
     * method to set card in hand more likely to defeat opponent
     * @param newcard
     */
    public void setCardAssociated(int newcard){
        card = newcard;
    }
}

