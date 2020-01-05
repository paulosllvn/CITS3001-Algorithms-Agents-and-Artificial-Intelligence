package agents;
import loveletter.*;
import java.math.*;
import java.security.KeyPair;
import java.util.*;

/**
 * Bayesian Agent 
 */
/** Heuristics:
 * Always target the player with the highest number of hearts if they are not protected by the handmaiden
 * Agent guesses highest probability card another left in deck if no players cards are known
 * Agent never targets a player protected by the handmaiden, themself (unless necessary), or eliminated players
 * The agent will always target a player via the guard if the card is known and is not a guard, and only via a baron if the known card is less than the agents other card
 * The agent will always target a play via the priest if that card is not yet known
 * The agent plays a prince against a target with a known high card target 
 * The agent plays a king against a target with a known higher card value than them
 * The agent will play their lower card (to retain their higher card), unless any of the above clauses override this
 * The agent will play the handmaiden if there is no known target
 */
/**
 * An agent developed for the game Love Letter 
 * @author Paul O'Sullivan 21492328 and Ashling Charles 22260738
 * */
public class BayesianAgent implements Agent {

    private Random rand;
    private State current;
    private int myIndex;
    private Card[] cardState; //array of cards that is in remaining deck or in opponents decks
    private boolean[] discarded; //tracks the cards that have been discarded

    private Map<Integer, Player> playerMap = new HashMap<>();
    private int roundCount = 0;

    //0 place default constructor
    public BayesianAgent() {
        rand = new Random();
        for (int i = 0; i < 4; i++) {
            playerMap.put(i, new Player(i));
        }
    }

    /**
     * Reports the agents name
     */
    public String toString() {
        return "Test";
    }

    /**
     * Method called at the start of a round
     *
     * @param start the starting state of the round
     **/
    public void newRound(State start) {
        current = start;
        myIndex = current.getPlayerIndex();
        playerMap.clear();
        for (int i = 0; i < 4; i++) {
            playerMap.put(i, new Player(i));
        }
        roundCount = 0;

    }

    /**
     * Method called when any agent performs an action.
     * Adds the action taken by each player (card player, card guessed, target)
     * per round and stores it into the player object
     * @param act     the action an agent performs
     * @param results the state of play the agent is able to observe.
     **/
    public void see(Action act, State results) {
        current = results;
        int card = act.card().value();
        Player currentplayer = playerMap.get(act.player());
        if ((card == 1 || card == 2 || card == 3 || card == 5 || card == 6)) { //only store target for cards that
            currentplayer.setTarget(act.target());                              //have a target
        }
        if (currentplayer != null) {
            currentplayer.setCard(act.card().value());

            if (act.card().value() == 1) {
                currentplayer.setGuess(act.guess().value());
            }
            if (act.card().value() == 1 || act.card().value() == 3) {
                if (current.eliminated(act.target())) {
                    currentplayer.setSuccess();
                }
            }
        }
    }
    
    /**
    * deals the hypothetical deck 
    */
    private void init() {
        cardState = Card.deal(rand);
    }

    /**
     *  function to swap probabilities when a king is played between the player that palyed the king and the target
     * @param player who played king
     * @param target
     */
    private void kingMove(Player playedking, Player target) {
        double[] swap = playedking.getProbability();
        playedking.setProbability(target.getProbability());
        target.setProbability(swap);
    }

    /**
    * divides all probabilities by the sum of probabilities for the player
     * sets probabilities to -1 if they are eliminated
    */
    public void updateProbRatios() {
        for (int i = 0; i < playerMap.size(); i++) {
            double sum = 0.0;
            for (int j = 0; j < playerMap.get(i).getProbability().length; j++) {
                if (playerMap.get(i).getProbability()[j] < 0) {
                    playerMap.get(i).getProbability()[j] = 0.0;
                }
            }
            for (int j = 0; j < playerMap.get(i).getProbability().length; j++) {
                sum = sum + playerMap.get(i).getProbability()[j];
            }
            for (int j = 0; j < playerMap.get(i).getProbability().length; j++) {
                playerMap.get(i).getProbability()[j] = ((playerMap.get(i).getProbability()[j] / sum));
            }

            for (int j = 0; j < playerMap.get(i).getProbability().length; j++) {
                if (current.eliminated(i)) playerMap.get(i).getProbability()[j] = -1.0;
            }
        }
    }

