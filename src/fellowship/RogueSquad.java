package fellowship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.collections.impl.factory.Sets;

import com.nmerrill.kothcomm.game.maps.Point2D;

import fellowship.Player;
import fellowship.abilities.ActionAbility;
import fellowship.abilities.defensive.Spikes;
import fellowship.abilities.stats.Focused;
import fellowship.abilities.vision.FarSight;
import fellowship.abilities.vision.Invisible;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.other.Clone;
import fellowship.actions.statuses.Poison;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.EnemyCharacter;
import fellowship.characters.ReadonlyCharacter;

public class RogueSquad extends Player {
    private static final String INVISIBLE_NAME = new Invisible().getName();
    private static final String SPIKES_NAME = new Spikes().getName();

    private Set<Point2D> enemySliceLocations = new HashSet<>();
    private Set<Point2D> enemySightLocations = new HashSet<>();

    private CharacterTemplate scoutTemplate() {
        return new CharacterTemplate(0, 0, 20,
                new ActionAbility(Clone::new),
                new Invisible(),
                new FarSight());
    }

    private CharacterTemplate assasinTemplate() {
        return new CharacterTemplate(0, 0, 20,
                new ActionAbility(Clone::new),
                new ActionAbility(Poison::new),
                new Focused());
    }

    @Override
    public List<CharacterTemplate> createCharacters() {
        return Arrays.asList(assasinTemplate(), scoutTemplate(), assasinTemplate());
    }

    private <T> T chooseRandom(Collection<T> collection) {
        if (!collection.isEmpty()) {
            int i = getRandom().nextInt(collection.size());
            for (T t : collection) {
                if (i == 0) {
                    return t;
                }
                -- i;
            }
        }
        return null;
    }

    private boolean setLocation(ReadonlyAction action, Point2D location) {
        if (location != null) {
            action.setLocation(location);
        }
        return location != null;
    }

    private boolean setSafeLocation(ReadonlyAction action) {
        return setLocation(action, chooseRandom(Sets.difference(action.availableLocations(), enemySliceLocations)));
    }

    private boolean setRandomLocation(ReadonlyAction action) {
        return setLocation(action, chooseRandom(action.availableLocations()));
    }

    private boolean setTarget(ReadonlyAction action, ReadonlyCharacter target) {
        if (target != null) {
            action.setTarget(target);
        }
        return target != null;
    }

    private boolean setSafeTarget(ReadonlyAction action) {
        return setTarget(action, chooseRandom(action.availableTargets()
                .reject(t -> t.getAbilities().anySatisfy(a -> a.name().equals(SPIKES_NAME)))));
    }

    private boolean setWeakestTarget(ReadonlyAction action) {
        double health = Double.MAX_VALUE;
        List<ReadonlyCharacter> list = new ArrayList<>();
        for (ReadonlyCharacter t : action.availableTargets()) {
            if (t.getHealth() < health) {
                list.clear();
            }
            if (t.getHealth() <= health) {
                list.add(t);
            }
        }
        return setTarget(action, chooseRandom(list));
    }

    private ReadonlyAction scoutAction(ReadonlyAction step, ReadonlyAction slice, ReadonlyAction smile,
            ReadonlyAction clone, ReadonlyCharacter character) {
        if (clone != null && (!character.isInvisible() || !enemySightLocations.contains(character.getLocation())) &&
                (setSafeLocation(clone) || setRandomLocation(clone))) {
            return clone;
        }
        if (step != null && !character.isInvisible() && enemySliceLocations.contains(character.getLocation()) &&
                setSafeLocation(step)) {
            return step;
        }
        if (slice != null && !character.isInvisible() && setSafeTarget(slice)) {
            return slice;
        }
        if (step != null && (setSafeLocation(step) || setRandomLocation(step))) {
            return step;
        }
        return smile;
    }

    private ReadonlyAction assasinAction(ReadonlyAction step, ReadonlyAction slice, ReadonlyAction smile,
            ReadonlyAction clone, ReadonlyAction poison, ReadonlyCharacter character) {
        if (clone != null && (setSafeLocation(clone) || setRandomLocation(clone))) {
            return clone;
        }
        if (step != null && enemySliceLocations.contains(character.getLocation()) && setSafeLocation(step)) {
            return step;
        }
        if (slice != null && setSafeTarget(slice)) {
            return slice;
        }
        if (poison != null && setWeakestTarget(poison)) {
            return poison;
        }
        if (step != null && (setSafeLocation(step) || setRandomLocation(step))) {
            return step;
        }
        return smile;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
        Map<String, ReadonlyAction> actionMap = new HashMap<>();
        for (ReadonlyAction action : actions) {
            actionMap.put(action.getName(), action);
        }
        enemySliceLocations.clear();
        enemySightLocations.clear();
        for (Entry<Point2D, EnemyCharacter> entry : visibleEnemies.entrySet()) {
            enemySliceLocations.addAll(entry.getValue().rangeAround(entry.getValue().getSliceRange(), entry.getKey()));
            enemySightLocations.addAll(entry.getValue().rangeAround(entry.getValue().getSightRange(), entry.getKey()));
        }
        if (character.getAbilities().anySatisfy(t -> t.name().equals(INVISIBLE_NAME))) {
            return scoutAction(actionMap.get("Step"), actionMap.get("Slice"), actionMap.get("Smile"),
                    actionMap.get("Clone"), character);
        } else {
            return assasinAction(actionMap.get("Step"), actionMap.get("Slice"), actionMap.get("Smile"),
                    actionMap.get("Clone"), actionMap.get("Poison"), character);
        }
    }
}
