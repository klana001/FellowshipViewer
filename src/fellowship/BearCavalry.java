package fellowship; 

import com.nmerrill.kothcomm.game.maps.Point2D;
import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Absorb;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.other.Clone;
import fellowship.actions.other.Bear;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;
import fellowship.Player;
import org.eclipse.collections.api.set.MutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BearCavalry extends Player{
    private final double CRITICAL_HEALTH = 20;
    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(9, 0, 11,
                        new Absorb(),
                        new ActionAbility(Clone::new),
                        new ActionAbility(Bear::new)));
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
    for(ReadonlyAction action: actions){
        if (action.getName().equals("Clone") && action.isAvailable()){
        action.setLocation(toTeam(action.availableLocations()));
        return action;
        }
    }
    for(ReadonlyAction action: actions){
        if (action.getName().equals("Bear") && action.isAvailable()){
        action.setLocation(toEnemy(action.availableLocations()));
        return action;
        }
    }
    for(ReadonlyAction action: actions){
        if (action.getName().equals("Slice") && action.isAvailable()){
        action.setTarget(action.availableTargets().minBy(ReadonlyCharacter::getHealth));
        return action;
        }
    }
    for(ReadonlyAction action: actions){
        if (action.getName().equals("Step") && action.isAvailable()){
        action.setLocation(toEnemy(action.availableLocations()));
        return action;
        }
    }
    for(ReadonlyAction action: actions){
        if (action.getName().equals("Smile")){
        return action;
        }
    }
    return null;
    }

    private Point2D toTeam(MutableSet<Point2D> availableLocations){
        if (team.isEmpty()){
            return availableLocations.iterator().next();
        }
        return availableLocations.minBy(p1 ->
                p1.cartesianDistance(team.collect(ReadonlyCharacter::getLocation).minBy(p1::cartesianDistance))
        );
    }

    private Point2D toEnemy(MutableSet<Point2D> availableLocations){
        if (visibleEnemies.isEmpty()){
            return availableLocations.iterator().next();
        }
        return availableLocations.minBy(p1 ->
                p1.cartesianDistance(visibleEnemies.keysView().minBy(p1::cartesianDistance))
        );
    }
}