    /**
     * A method that updates the probabilities using inductive and deductive logic for each player.
     * @param deckProbability current probability of the deck based on discarded card pile
     * @param cardOne current card
     * @param cardTwo drawn card
     */
    public void updateProbabilities(double[] deckProbability, int cardOne,int cardTwo) {
        for (int i = 0; i < playerMap.size(); i++) { //iterates through each player
            Player player = playerMap.get(i);
            /*If a player has been eliminated in a past round do not account for their previous actions in the hashmap
            as they are old actions and have no impact on the current round */
            if (player.getPrevCard() == player.getCard() && player.getTarget() == player.getPrevTarget() && current.eliminated(player.getName())) {
                continue;
            }

            //gets the card that the player we are accessing has just played 
            int card = player.getCard();

            //If a player plays a baron and is successful, then their other card cannot be less than the loser's discarded card
            if (card == 3 && player.getSuccess()) {

                //Get the card of the player that was eliminated
                int cardremoved = current.getDiscards(player.getTarget()).next().value();
                for (int j = 0; j < cardremoved; j++) {
                    player.getProbability()[j] = 0.0; //updates probabilites - no chance of being a card less than target
                }
            }
            //If a player plays a baron and is unsuccessful, then the targets card cannot be less than the loser's discarded card
            if (card == 3 && current.eliminated(player.getName())) {
                int cardremoved = current.getDiscards(player.getName()).next().value();
                for (int j = 0; j < cardremoved; j++) {
                    playerMap.get(player.getTarget()).getProbability()[j] = 0.0; //updates probabilities of target 
                }
            }
            //If a player plays a King, swap their probabilities and adjust probs assuming their new card must be >king (rational)
            //Or if a player plays a prince update probabilities to zero for everything below this
            if (card == 6 || card == 5) {
                if (card == 6) {
                    kingMove(player, playerMap.get(player.getTarget()));
                    for (int j = 0; j < 6; j++) {
                        player.getProbability()[j] *= 0.2;
                    }
                } else if (card == 5) {
                    for (int j = 0; j < 5; j++) {
                        player.getProbability()[j] *= 0.2;
                    }
                }
            }
            //If a player plays a countess, then there is no reason for them not to have anything less than a prince
            if (card == 7) {
                for (int j = 0; j < 4; j++) {
                    player.getProbability()[j] *= 0.1;
                }
            }
            int[] counter = new int[8]; //array to count amount of each card
            for (int j = 0; j < 8; j++) {
                counter[j] = 0;
            }

            cardOne = cardOne - 1;
            cardTwo = cardTwo - 1;
            for (int k = 0; k < 8; k++) {
                if (cardOne == i) {
                    counter[k]--;
                }
                if (cardTwo == i) {
                    counter[k]--;
                }
            }
            for (int l = 0; l < cardState.length; l++) {
                if (discarded[l] == false) {
                    Card currentCard = cardState[l];
                    if (currentCard.value() == 1) {
                        counter[0]++;
                    }
                    if (currentCard.value() == 2) {
                        counter[1]++;
                    }
                    if (currentCard.value() == 3) {
                        counter[2]++;
                    }
                    if (currentCard.value() == 4) {
                        counter[3]++;
                    }
                    if (currentCard.value() == 5) {
                        counter[4]++;
                    }
                    if (currentCard.value() == 6) {
                        counter[5]++;
                    }
                    if (currentCard.value() == 7) {
                        counter[6]++;
                    }
                    if (currentCard.value() == 8) {
                        counter[7]++;
                    }
                }
            }
            //If a player guesses that you have a card, then assume that this player does not have the guess card if there only
            //remains one instance of this card type in the unseen deck of cards.
            if (card == 1 && player.getSuccess() && counter[player.getGuess()-1] == 1) {
                player.getProbability()[player.getGuess() - 1] *= 0.1;
            }

            //If a player guesses incorrectly, set the probability that the target has they guess card to 0.
            if (card == 1 && !player.getSuccess()) {
                playerMap.get(player.getTarget()).getProbability()[player.getGuess() - 1] = 0.0;
            }
            //Assume opponent plays lowest card 70% of the time
            for (int j = 0; j < card; j++) {
                player.getProbability()[j] *= 0.3;
            }
        }
    }

