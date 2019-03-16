package fellowship;

import com.nmerrill.kothcomm.game.maps.Point2D;
import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Absorb;
import fellowship.abilities.attacking.Feast;
import fellowship.abilities.stats.Strong;
import fellowship.abilities.stats.Regenerate;
import fellowship.actions.ReadonlyAction;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;
import fellowship.Player;
import org.eclipse.collections.api.set.MutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Vampire extends Player{
    private final double CRITICAL_HEALTH = 5;
    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(30, 0, 0,
                    new Absorb(),
                    new Feast(),
                    new Regenerate(),
                    new Strong()));
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
        int minPriority = Integer.MAX_VALUE;
        ReadonlyAction chosen = null;
        for (ReadonlyAction action: actions){
            int priority = getPriorityFor(action, character);
            if (priority < minPriority){
                chosen = action;
                minPriority = priority;
            }
        }
        if (chosen == null){
            throw new RuntimeException("No valid actions");
        }
        if (chosen.needsLocation()){
            chosen.setLocation(chooseLocationFor(chosen, character));
        } else if (chosen.needsTarget()){
            chosen.setTarget(chooseTargetFor(chosen));
        }
        return chosen;
    }

    private Point2D chooseLocationFor(ReadonlyAction action, ReadonlyCharacter character){
        if (action.movementAction()){
            if (character.getHealth() <= CRITICAL_HEALTH){
                return fromEnemy(action.availableLocations());
            } else {
                return toEnemy(action.availableLocations());
            }
        }
        return toTeam(action.availableLocations());
    }

    private Point2D toEnemy(MutableSet<Point2D> availableLocations){
        if (visibleEnemies.isEmpty()){
            return availableLocations.iterator().next();
        }
        return availableLocations.minBy(p1 ->
                p1.cartesianDistance(visibleEnemies.keysView().minBy(p1::cartesianDistance))
        );
    }

    private Point2D fromEnemy(MutableSet<Point2D> availableLocations){
        if (visibleEnemies.isEmpty()){
            return availableLocations.iterator().next();
        }
        return availableLocations.maxBy(p1 ->
                p1.cartesianDistance(visibleEnemies.keysView().minBy(p1::cartesianDistance))
        );
    }

    private Point2D toTeam(MutableSet<Point2D> availableLocations){
        if (team.isEmpty()){
            return availableLocations.iterator().next();
        }
        return availableLocations.minBy(p1 ->
                p1.cartesianDistance(team.collect(ReadonlyCharacter::getLocation).minBy(p1::cartesianDistance))
        );
    }

    private ReadonlyCharacter chooseTargetFor(ReadonlyAction action){
        return action.availableTargets().minBy(ReadonlyCharacter::getHealth);
    }

    private int getPriorityFor(ReadonlyAction action, ReadonlyCharacter character){
        if (action.getName().equals("Smile")){
            return 1000;
        }
        if (action.movementAction()){
            if (character.getHealth() <= CRITICAL_HEALTH){
                return 0;
            }
            return 999;
        }
        if (action.needsTarget()) {
            return ((int) action.availableTargets().minBy(ReadonlyCharacter::getHealth).getHealth());
        }
        return 998;
    }
}
