package fellowship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.list.primitive.IntInterval;

import com.nmerrill.kothcomm.game.maps.Point2D;

import fellowship.Player;
import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Critical;
import fellowship.abilities.attacking.Reflexive;
import fellowship.abilities.defensive.Spikes;
import fellowship.abilities.vision.FarSight;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.attacking.Slice;
import fellowship.actions.attacking.Weave;
import fellowship.actions.mobility.Step;
import fellowship.actions.other.Smile;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;

public class SniperSquad extends Player {
    private static final ImmutableSet<Point2D> MAP_LOCATIONS = IntInterval.fromTo(0, 9)
            .collect(x -> IntInterval.fromTo(0, 9).collect(y -> new Point2D(x, y))).flatCollect(t -> t)
            .toSet().toImmutable();

    private ImmutableSet<Point2D> enemyLocations = null;
    private ImmutableSet<Point2D> enemySliceLocations = null;
    private ImmutableSet<Point2D> enemySightLocations = null;
    private ImmutableSet<Point2D> ownHiddenLocations = null;

    private CharacterTemplate spotterTemplate() {
        return new CharacterTemplate(20, 0, 0,
                new FarSight(),
                new FarSight(),
                new FarSight(),
                new FarSight());
    }

    private CharacterTemplate shooterTemplate() {
        return new CharacterTemplate(20, 0, 0,
                new ActionAbility(Weave::new),
                new Critical(),
                new Critical(),
                new Critical());
    }

    @Override
    public List<CharacterTemplate> createCharacters() {
        return Arrays.asList(shooterTemplate(), spotterTemplate(), shooterTemplate());
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

    private <T> T chooseSmallest(Collection<T> collection, Comparator<T> comparator) {
        if (!collection.isEmpty()) {
            List<T> list = new ArrayList<>();
            for (T t : collection) {
                if (list.isEmpty()) {
                    list.add(t);
                } else {
                    int c = comparator.compare(t, list.get(0));
                    if (c < 0) {
                        list.clear();
                    }
                    if (c <= 0) {
                        list.add(t);
                    }
                }
            }
            return list.get(getRandom().nextInt(list.size()));
        }
        return null;
    }

    private Point2D chooseClosest(Collection<Point2D> available, RichIterable<Point2D> targets) {
        if (targets.isEmpty()) {
            return chooseRandom(available);
        } else {
            Map<Point2D, Integer> map = new HashMap<>();
            for (Point2D a : available) {
                map.put(a, targets.collect(t -> t.cartesianDistance(a)).min());
            }
            return chooseSmallest(available, (o1, o2) -> Integer.compare(map.get(o1), map.get(o2)));
        }

    }

    private Point2D chooseFarthest(Collection<Point2D> available, RichIterable<Point2D> targets) {
        if (targets.isEmpty()) {
            return chooseRandom(available);
        } else {
            Map<Point2D, Integer> map = new HashMap<>();
            for (Point2D a : available) {
                map.put(a, targets.collect(t -> t.cartesianDistance(a)).min());
            }
            return chooseSmallest(available, (o1, o2) -> Integer.compare(map.get(o2), map.get(o1)));
        }

    }

    private boolean setLocation(ReadonlyAction action, Point2D location) {
        if (location != null) {
            action.setLocation(location);
        }
        return location != null;
    }

    private boolean setRetreatLocation(ReadonlyAction action, ReadonlyCharacter character) {
        return enemySliceLocations.contains(character.getLocation()) && setLocation(action,
                chooseFarthest(action.availableLocations().difference(enemySliceLocations), enemyLocations));
    }

    private boolean setSpotterAdvanceLocation(ReadonlyAction action, ReadonlyCharacter character) {
        if (enemySightLocations.contains(character.getLocation())) {
            return setLocation(action, chooseFarthest(action.availableLocations().difference(enemySliceLocations), enemyLocations));
        } else if (ownHiddenLocations.notEmpty()) {
            return setLocation(action, chooseClosest(action.availableLocations().difference(enemySliceLocations), ownHiddenLocations));
        } else {
            return false;
        }
    }

    private boolean setShooterAdvanceLocation(ReadonlyAction action, ReadonlyCharacter character) {
        return enemySightLocations.contains(character.getLocation()) && setLocation(action,
                chooseFarthest(action.availableLocations().difference(enemySliceLocations), enemyLocations));
    }

    private boolean isTargetSafe(ReadonlyCharacter character) {
        return character.getAbilities().noneSatisfy(a ->
                a.abilityClass().equals(Spikes.class) ||
                a.abilityClass().equals(Reflexive.class));
    }

    private boolean setTarget(ReadonlyAction action, ReadonlyCharacter target) {
        if (target != null) {
            action.setTarget(target);
        }
        return target != null;
    }

    private boolean setSafeTarget(ReadonlyAction action) {
        return setTarget(action, chooseRandom(action.availableTargets().select(this::isTargetSafe)));
    }

    private ReadonlyAction spotterAction(ReadonlyAction step, ReadonlyAction slice, ReadonlyAction smile,
            ReadonlyCharacter character) {
        if (slice != null && setSafeTarget(slice)) {
            return slice;
        }
        if (step != null && setRetreatLocation(step, character)) {
            return step;
        }
        if (step != null && setSpotterAdvanceLocation(step, character)) {
            return step;
        }
        return smile;
    }

    private ReadonlyAction shooterAction(ReadonlyAction step, ReadonlyAction slice, ReadonlyAction smile,
            ReadonlyAction weave, ReadonlyCharacter character) {
        if (weave != null && weave.availableTargets().allSatisfy(this::isTargetSafe)) {
            return weave;
        }
        if (slice != null && setSafeTarget(slice)) {
            return slice;
        }
        if (step != null && setRetreatLocation(step, character)) {
            return step;
        }
        if (step != null && setShooterAdvanceLocation(step, character)) {
            return step;
        }
        return smile;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
        ImmutableMap<Class<?>, ReadonlyAction> actionMap = Sets.immutable.ofAll(actions)
                .groupByUniqueKey(ReadonlyAction::actionClass);
        enemyLocations = visibleEnemies.keysView().toSet().toImmutable();
        enemySliceLocations = visibleEnemies.keyValuesView().flatCollect(
                p -> p.getTwo().rangeAround(p.getTwo().getSliceRange(), p.getOne())).toSet().toImmutable();
        enemySightLocations = visibleEnemies.keyValuesView().flatCollect(
                p -> p.getTwo().rangeAround(p.getTwo().getSightRange(), p.getOne())).toSet().toImmutable();
        ownHiddenLocations = MAP_LOCATIONS.difference(team.flatCollect(c -> c.rangeAround(c.getSightRange())));

        if (character.getAbilities().anySatisfy(t -> t.abilityClass().equals(FarSight.class))) {
            return spotterAction(actionMap.get(Step.class), actionMap.get(Slice.class), actionMap.get(Smile.class),
                    character);
        } else {
            return shooterAction(actionMap.get(Step.class), actionMap.get(Slice.class), actionMap.get(Smile.class),
                    actionMap.get(Weave.class), character);
        }
    }
}
