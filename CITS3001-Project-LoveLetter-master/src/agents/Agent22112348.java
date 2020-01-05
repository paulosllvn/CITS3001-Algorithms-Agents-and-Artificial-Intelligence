package agents;
import loveletter.*;
import java.util.Random;
import java.util.Iterator;
import java.lang.Math;

/**
 * @author Viet 22112348
 * To test my agent, make sure to import agent.* or agent.Agent22112348
 * Then call the constructor and run test
 */
public class Agent22112348 implements Agent{
    private Random rand;
    private State current;
    private int myIndex;
    private int ownCard = 0;
    private int newCard = 0;
    private int target = -1;
    private int guessCard = 0;
    
    //0 place default constructor
    public Agent22112348(){
        rand  = new Random();
    }

    public String toString(){return "Viet";}

    public void newRound(State start){
        current = start;
        myIndex = current.getPlayerIndex();
    }

    public void see(Action act, State results){
        current = results;
    }

    // Sub play card function return 1 on success, 0 fail
    private int playGuard(){
        double bestGuess = 0;
        int bestTarget = -1;
        int bestCard = 0;
        
        // Check if you play priest last turn
        boolean goodChance = false;
        for(int i = 0; i < current.numPlayers(); i++){
            // Check for not self, player not eliminated and not have handmaid protection
            if(i == myIndex || current.eliminated(i) || current.handmaid(i)){
                continue;
            }
            
            // Check if i play priest last turn and i can still see their cards(in case they play king or prince)
            if(current.getCard(i) != null){
                bestCard = current.getCard(i).value();
                bestTarget = i;
                break;
            }
            
            // Check if they last play countess, prioritize them
            if(playLast(i) == 7){
                double tempGuess = 0;
                for(int theCard = 5; theCard < 9; theCard++){
                    if(unplayedProb(theCard) > tempGuess){
                        tempGuess = unplayedProb(theCard);
                        bestCard = theCard;
                    }
                }
                if(tempGuess > 0){
                    goodChance = true;
                }
            }
            
            // Start comparing the possible target and their card possibilities
            if(goodChance){
                continue;
            }
            for(int theCard = 2; theCard < 9; theCard++){
                // Can not guess guard
                if(unplayedProb(theCard) > bestGuess){
                    bestGuess = unplayedProb(theCard);
                    bestTarget = i;
                    bestCard = theCard;
                }
            }
        }
        target = bestTarget;
        guessCard = bestCard;
        
        if(target != -1 && guessCard != 0){
            return 1;
        }
        return 0;
    }
    
    private int playPriest(){
        int bestTarget = -1;
        // Choose the player without hand maid
        for(int i = 0; i < current.numPlayers(); i++){
            // Check for not self, player not eliminated, not have handmaid protection and we have not use priest on before
            if(i == myIndex || current.eliminated(i) || current.handmaid(i) || current.getCard(i) != null){
                continue;
            }
                       
            bestTarget = i;
        }
        
        target = bestTarget;
        if(target != -1){
            return 1;
        }
        return 0;
    }
    
    private int playBaron(int highestValue){
        double bestGuess = 0;
        int bestTarget = -1;
        int bestCard = 0;
        for(int i = 0; i < current.numPlayers(); i++){
            // Check for not self, player not eliminated and not have handmaid protection
            if(i == myIndex || current.eliminated(i) || current.handmaid(i)){
                continue;
            }
            
            if(highestValue == 8){
                // Choose whoever
                bestTarget = i;
                break;
            }
            
            if(ownCard == 6 || newCard == 6){
                // We have King check if princess and countess is discarded
                if(unplayedProb(7) == 0 && unplayedProb(8) == 0){
                    bestTarget = i;
                    break;
                }
            }
            
            // Check for if last play priest
            if(current.getCard(i) != null){
                if(current.getCard(i).value() < highestValue){
                    bestTarget = i;
                    break;
                }
            }
            
            // They last play countess means they prob have high card
            if(playLast(i) == 7){
                continue;
            }
        }
        
        target = bestTarget;
        
        if(target != -1){
            return 1;
        }
        return 0;
    }
    
    private int playPrince(int theOtherCard){
        double bestGuess = 0;
        int bestTarget = -1;
        int bestCard = 0;
        for(int i = 0; i < current.numPlayers(); i++){
            // Check for not self, player not eliminated and not have handmaid protection
            if(i == myIndex || current.eliminated(i) || current.handmaid(i)){
                continue;
            }
            
            // Check for if last play priest
            if(current.getCard(i) != null){
                if(current.getCard(i).value() >= theOtherCard){
                    bestTarget = i;
                    break;
                }
            }
            
            // They play countess last means prob have higher card worth discarding
            if(playLast(i) == 7){
                bestTarget = i;
                break;
            }else{
                if(playLast(i) == 3){
                    // Compare means they prob have high value card
                    bestTarget = i;
                    bestCard = 3;
                    //Not break incase countess
                }else if(playLast(i) == 6 && bestCard != 3){
                    bestTarget = i;
                    // Not break incase countess or baron
                }
            }
        }
        
        target = bestTarget;
        if(target != -1){
            return 1;
        }
        return 0;
    }
    
