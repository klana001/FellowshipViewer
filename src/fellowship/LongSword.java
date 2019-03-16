package fellowship;

import fellowship.*;
import com.nmerrill.kothcomm.game.maps.Point2D;
import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Flexible;
import fellowship.abilities.attacking.Ranged;
import fellowship.abilities.stats.Strong;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.attacking.Quick;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LongSword/*Closest NetHack Role: Valkyrie*/ extends Player{

    //debugging
    private boolean debug = false;
    private void println(String text) {
        if(debug)
            System.out.println(text);
    }

    //variables use to hold the start Y coordinate of the bot
    private boolean started = false;
    private int startY = 5;

    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(30, 0, 0,
                    new Ranged(), //Adds 1 to the range of Slice
                    new Flexible(), //Can Slice in any of the 8 directions
                    new ActionAbility(Quick::new), //Slice twice, Mana: 3, Cooldown: 0
                    new Strong())); //You gain 10 attribute points
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
        if(!started) {
            startY = character.getLocation().getY(); //giving startY the value of the bot's starting y-value
            started = true; //do this only once, that's why there is the if statement
        }

        ReadonlyAction current = null;

        //choosing action depending on priority
        int priority = Integer.MAX_VALUE;
        for(ReadonlyAction action:actions) {
            int priorityLocal = getPriority(action, character);
            if(priorityLocal < priority) {
                current = action;
                priority = priorityLocal;
            }
        }

        if (current == null){
            throw new RuntimeException("No valid actions");
        }

        println(current.getName());

        if(current.needsLocation()) {
            if(visibleEnemies.isEmpty()) {
                if (character.getHealth() < 100) {
                    //if has low health, go backwards towards "base"
                    //println("lowHealth");
                    current.setLocation(move(current, character, "backward"));
                } else {
                    //else go forwards to enemy's "base"
                    current.setLocation(move(current, character, "forward"));
                }
            }else{
                //go towards closest enemy
                current.setLocation(current.availableLocations().minBy(p1->p1.cartesianDistance(visibleEnemies.keysView().minBy(p1::cartesianDistance))));
            }
        }
        if(current.needsTarget()) {
            //get closest target
            current.setTarget(current.availableTargets().minBy(p1 -> 0));
        }

        return current;
    }

    //move backwards or forwards
    private Point2D move(ReadonlyAction readonlyAction, ReadonlyCharacter character, String direction) {
        Point2D location = null;

        //move direction depending on Y coordinate of point
        for(Point2D point2D:readonlyAction.availableLocations()) {
            switch (direction) {
                case "forward":
                    if(startY > 5) { //bot started at bottom
                        if (point2D.getY() < character.getLocation().getY())
                            location = point2D;
                    }else{ //bot started at top
                        if (point2D.getY() > character.getLocation().getY())
                            location = point2D;
                    }
                    break;
                case "backward":
                    if(startY > 5) { //bot started at bottom
                        if (point2D.getY() > character.getLocation().getY())
                            location = point2D;
                    }else{ //bot started at top
                        if (point2D.getY() < character.getLocation().getY())
                            location = point2D;
                    }
                    break;
            }

        }

        //if no available locations, just choose the first available location
        if(location == null) {
            location = readonlyAction.availableLocations().iterator().next();
        }

        println(location.getY()+","+character.getLocation().getY());

        return location;
    }

    private int getPriority(ReadonlyAction action, ReadonlyCharacter character) {
        if(visibleEnemies.isEmpty()) {
            //if there are no visible enemies, Step. In the choose function, this becomes move forward or backward depending on health
            if(action.getName().equals("Step")) {
                return 100;
            }
        }else {
            /*
             * PRIORITIES:
             *  1. Quick (Slice twice)
             *  2. Slice
             *  3. Step (when enemy is not in range --> move towards enemy)
             */
            if (action.getName().equals("Quick")) {
                return 1;
            }else if(action.getName().equals("Slice")) {
                return 10;
            }else if(action.getName().equals("Step")) {
                return 50;
            }
        }
        //Kids, don't Smile, instead Step or Slice
        return 1000;
    }
}