    /**
    * method to track which cards have been discarded from the deck 
    */
    private void discardUpdate() {
        discarded = new boolean[cardState.length];
        for (int i = 0; i < 16; i++) {
            discarded[i] = false;
        }
        for (int i = 0; i < current.numPlayers(); i++) {
            java.util.Iterator<Card> iter = current.getDiscards(i);
            while (iter.hasNext()) {
                Card current = iter.next();
                for (int j = 0; j < cardState.length; j++) {
                    if (discarded[j] == false && cardState[j] == current) {
                        discarded[j] = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * method that returns an array with probability of cards being in the deck
     * after considering the discard pile and cards in agents hand
     * @param cardOne current card
     * @param cardTwo drawn card
     * @return array of card probabilities of type double 
     */
    public double[] probAfterDiscard(int cardOne, int cardTwo) {
        int[] counter = new int[8]; //array to count amount of each card
        double[] probability = new double[8];
        for (int i = 0; i < 8; i++) {
            counter[i] = 0;
        }

        cardOne = cardOne - 1;
        cardTwo = cardTwo - 1;
        for (int i = 0; i < 8; i++) {
            if (cardOne == i) {
                counter[i]--;
            }
            if (cardTwo == i) {
                counter[i]--;
            }
        }
        for (int i = 0; i < cardState.length; i++) {
            if (discarded[i] == false) {
                Card currentCard = cardState[i];
                if (currentCard.value() == 1) {
                    counter[0]++;
                }
                if (currentCard.value() == 2) {
                    counter[1]++;
                }
                if (currentCard.value() == 3) {
                    counter[2]++;
                }
                if (currentCard.value() == 4) {
                    counter[3]++;
                }
                if (currentCard.value() == 5) {
                    counter[4]++;
                }
                if (currentCard.value() == 6) {
                    counter[5]++;
                }
                if (currentCard.value() == 7) {
                    counter[6]++;
                }
                if (currentCard.value() == 8) {
                    counter[7]++;
                }
            }
        }

        double sum = 0;
        for (int i = 0; i < counter.length; i++) {
            sum = sum + counter[i];
        }

        for (int i = 0; i < counter.length; i++) {
            probability[i] = counter[i] / sum;
        }

        //stores these probabilities for each player 
        for (int i = 0; i < playerMap.size(); i++) {
            if (playerMap.get(i) != null) {
                double[] probabilitytemp = new double[8];
                for (int j = 0; j < probability.length; j++) {

                    probabilitytemp[j] = probability[j];
                }
                playerMap.get(i).setProbability(probabilitytemp);
            }
        }
        return probability;
    }

    /**
    * returns the card with the highest chance of being in the remaining deck
    * @param cardOne first card in agents hand
    * @param cardTwo second card in agents hand
    * @return Card with the highest probability of being in the deck 
    */
    private Card probabilityOfCards(int cardOne, int cardTwo) {
        int[] counter = new int[8]; //array to count amount of each card
        for (int i = 0; i < 8; i++) {
            counter[i] = 0;
        }
        cardOne = cardOne - 1;
        cardTwo = cardTwo - 1;
        for (int i = 0; i < 8; i++) {
            if (cardOne == i) {
                counter[i]--;
            }
            if (cardTwo == i) {
                counter[i]--;
            }
        }
        for (int i = 0; i < cardState.length; i++) {
            if (discarded[i] == false) {
                Card currentCard = cardState[i];

                if (currentCard.value() == 2) {
                    counter[1]++;
                }
                if (currentCard.value() == 3) {
                    counter[2]++;
                }
                if (currentCard.value() == 4) {
                    counter[3]++;
                }
                if (currentCard.value() == 5) {
                    counter[4]++;
                }
                if (currentCard.value() == 6) {
                    counter[5]++;
                }
                if (currentCard.value() == 7) {
                    counter[6]++;
                }
                if (currentCard.value() == 8) {
                    counter[7]++;
                }
            }
        }

        int maxChance = -1;
        int maxChanceIdentity = -1;
        for (int i = 0; i < counter.length; i++) {
            if (maxChance < counter[i]) {
                maxChance = counter[i];
            }
        }

        ArrayList<Integer> temparray = new ArrayList<>();

        for (int i = 0; i < counter.length; i++) {
            if (counter[i] == maxChance && i != 0) {
                temparray.add(i);
            }
        }

        maxChanceIdentity = temparray.get(rand.nextInt(temparray.size()));

        maxChanceIdentity += 1;
        Card guess = cardState[0];
        for (int i = 0; i < cardState.length; i++) {
            Card guessCard = cardState[i];
            if (maxChanceIdentity == guessCard.value()) {
                guess = guessCard;
            }
        }
        return guess;
    }

    /**
     * Function that checks if a target is protected by the handmaiden. If protected, choose another target
     * @return random target not protected by handmaiden
     */
    private int handmaidenTarget() {
        int target = rand.nextInt(current.numPlayers());
        int count = 0;
        for (int k = 0; k < current.numPlayers(); k++) {
            if (current.eliminated(k)) count++;
        }
        for (int i = 0; i < current.numPlayers(); i++) {
            if (count > 2) {
                while (current.handmaid(i) == true && target == i || i != current.getPlayerIndex()) {
                    for (int j = 0; j < 4; j++) {
                    }
                    target = rand.nextInt(current.numPlayers());
                }
            }
        }
        return target;
    }

    /**
     * Function that identifies a target due to our agent knowing what card the target has
     * Does not target the player if they are protected by the handmaiden
     * @return target player id if target is identified
     */
    private int knownTarget(int biggestThreat) {
        int target = -1;
        if (biggestThreat > -1 && current.getCard(biggestThreat) != null && !current.handmaid(biggestThreat) && !current.eliminated(biggestThreat)) {
            target = biggestThreat;
        } else {
            for (int i = 0; i < current.numPlayers(); i++) {
                if (current.getCard(i) != null) {
                    if (i != current.getPlayerIndex() && !current.handmaid(i) && !current.eliminated(i)) {
                        if (current.getCard(i).value() == 1) {
                            if (target != -1) {
                                continue;
                            }
                        }
                        target = i;
                    }
                } else {
                    target = -1;
                }
            }
        }
        return target;
    }

    /**
     * Function that identifies a target if the current player plays a priest
     * Does not target the player if they are protected by the handmaiden
     * @return target player id if target is identified
     */
    private int priestTarget(int biggestThreat) {
        int target = -1;
        if (biggestThreat > -1) {
            if (current.getCard(biggestThreat) == null && biggestThreat != current.getPlayerIndex() && !current.handmaid(biggestThreat) && !current.eliminated(biggestThreat)) {
                target = biggestThreat;
            }
        } else {
            for (int i = 0; i < current.numPlayers(); i++) {
                if (current.getCard(i) == null && i != current.getPlayerIndex() && !current.handmaid(i) && !current.eliminated(i)) {

                    target = i;
                }
            }
        }
        return target;
    }


    /**
     * Identifies the player with the most amount of hearts and targets them as they are the biggest threat.
     * @return target or -1 if target can't be found.
     */
    private ArrayList<Integer> biggestThreat() {
        int target = -1;
        int tempScore = 0;

        ArrayList<Integer> biggestthreats = new ArrayList<>();

        for (int i = 0; i < current.numPlayers(); i++) {
            if (i == current.getPlayerIndex() || current.handmaid(i) || current.eliminated(i)) continue;
            if (current.score(i) > tempScore) {
                tempScore = current.score(i);
            }
        }
        for (int i = 0; i < current.numPlayers(); i++) {
            if (!current.eliminated(i) && i != current.getPlayerIndex() && !current.handmaid(i) && current.score(i) == tempScore) {
                biggestthreats.add(i);
            }
        }
        return biggestthreats;
    }

    /**
     * Receives both cards and analyses the probability of winning players, Then determines which player to target based
     * on the cards possessed by our agent.
     * @param cardOne first card in agents deck
     * @param cardTwo second card in agents deck
     * @param biggestThreat arraylist of biggest threat added last
     * @param probDeck deck probabilities after being updated
     * @return CardPlayed object which can be used to find the value of the player to be targetted and which card to play
     * on them
     */
    public CardPlayed decisionmakerAttack(int cardOne, int cardTwo, ArrayList<Integer> biggestThreat, double[] probDeck) {
        Map<Integer, CardPlayed> threats = new HashMap<>();
        CardPlayed target = null;
        if (biggestThreat.size() > 0) {
            for (int i = 0; i < biggestThreat.size(); i++) {
                threats.put(i, new CardPlayed());
                threats.get(i).setName(biggestThreat.get(i));
                threats.get(i).setCardAssociated(Math.min(cardOne, cardTwo));
            }
            for (int j = 0; j < threats.size(); j++) {
                double probKilling = 0.0;
                int bestCard = 0;
                double probDying = 0.0;
                int guess = -1;
                double[] playerProb = playerMap.get(biggestThreat.get(j)).getProbability();

                if (cardOne == 1 || cardTwo == 1) { //guards
                    for (int i = 0; i < playerProb.length; i++) {
                        if (playerProb[i] > probKilling && i != 0) {
                            if (cardOne == 1) {
                                probKilling = playerProb[i];
                                bestCard = cardOne;
                                guess = i + 1;
                            } else {
                                probKilling = playerProb[i];
                                bestCard = cardTwo;
                                guess = i + 1;
                            }
                        }
                    }
                }
                if (cardOne == 3 || cardTwo == 3) { //barons
                    if (cardOne == 3) {
                        double tempProb = 0.0;
                        for (int i = 0; i < playerProb.length; i++) {
                            if (i < cardTwo - 1) {
                                tempProb += playerProb[i];
                            }
                        }
                        if (tempProb > probKilling) {
                            probKilling = tempProb;
                            bestCard = cardOne;
                        }
                    }
                    if (cardTwo == 3) {
                        double tempProb = 0.0;
                        for (int i = 0; i < playerProb.length; i++) {
                            if (i < cardOne - 1) {
                                tempProb += playerProb[i];
                            }
                        }
                        if (tempProb > probKilling) {
                            probKilling = tempProb;
                            bestCard = cardTwo;
                        }
                    }
                }
                if ((cardOne == 5 || cardTwo == 5) && (cardOne != 7 && cardTwo !=7)) { //prince without countess 
                    if (playerProb[7] > probKilling) {
                        probKilling = playerProb[playerProb.length - 1];
                        bestCard = 5;
                    }
                }

                threats.get(j).setCardAssociated(bestCard);
                threats.get(j).setCardOutcome(probKilling);
                if (guess != -1) threats.get(j).setGuess(guess);
            }

            //Choose the target with the highest chance of elimination
            double temp = -1;
            for (int i = 0; i < threats.size(); i++) {
                if (threats.get(i).getCardOutcome() > temp) {
                    temp = threats.get(i).getCardOutcome();
                    target = threats.get(i);
                }
            }
        }
        return target;
    }
    /**
     * Receive a card and analyses players with the hope of obtaining a high card or protection from a priest
     * @param cardOne first card in agents deck
     * @param cardTwo second card in agents deck
     * @param potentialTargets arraylist of all active opponents in the game
     * @return CardPlayed object which can be used to find the value of the player which should be targeted and
     * what card to use on them
     */
    public CardPlayed decisionDefense(int cardOne, int cardTwo, ArrayList<Integer> potentialTargets) {
        Map<Integer, CardPlayed> targets = new HashMap<>();
        CardPlayed target = null;

        if (potentialTargets.size() > 0) {
            for (int i = 0; i < potentialTargets.size(); i++) {
                    targets.put(i, new CardPlayed());
                    targets.get(i).setName(potentialTargets.get(i));
                    targets.get(i).setCardAssociated(Math.min(cardOne, cardTwo));
                }
            }
        if(targets.size() >0) {
            for (int i = 0; i < targets.size(); i++) {
                double probDying = 0.0;
                int bestCard = 0;
                double[] playerProb = playerMap.get(potentialTargets.get(i)).getProbability();

                if (cardOne == 4 && cardTwo < 4) {
                    if(playerProb[1] > 0.3) {
                        probDying = playerProb[1];
                        bestCard = cardOne;
                    }
                }
                if (cardTwo == 4 && cardOne < 4) {
                    if(playerProb[1] > 0.3) {
                        probDying = playerProb[1];
                        bestCard = cardTwo;
                    }
                }

                if ((cardOne == 6 || cardOne == 5) && cardTwo < 6) {
                    double temp = 0;
                    for (int j = cardTwo - 1; j < playerProb.length; j++) {
                        temp += playerProb[j];
                        if (temp > probDying) {
                            probDying = temp;
                            bestCard = cardOne;
                        }
                    }
                }
                if ((cardTwo == 6 || cardTwo == 5) && cardOne < 6) {
                    double temp = 0;
                    for (int j = cardOne - 1; j < playerProb.length; j++) {
                        temp += playerProb[j];
                        if (temp > probDying) {
                            probDying = temp;
                            bestCard = cardTwo;
                        }
                    }
                }
                targets.get(i).setCardAssociated(bestCard);

                targets.get(i).setCardOutcome(probDying);
            }

            if (targets.size() > 0) {
                double temp = -1.0;
                for (int i = 0; i < targets.size(); i++) {

                    if (targets.get(i).getCardOutcome() > temp && targets.get(i).getName() != current.getPlayerIndex()) {
                        temp = targets.get(i).getCardOutcome();
                        target = targets.get(i);
                    }
                }
            }
        }
        return target;

    }

    /**
     * Perform an action after drawing a card from the deck
     * @param c the card drawn from the deck
     * @return the action the agent chooses to perform
     * @throws IllegalActionException when the Action produced is not legal.
     */
    public Action playCard(Card c) {

        init();
        discardUpdate();
        Action act = null;
        Card play;
        Card guess;
        int priestTarget, nonPriestTarget;
        double[] probability = probAfterDiscard(c.value(), current.getCard(myIndex).value());
        roundCount++;

        int biggestThreat = -1;
        ArrayList<Integer> bT = biggestThreat();
        if (bT.size() != 0) {
            biggestThreat = bT.get(rand.nextInt(bT.size()));
        }

        updateProbabilities(probability,c.value(),current.getCard(myIndex).value());
        updateProbRatios();

        int counter = 0;
        while (!current.legalAction(act, c)) {

            priestTarget = priestTarget(biggestThreat);
            nonPriestTarget = knownTarget(biggestThreat);

            counter++;
            if (c.value() <= current.getCard(myIndex).value()) {
                play = c;
            } //second card
            else {
                play = current.getCard(myIndex);
            } //first card
            if (counter > 5) { //if it's illegal try choosing the other card
                if (rand.nextDouble() < 0.5) play = c;
                else play = current.getCard(myIndex);
            }

            guess = probabilityOfCards(c.value(), current.getCard(myIndex).value());

            //If target is known, and you draw or already possess a Prince, play the Prince against them if they have a higher card value
            if (nonPriestTarget != -1) {
                if (c.value() == 5) {
                    if (current.getCard(nonPriestTarget).value() > 4) {
                        play = c;
                    }
                } else if (current.getCard(myIndex).value() == 5) {
                    if (current.getCard(nonPriestTarget).value() > 4) {
                        play = current.getCard(myIndex);
                    }
                }
            }

            //If target is known, and you draw or already possess a King, play the King against them if they have a higher card value
            if (nonPriestTarget != -1) {
                if (c.value() == 6) {
                    if (current.getCard(nonPriestTarget).value() > 6) {
                        play = c;
                    }
                } else if (current.getCard(myIndex).value() == 6) {
                    if (current.getCard(nonPriestTarget).value() > 6) {
                        play = current.getCard(myIndex);
                    }
                }
            }
            //If a countess is drawn or posssessed and a king of prince is also possessed, play the countess.
            if ((c.value() == 7 || current.getCard(myIndex).value() == 7) && ((c.value() == 5 || current.getCard(myIndex).value() == 6) ||
                    (current.getCard(myIndex).value() == 5 || c.value() == 6))) {
                if (c.value() == 7) play = c;
                else play = current.getCard(myIndex);
            }

            //If target is known, and you draw or already possess a Baron, play the Baron against this target if you have the higher card
            if (nonPriestTarget != -1) {
                if (c.value() == 3) {
                    if (current.getCard(myIndex).value() > current.getCard(nonPriestTarget).value()) {
                        play = c;
                    }
                } else if (current.getCard(myIndex).value() == 3) {
                    if (c.value() > current.getCard(nonPriestTarget).value()) {
                        play = current.getCard(myIndex);
                    }
                }
            }

            //If a target is known and is not a guard, and you draw or already possess a Guard, play the guard against this target
            if (nonPriestTarget != -1 && current.getCard(nonPriestTarget).value() != 1) {
                if (c.value() == 1) {
                    play = c;
                    guess = current.getCard(nonPriestTarget);
                } else if (current.getCard(myIndex).value() == 1) {
                    play = current.getCard(myIndex);
                    guess = current.getCard(nonPriestTarget);
                }
            }

            //If target not known and you possess a guard and a priest, play the priest first
            if (nonPriestTarget == -1) {
                if (c.value() == 1 && current.getCard(myIndex).value() == 2) {
                    play = current.getCard(myIndex);
                } else if (c.value() == 2 && current.getCard(myIndex).value() == 1) {
                    play = c;
                }
            }
            //if a target is known and they possess a priest, play handmaid
            if(nonPriestTarget!= -1){
                if(current.getCard(nonPriestTarget).value() == 2){
                    if(c.value() == 4){
                        play = c;
                    }
                    if(current.getCard(myIndex).value() ==4){
                        play = current.getCard(myIndex);

                    }
                }
            }
            //Obtain an array list of all active playerse
            ArrayList<Integer> targets = new ArrayList<>();
            for (int i = 0; i < playerMap.size(); i++) {
                if (i != current.getPlayerIndex() && !current.eliminated(i) && !current.handmaid(i)) {
                    targets.add(playerMap.get(i).getName());
                }
            }

            boolean isattack = (c.value()==1 || c.value()==3 || c.value() ==5) || (current.getCard(myIndex).value()==1 ||
                    current.getCard(myIndex).value()==3 || current.getCard(myIndex).value()==5);
            //If known targets cannot be identified, perform probability analysis on opponents.
            if (nonPriestTarget == -1 && isattack) {
                CardPlayed target = decisionmakerAttack(c.value(), current.getCard(myIndex).value(), bT, probability);
                if(target != null) {

                    if(target.getCardAssociated()!=0) {
                        play = Card.values()[target.getCardAssociated() - 1];
                        nonPriestTarget = target.getName();

                        if(target.getGuess() != 0){
                            guess = Card.values()[target.getGuess()-1];
                        }
                    }
                }
            }
            //If agent does not possess an attacking card, utilise defensive cards
            if(nonPriestTarget == -1 && !isattack){
                CardPlayed target = decisionDefense(c.value(), current.getCard(myIndex).value(),targets);
                if(target != null){
                    if(target.getCardAssociated() != 0){

                        play = Card.values()[target.getCardAssociated()-1];
                        nonPriestTarget = target.getName();
                    }
                }
            }

                //If known targets cannot be identified, target the biggest threat or choose a random non protected target.
                if (nonPriestTarget == -1) {
                    if (biggestThreat == -1 || current.handmaid(biggestThreat) || biggestThreat == current.getPlayerIndex()
                            || current.eliminated(biggestThreat)) {
                        nonPriestTarget = handmaidenTarget();
                    } else nonPriestTarget = biggestThreat;
                }
                if (priestTarget == -1) priestTarget = handmaidenTarget();

                //always play the handmaid if you have one unless you know a target
                if(nonPriestTarget == -1){
                    if(c.value()==4){ play= c;} //second card
                    else if(current.getCard(myIndex).value() == 4) //first card
                    {
                    play= current.getCard(myIndex);
                    }
                }


                try {
                    switch (play) {
                        case GUARD:
                            act = Action.playGuard(myIndex, nonPriestTarget, guess);
                            break;
                        case PRIEST:
                            act = Action.playPriest(myIndex, priestTarget);
                            break;
                        case BARON:
                            act = Action.playBaron(myIndex, nonPriestTarget);
                            break;
                        case HANDMAID:
                            act = Action.playHandmaid(myIndex);
                            break;
                        case PRINCE:
                            act = Action.playPrince(myIndex, nonPriestTarget);
                            break;
                        case KING:
                            act = Action.playKing(myIndex, nonPriestTarget);
                            break;
                        case COUNTESS:
                            act = Action.playCountess(myIndex);
                            break;
                        default:
                            act = null;//never play princess
                    }
                } catch (IllegalActionException e) {System.out.println(e);}
            }
            return act;
        }
    }