    private int playKing(int theOtherCard){
        double bestGuess = 0;
        int bestTarget = -1;
        int bestCard = 0;
        for(int i = 0; i < current.numPlayers(); i++){
            // Check for not self, player not eliminated and not have handmaid protection
            if(i == myIndex || current.eliminated(i) || current.handmaid(i)){
                continue;
            }
            
            // Check for if last play priest
            if(current.getCard(i) != null){
                if(current.getCard(i).value() >= theOtherCard){
                    bestTarget = i;
                    break;
                }
            }
            
            // They play countess last means prob have higher card worth trading
            if(playLast(i) == 7){
                bestTarget = i;
                bestCard = 7;
                break;
            }else{
                if(playLast(i) == 3){
                    // Compare means they prob have high value card
                    bestTarget = i;
                    bestCard = 3;
                    //Not break incase countess
                }
            }
        }
        
        target = bestTarget;
        guessCard = bestCard;
        if(target != -1){
            return 1;
        }
        return 0;
    }
    
    public Action playCard(Card c){
        target = -1;
        guessCard = 0;
        Action act = null;
        Card play = current.getCard(myIndex);
        
        ownCard = play.value();
        newCard = c.value();
        
        int target = -1;
        int guessCard = 0;
        // Never play princess
        double bestGuess = 0;
        int bestTarget = -1;
        int bestCard = 0;
        if(play.value() == 8){
            play = c;
            switch(play){
                case GUARD:
                    playGuard();
                    break;
                case PRIEST:
                    playPriest();
                    break;
                case BARON: 
                    playBaron(8);
                    break;
                case HANDMAID:
                    break;
                case PRINCE:  
                    playPrince(8);                    
                    break;
                case KING:
                    playKing(8);
                    break;
            }
        }else if(c.value() == 8){
            play = play;
            switch(play){
                case GUARD:
                    playGuard();
                    break;
                case PRIEST:
                    playPriest();
                    break;
                case BARON: 
                    playBaron(8);
                    break;
                case HANDMAID:
                    break;
                case PRINCE:  
                    playPrince(8);                    
                    break;
                case KING:
                    playKing(8);
                    break;
            }
        }else{
            // Check for force actions
            if(play.value() == 7 && c.value() > 4){
                play = play;
            }else if(c.value() == 7 && play.value() > 4){
                play = c;
            }else if(play.value() == c.value()){
                // Dupp card case
                switch(play){
                    case GUARD:
                        playGuard();
                        break;
                    case PRIEST:
                        playPriest();
                        break;
                    case BARON: 
                        playBaron(3);
                        break;
                    case HANDMAID:
                        break;
                    case PRINCE:  
                        playPrince(5);
                        break;
                }
            }else{
                // General action
                // No combination of countess and other card with value greater than 4
                // No dupp cards
                switch(play){
                    case GUARD:
                        switch(c){
                            case PRIEST:
                                if(playPriest() == 0){
                                    playGuard();
                                }else{
                                    // Play priest successful
                                    play = c;
                                }
                                break;
                            case BARON:  
                                // Def not playing Baron
                                playGuard();
                                break;
                            case HANDMAID:
                                playGuard();
                                break;
                            case PRINCE:  
                                playGuard();
                                break;
                            case KING:
                                playGuard();
                                break;
                            case COUNTESS:
                                playGuard();
                                break;
                        }
                        break;
                    case PRIEST:
                        switch(c){
                            case GUARD:
                                if(playPriest() == 0){
                                    playGuard();
                                    play = c;
                                }
                                break;
                            case BARON: 
                                // Try for baron
                                if(playBaron(2) == 0){
                                    // Baron fail
                                    playPriest();
                                }else{
                                    play = c;
                                }
                                break;
                            case HANDMAID:
                                playPriest();
                                break;
                            case PRINCE:  
                                playPriest();
                                break;
                            case KING:
                                if(playKing(2) == 0){
                                    playPriest();
                                }else{
                                    play = c;
                                }
                                break;
                            case COUNTESS:
                                playPriest();
                                break;
                        }
                        break;
                    case BARON: 
                        switch(c){
                            case GUARD:
                                playGuard();
                                play = c;
                                break;
                            case PRIEST:
                                // Try for baron
                                if(playBaron(2) == 0){
                                    // Baron fail
                                    playPriest();
                                    play = c;
                                }
                                break;
                            case HANDMAID:
                                playBaron(4);
                                break;
                            case PRINCE:  
                                // Try play prince
                                if(playPrince(3) == 0){
                                    playBaron(5);
                                }else{
                                    play = c;
                                }
                                break;
                            case KING:
                                playBaron(6);
                                break;
                            case COUNTESS:
                                playBaron(7);
                                break;
                        }
                        break;
                    case HANDMAID:
                        switch(c){
                            case GUARD:
                                playGuard();
                                play = c;
                                break;
                            case PRIEST:
                                playPriest();
                                play = c;
                                break;
                            case BARON: 
                                playBaron(4);
                                break;
                            case PRINCE:  
                                if(playPrince(4) != 0){
                                    play = c;
                                }
                                break;
                            case KING:
                                break;
                            case COUNTESS:
                                break;
                        }
                        break;
                    case PRINCE:  
                        switch(c){
                            case GUARD:
                                playGuard();
                                play = c;
                                break;
                            case PRIEST:
                                playPriest();
                                play = c;
                                break;
                            case BARON:  
                                // Try play prince
                                if(playPrince(3) == 0){
                                    playBaron(5);
                                    play = c;
                                }
                                break;
                            case HANDMAID:
                                if(playPrince(4) == 0){
                                    play = c;
                                }
                                break;
                            case KING:
                                playPrince(6);
                                break;
                        }
                        break;
                    case KING:
                        switch(c){
                            case GUARD:
                                playGuard();
                                play = c;
                                break;
                            case PRIEST:
                                playPriest();
                                play = c;
                                break;
                            case BARON:  
                                playBaron(6);
                                play = c;
                                break;
                            case HANDMAID:
                                play = c;
                                break;
                            case PRINCE: 
                                playPrince(6);
                                play = c;
                                break;
                        }
                        break;
                    case COUNTESS:
                        switch(c){
                            case GUARD:
                                playGuard();
                                play = c;
                                break;
                            case PRIEST:
                                playPriest();
                                play = c;
                                break;
                            case BARON:  
                                playBaron(7);
                                play = c;
                                break;
                            case HANDMAID:
                                if(turnsLeft() <= 2){
                                    play = c;
                                }
                                break;
                        }
                        break;
                }
            }
        }
        
        boolean fail = false;
        while(!current.legalAction(act, c)){
            if(target == -1 || fail){
                target = rand.nextInt(current.numPlayers());
            }
            
            try{
                if(guessCard == 0 || fail){
                    guessCard = rand.nextInt(7) + 2;
                }
                switch(play){
                    case GUARD:
                        act = Action.playGuard(myIndex, target, Card.values()[guessCard - 1]);
                        break;
                    case PRIEST:
                        act = Action.playPriest(myIndex, target);
                        break;
                    case BARON:  
                        act = Action.playBaron(myIndex, target);
                        break;
                    case HANDMAID:
                        act = Action.playHandmaid(myIndex);
                        break;
                    case PRINCE:  
                        act = Action.playPrince(myIndex, target);
                        break;
                    case KING:
                        act = Action.playKing(myIndex, target);
                        break;
                    case COUNTESS:
                        act = Action.playCountess(myIndex);
                        break;
                    default:
                        act = null;//never play princess
                }
            }catch(IllegalActionException e){
                fail = true;
            }
            fail = true;
        }
    
        return act;
    }
    
