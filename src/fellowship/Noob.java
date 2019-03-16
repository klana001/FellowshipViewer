package fellowship;

import fellowship.*;
import com.nmerrill.kothcomm.game.maps.Point2D;
import fellowship.Stat;
import fellowship.abilities.ActionAbility;
import fellowship.abilities.stats.Regenerate;
import fellowship.abilities.stats.Strong;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.defensive.Shield;
import fellowship.actions.statuses.Silence;
import fellowship.actions.statuses.Stun;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;
import fellowship.Player;
import org.eclipse.collections.api.set.MutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Noob/*Destroyer*/ extends Player {

    private boolean debug = false;
    private void println(String text) {
        if(debug)
            System.out.println(text);
    }

    private boolean started = false;
    private int startY = 5;

    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(40, 0, 0,
                    new Regenerate(),
                    new ActionAbility(Stun::new),
                    new Strong(),
                    new Strong()));
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
        if(!started) {
            startY = character.getLocation().getY();
            started = true;
        }

        ReadonlyAction readonlyAction = null;

        //get priority of action
        int priority = Integer.MAX_VALUE;

        for(ReadonlyAction action:actions) {
            int priorityLocal = getPriority(action, character);
            if(priorityLocal < priority) {
                readonlyAction = action;
                priority = priorityLocal;
            }
        }

        if (readonlyAction == null){
            println("NULL!");
            throw new RuntimeException("No valid actions");
        }

        //movement
        if(readonlyAction.needsLocation()) {
            if(visibleEnemies.isEmpty()) {
                if (character.getHealth() < 100) {
                    readonlyAction.setLocation(move(readonlyAction, character, "backward"));
                } else {
                    readonlyAction.setLocation(move(readonlyAction, character, "forward")); //enemy base is "forward"
                }
            }else{
                readonlyAction.setLocation(readonlyAction.availableLocations().minBy(p1->p1.cartesianDistance(visibleEnemies.keysView().minBy(p1::cartesianDistance))));
            }
        }

        if(readonlyAction.needsTarget()) {
            readonlyAction.setTarget(readonlyAction.availableTargets().minBy(p1 -> 0));
        }

        return readonlyAction;
    }

    private Point2D move(ReadonlyAction readonlyAction, ReadonlyCharacter character, String direction) {
        Point2D location = null;

        for(Point2D point2D:readonlyAction.availableLocations()) {
            switch (direction) {
                case "forward":
                    if(startY > 5) { //bot starts at bottom
                        if (point2D.getY() < character.getLocation().getY())
                            location = point2D;
                    }else{ //bot starts at top
                        if (point2D.getY() > character.getLocation().getY())
                            location = point2D;
                    }
                    break;
                case "backward":
                    if(startY > 5) { //bot starts at bottom
                        if (point2D.getY() > character.getLocation().getY())
                            location = point2D;
                    }else{ //bot starts at top
                        if (point2D.getY() < character.getLocation().getY())
                            location = point2D;
                    }
                    break;
            }

        }

        if(location == null) {
            location = readonlyAction.availableLocations().iterator().next();
        }
        return location;
    }

    private int getPriority(ReadonlyAction action, ReadonlyCharacter character) {
        if(visibleEnemies.isEmpty()) {
            if(action.getName().equals("Step")) {
                return 100;
            }
        }else {
            if (action.getName().equals("Slice")) {
                return 10;
            }else if(action.getName().equals("Step")) {
                return 50;
            }else if(action.getName().equals("Stun") && !action.availableTargets().minBy(p1->0).isStunned()) {
                //if target is not stunned, stun 'em
                return 1;
            }
        }
        return 1000;
    }
}