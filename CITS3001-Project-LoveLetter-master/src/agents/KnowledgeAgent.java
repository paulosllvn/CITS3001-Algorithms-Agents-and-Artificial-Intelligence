package agents;
import loveletter.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Knowledge based agent
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
public class KnowledgeAgent implements Agent{

  private Random rand;
  private State current;
  private int myIndex;
  private Card[] cardState; //array of cards that is in remaining deck or in opponents decks 
  private boolean[] discarded; //tracks the cards that have been discarded

  //0 place default constructor
  public KnowledgeAgent(){
    rand  = new Random();
  }

  /**
   * Reports the agents name
   * */
  public String toString(){return "Test";}


  /**
   * Method called at the start of a round
   * @param start the starting state of the round
   **/
  public void newRound(State start){
    current = start;
    myIndex = current.getPlayerIndex();
  }

  /**
   * Method called when any agent performs an action.
   * @param act the action an agent performs
   * @param results the state of play the agent is able to observe.
   * **/
  public void see(Action act, State results){
    current = results;
  }

  /**
   * deals the hypothetical deck 
   */
  private void init(){
    cardState = Card.deal(rand);
  }

  /**
   * method to track which cards have been discarded from the deck 
   */
  private void discardUpdate(){
       discarded = new boolean[cardState.length];
       for(int i=0; i<16; i++){
         discarded[i] = false;
       }
       for(int i=0; i<current.numPlayers(); i++){
         java.util.Iterator<Card> iter = current.getDiscards(i);
         while(iter.hasNext()){
          Card current = iter.next();
          for(int j=0; j<cardState.length; j++){
            if(discarded[j] == false && cardState[j] == current){
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
  * @param cardOne currently possessed card
  * @param cardTwo dr
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

    return probability;
  }

  /**
   * returns the card with the highest chance of being in the remaining deck
   * @param cardOne first card in agents hand
   * @param cardTwo second card in agents hand
   * @return Card with the highest probability of being in the deck 
   */
  private Card probabilityOfCards(int cardOne, int cardTwo){
    int[] counter = new int[8]; //array to count amount of each card
    for(int i=0; i<8; i++){
      counter[i] = 0;
    }
    cardOne = cardOne-1;
    cardTwo = cardTwo-1;
    for(int i=0; i<8; i++){
      if(cardOne == i){
        counter[i]--;
      }
      if(cardTwo == i){
        counter[i]--;
      }
    }
    for(int i=0; i<cardState.length; i++){
      if(discarded[i]==false){
        Card currentCard = cardState[i];

        if(currentCard.value() == 2){
          counter[1]++;
        }
        if(currentCard.value() == 3){
          counter[2]++;
        }
        if(currentCard.value() == 4){
          counter[3]++;
        }
        if(currentCard.value() == 5){
          counter[4]++;
        }
        if(currentCard.value() == 6){
          counter[5]++;
        }
        if(currentCard.value() == 7){
          counter[6]++;
        }
        if(currentCard.value() == 8){
          counter[7]++;
        }
      }
    }

    int maxChance = -1;
    int maxChanceIdentity = -1;
    for(int i=0; i<counter.length; i++){
      if(maxChance<counter[i]){
        maxChance = counter[i];
      }
    }

    ArrayList<Integer> temparray = new ArrayList<>();

    for(int i=0; i<counter.length; i++){
      if(counter[i] == maxChance && i !=0){
        temparray.add(i);
      }
    }

    maxChanceIdentity = temparray.get(rand.nextInt(temparray.size()));

    maxChanceIdentity += 1;
    Card guess = cardState[0];
    for(int i=0; i<cardState.length; i++){
      Card guessCard = cardState[i];
      if(maxChanceIdentity == guessCard.value()){
        guess = guessCard;
      }
    }
    return guess;
  }

  /**
   * Function that checks if a target is protected by the handmaiden. If protected, choose another target
   * @return random target not protected by handmaiden
   */
  private int handmaidenTarget(){
    int target = rand.nextInt(current.numPlayers());
    int count = 0;
    for(int k = 0; k<current.numPlayers();k++){
      if(current.eliminated(k)) count++;
    }
    for(int i=0; i<current.numPlayers(); i++){
      if(count>2) {
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
   * @return -1 if no target is found
   * @return target player id if target is identified
   */
  private int knownTarget(int biggestThreat) {
    int target = -1;   // Set target to a number other than 0-3
    if(biggestThreat > -1 && current.getCard(biggestThreat) != null && !current.handmaid(biggestThreat) && !current.eliminated(biggestThreat)){
      target = biggestThreat;
    }
    else {
      for (int i = 0; i < current.numPlayers(); i++) {                          // Loop through players
        if (current.getCard(i) != null) {                                       // If a players card is known for some reason.
          if (i != current.getPlayerIndex() && !current.handmaid(i) && !current.eliminated(i)) {    //Target this player if they not eliminated (null), are not targetting themselves and are not protected by the handmaid,
            if (current.getCard(i).value() == 1) {
              if (target != -1) { //if a target is already known that is not a guard, continue the loop without setting target to a guard
                continue;       //if a guard gets through, it will be handled in the playCard method
              }
            }
            target = i;
          }
        } else {
          target = -1;                                                          //else, keep target at -1
        }
      }
    }
    return target;
  }

  /**
   * Function that identifies a target if the current player plays a priest
   * Does not target the player if they are protected by the handmaiden
   * @return -1 if no target is found
   * @return target player id if target is identified
   */
  private int priestTarget(int biggestThreat) {
    int target = -1;

    if(biggestThreat > -1) {
      if (current.getCard(biggestThreat) == null && biggestThreat != current.getPlayerIndex() && !current.handmaid(biggestThreat) && !current.eliminated(biggestThreat)) {
        target = biggestThreat;
      }
    }
    else {
      for (int i = 0; i < current.numPlayers(); i++) {
        if (current.getCard(i) == null && i != current.getPlayerIndex() && !current.handmaid(i) && !current.eliminated(i)) {

          target = i;   //If a potential target is not you, their card is known and are not protected by handmaid
        }
      }
    }

    return target;    //Else no target found.
  }

  /**
   * Identifies the player with the most amount of hearts and targets them as they are the biggest threat.
   * @return target or -1 if target can't be found.
   */
  private ArrayList<Integer> biggestThreat(){
    int target = -1;
    int tempScore = 0;

    ArrayList<Integer> biggestthreats = new ArrayList<>();

    for (int i = 0; i < current.numPlayers(); i++) {
      if(current.score(i)==0 || i == current.getPlayerIndex() || current.handmaid(i) || current.eliminated(i)) continue;
      if(current.score(i)>tempScore){
        tempScore = current.score(i);
      }
    }
      for (int i = 0; i < current.numPlayers() ; i++) {
          if(current.score(i)!=0 && i != current.getPlayerIndex() && !current.handmaid(i) && current.score(i) == tempScore){
              biggestthreats.add(i);
          }
      }
      if(biggestthreats.size() != 0){
          target = biggestthreats.get(rand.nextInt(biggestthreats.size()));
      }

    return biggestthreats;
  }

  /**
   * This method is called when an agent possesses an attacking card (guard, baron, prince)
   * and chooses the target who has the highest probability of being eliminated based on the agents card 
   * @param cardOne
   * @param cardTwo
   * @param biggestThreat
   * @param playerProb
   * @return CardPlayed 
   */
  public CardPlayed decisionmakerAttack(int cardOne, int cardTwo, ArrayList<Integer> biggestThreat, double[] playerProb) {

      Map<Integer, CardPlayed> threats = new HashMap<>();
      CardPlayed target = null;
      if(biggestThreat.size()>0) {
        int targetvalue = biggestThreat.get(rand.nextInt(biggestThreat.size()));

        threats.put(1, new CardPlayed());
        threats.get(1).setName(targetvalue);
        threats.get(1).setCardAssociated(Math.min(cardOne, cardTwo));

        double probKilling = 0.0;
        int bestCard = 0;
        int guess = -1;

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
        if ((cardOne == 5 || cardTwo == 5) && (cardOne != 7 && cardTwo != 7)) { //prince without countess
          if (playerProb[7] > probKilling) {
            probKilling = playerProb[playerProb.length - 1];
            bestCard = 5;
          }
        }

        threats.get(1).setCardAssociated(bestCard);
        threats.get(1).setCardOutcome(probKilling);
        if (guess != -1) threats.get(1).setGuess(guess);

        target = threats.get(1);
      }
    return target;
  }

  /**
   * A method that is called when an agent possesses two non attacking cards (prince, priest, king)
   * choosing a target with the highest probability of having a higher card than the agents remaining card
   * @param cardOne
   * @param cardTwo
   * @param potentialTargets
   * @param playerProb
   * @return CardPlayed 
   */
  public CardPlayed decisionDefense(int cardOne, int cardTwo, ArrayList<Integer> potentialTargets, double[] playerProb) {

    Map<Integer, CardPlayed> threats = new HashMap<>();
    CardPlayed target = null;
    if(potentialTargets.size()>0) {
      int targetvalue = potentialTargets.get(rand.nextInt(potentialTargets.size()));

      threats.put(1, new CardPlayed());
      threats.get(1).setName(targetvalue);
      threats.get(1).setCardAssociated(Math.min(cardOne, cardTwo));

      double probDying = 0.0;
      int bestCard = 0;
      int guess = -1;

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
      threats.get(1).setCardAssociated(bestCard);
      threats.get(1).setCardOutcome(probDying);
      }
      target = threats.get(1);

    return target;
  }

  /**
   * Perform an action after drawing a card from the deck
   * @param c the card drawn from the deck
   * @return the action the agent chooses to perform
   * @throws IllegalActionException when the Action produced is not legal.
   * */
  public Action playCard(Card c){
    init();
    discardUpdate();
    Action act = null;
    Card play;
    Card guess;
    int priestTarget,nonPriestTarget;

    int biggestThreat = -1;
    ArrayList<Integer> bT = biggestThreat();
    if (bT.size() > 0) {
      biggestThreat = bT.get(rand.nextInt(bT.size()));
    }
    double[] probability = probAfterDiscard(c.value(), current.getCard(myIndex).value());

    int counter =0;
    while(!current.legalAction(act, c)){

      priestTarget = priestTarget(biggestThreat);
      nonPriestTarget = knownTarget(biggestThreat);

      counter++;
      if(c.value()<=current.getCard(myIndex).value()){ play= c;} //second card
      else{ play = current.getCard(myIndex);} //first card
      if(counter > 5){ //if it's illegal try choosing the other card
        if(rand.nextDouble()<0.5) play= c;
        else play = current.getCard(myIndex);
      }

      guess = probabilityOfCards(c.value(), current.getCard(myIndex).value());

      //If target is known, and you draw or already possess a Prince, play the Prince against them if they have a higher card value
      if(nonPriestTarget != -1){
        if(c.value() == 5){
          if(current.getCard(nonPriestTarget).value() > 4){
            play = c;
          }
        }
        else if(current.getCard(myIndex).value() == 5){
          if(current.getCard(nonPriestTarget).value() > 4){
            play = current.getCard(myIndex);
          }
        }
      }

      //If target is known, and you draw or already possess a King, play the King against them if they have a higher card value
      if(nonPriestTarget != -1){
        if(c.value() == 6){
          if(current.getCard(nonPriestTarget).value() > 6){
            play = c;
          }
        }
        else if(current.getCard(myIndex).value() == 6){
          if(current.getCard(nonPriestTarget).value() > 6){
            play = current.getCard(myIndex);
          }
        }
      }

      if((c.value() == 7 || current.getCard(myIndex).value()==7) && ((c.value() == 5 || current.getCard(myIndex).value()==6) ||
              (current.getCard(myIndex).value()==5 || c.value()==6))){
        if(c.value()==7) play = c;
        else play = current.getCard(myIndex);
      }

      //If target is known, and you draw or already possess a Baron, play the Baron against this target if you have the higher card
      if(nonPriestTarget != -1){
        if(c.value() == 3){
          if(current.getCard(myIndex).value() > current.getCard(nonPriestTarget).value()){
            play = c;
          }
        }
        else if(current.getCard(myIndex).value() == 3){
          if(c.value() > current.getCard(nonPriestTarget).value()){
            play = current.getCard(myIndex);
          }
        }
      }

      //If a target is known and is not a guard, and you draw or already possess a Guard, play the guard against this target
      if(nonPriestTarget != -1 && current.getCard(nonPriestTarget).value() != 1){
        if(c.value() == 1) {
          play = c;
          guess = current.getCard(nonPriestTarget);
        }
        else if(current.getCard(myIndex).value() == 1) {
          play = current.getCard(myIndex);
          guess = current.getCard(nonPriestTarget);
        }
      }

      //If target not known and you possess a guard and a priest, play the priest first
      if(nonPriestTarget == -1){
        if(c.value() == 1 && current.getCard(myIndex).value() == 2){
          play = current.getCard(myIndex);
        }
        else if(c.value() == 2 && current.getCard(myIndex).value() == 1){
          play = c;
        }
      }

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

      ArrayList<Integer> targets = new ArrayList<>();
      for (int i = 0; i < current.numPlayers(); i++) {
        if (i != current.getPlayerIndex() && !current.eliminated(i) && !current.handmaid(i)) {
          targets.add(i);
        }
      }

      if(nonPriestTarget == -1 && !isattack){
        CardPlayed target = decisionDefense(c.value(), current.getCard(myIndex).value(),targets,probability);
        if(target != null){
          if(target.getCardAssociated() != 0){
            play = Card.values()[target.getCardAssociated()-1];
            nonPriestTarget = target.getName();
          }
        }
      }

      //If known targets cannot be identified, target the biggest threat or choose a random non protected target.
      if(nonPriestTarget == -1) {
        if (biggestThreat == -1 || current.handmaid(biggestThreat) || biggestThreat == current.getPlayerIndex()
                || current.eliminated(biggestThreat)){
          nonPriestTarget = handmaidenTarget();
        }
        else nonPriestTarget = biggestThreat;
      }
      if(priestTarget == -1)priestTarget = handmaidenTarget();

      //always play the handmaid if you have one unless you know a target
      if(nonPriestTarget == -1){
        if(c.value()==4){ play= c;} //second card
        else if(current.getCard(myIndex).value() == 4) //first card
        {
          play= current.getCard(myIndex);
        }
      }


      try{
        switch(play){
          case GUARD:
            //Card guess = probabilityOfCards();
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
      }catch(IllegalActionException e){/*do nothing, just try again*/}
    }
    return act;
  }
}