    private int numUnplayed(){
        return current.unseenCards().length;
    }
    
    private int turnsLeft(){
        int numLeft = current.unseenCards().length - 2;
        return (int)((double)numLeft/(double)(current.numPlayers() - 1));
    }
    
    // How likely a specific card is on any player(not this) hand
    private double unplayedProb(int cardValue){
        Card[] cards = current.unseenCards();
        int sumLeft = cards.length - 2;
        int numLeft = 0;
        
        if(ownCard == cardValue){
            numLeft--;
        }
        if(newCard == cardValue){
            numLeft--;
        }
        
        for(Card each : cards){
            if(each.value() == cardValue){
                numLeft++;
            }
        }
        
        return ((double)numLeft / (double)sumLeft);
    }
    
    // How likely a specific player(except this) encounter the card
    private double playerCardProb(int cardValue, int playerIndex){
        Iterator<Card> discardList = current.getDiscards(playerIndex);
        int numDiscard = 0;
        int numInit = 0;
        
        while(discardList.hasNext()){
            Card currentCard = discardList.next();
            if(currentCard.value() == cardValue){
                numDiscard++;
                numInit = currentCard.count();
            }
        }
        
        double prob = unplayedProb(cardValue) * Math.pow(((double)1 / (double)numInit), numDiscard);
        
        return prob;
    }
    
    private int playLast(int playerIndex){
        Iterator<Card> discardList = current.getDiscards(playerIndex);
        Card lastcard = null;
        while(discardList.hasNext()){
            lastcard = discardList.next();
        }
        
        if(lastcard == null){
            return -1;
        }
        return lastcard.value();
    }
}
