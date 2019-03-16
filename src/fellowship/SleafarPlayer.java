package fellowship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.list.primitive.IntInterval;
import org.eclipse.collections.impl.tuple.Tuples;

import com.nmerrill.kothcomm.game.maps.Point2D;

import fellowship.Player;
import fellowship.Range;
import fellowship.abilities.ReadonlyAbility;
import fellowship.abilities.attacking.Critical;
import fellowship.abilities.attacking.Reflexive;
import fellowship.abilities.defensive.Spikes;
import fellowship.abilities.statuses.Immune;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.attacking.Quick;
import fellowship.actions.attacking.Slice;
import fellowship.actions.mobility.Step;
import fellowship.actions.other.Smile;
import fellowship.characters.CharacterInterface;
import fellowship.characters.EnemyCharacter;
import fellowship.characters.ReadonlyCharacter;

public abstract class SleafarPlayer extends Player {
    private static final ImmutableSet<Point2D> MAP_LOCATIONS = IntInterval.fromTo(0, 9)
            .collect(x -> IntInterval.fromTo(0, 9).collect(y -> new Point2D(x, y))).flatCollect(t -> t).toSet()
            .toImmutable();
    protected static final Comparator<CharacterInterface> HEALTH_COMPARATOR = (o1, o2) ->
            Double.compare(o1.getHealth(), o2.getHealth());
    private static final Range BLOCKING_RANGE = new Range(1, true);
    private static final Range STATIC_RANGE = new Range(1);

    protected static boolean hasAbility(CharacterInterface character, Class<?> ability) {
        return character.getAbilities().anySatisfy(a -> a.abilityClass().equals(ability));
    }

    protected static boolean isBear(CharacterInterface character) {
        return character.getAbilities().isEmpty();
    }

    protected static double calcSliceDamage(CharacterInterface character) {
        return character.getStat(character.primaryStat()) * (hasAbility(character, Quick.class) ? 2.0 : 1.0);
    }

    protected static boolean setLocation(ReadonlyAction action, Point2D location) {
        if (location != null) {
            action.setLocation(location);
        }
        return location != null;
    }

    protected static boolean setTarget(ReadonlyAction action, ReadonlyCharacter target) {
        if (target != null) {
            action.setTarget(target);
        }
        return target != null;
    }

    protected abstract class Character {
        protected final ReadonlyCharacter delegate;

        protected Character(ReadonlyCharacter delegate) {
            super();
            this.delegate = delegate;
        }

        protected abstract ReadonlyAction choose();

        protected double getHealth() {
            return delegate.getHealth();
        }

        protected double getHealthRegen() {
            return delegate.getHealthRegen();
        }

        protected double getMana() {
            return delegate.getMana();
        }

        protected double getManaRegen() {
            return delegate.getManaRegen();
        }

        protected Point2D getLocation() {
            return delegate.getLocation();
        }

        protected boolean isVisible() {
            return !delegate.isInvisible();
        }

        protected double getSliceDamage() {
            return delegate.getStat(delegate.primaryStat());
        }

        protected boolean isInEnemySliceRange() {
            return getEnemySliceLocations().contains(delegate.getLocation());
        }

        protected boolean isInEnemySightRange() {
            return getEnemySightLocations().contains(delegate.getLocation());
        }

        protected boolean isInEnemyStepSightRange() {
            return getEnemyStepSightLocations().contains(delegate.getLocation());
        }

        protected double calcSliceRetaliationDamage(CharacterInterface character) {
            double result = 0.0;
            double ownDamage = getSliceDamage();
            for (ReadonlyAbility ability : character.getAbilities()) {
                if (ability.abilityClass().equals(Critical.class)) {
                    ownDamage = ownDamage * 2;
                }
            }
            for (ReadonlyAbility ability : character.getAbilities()) {
                if (ability.abilityClass().equals(Spikes.class)) {
                    result += ownDamage / 2.0;
                } else if (ability.abilityClass().equals(Reflexive.class)) {
                    result += character.getStat(character.primaryStat());
                }
            }
            return result;
        }

        protected double calcSpellRetaliationDamage(CharacterInterface character, double ownDamage) {
            double result = 0.0;
            for (ReadonlyAbility ability : character.getAbilities()) {
                if (ability.abilityClass().equals(Spikes.class)) {
                    result += ownDamage / 2.0;
                }
            }
            return result;
        }

        protected boolean setRandomLocation(ReadonlyAction action) {
            return setLocation(action, chooseRandom(action.availableLocations()));
        }

        protected boolean setRandomLocation(ReadonlyAction action, ImmutableSet<Point2D> avoidLocations) {
            return setLocation(action, chooseRandom(action.availableLocations().difference(avoidLocations)));
        }

        protected boolean setClosestLocation(ReadonlyAction action, ImmutableSet<Point2D> targetLocations) {
            return setLocation(action, chooseClosest(action.availableLocations(), targetLocations));
        }

        protected boolean setClosestLocation(ReadonlyAction action, ImmutableSet<Point2D> avoidLocations,
                ImmutableSet<Point2D> targetLocations) {
            return setLocation(action, chooseClosest(action.availableLocations().difference(avoidLocations),
                    targetLocations));
        }

        protected boolean setClosestHiddenLocation(ReadonlyAction action, ImmutableSet<Point2D> preferredLocations) {
            return setClosestLocation(action, getEnemySightLocations(), preferredLocations);
        }

        protected boolean setClosestSafeLocation(ReadonlyAction action, ImmutableSet<Point2D> preferredLocations) {
            return setClosestLocation(action, getEnemySliceLocations(), preferredLocations);
        }

        protected boolean setFarthestLocation(ReadonlyAction action, ImmutableSet<Point2D> targetLocations) {
            return setLocation(action, chooseFarthest(action.availableLocations(), targetLocations));
        }

        protected boolean setFarthestLocation(ReadonlyAction action, ImmutableSet<Point2D> avoidLocations,
                ImmutableSet<Point2D> targetLocations) {
            return setLocation(action, chooseFarthest(action.availableLocations().difference(avoidLocations),
                    targetLocations));
        }

        public boolean setCloneLocation(ReadonlyAction action, int distance) {
            ImmutableSet<Point2D> cloneLocations = distance < 2 ? team.collect(t -> t.getLocation()).toImmutable() :
                team.flatCollect(t -> t.rangeAround(new Range(distance))).difference(
                team.flatCollect(t -> t.rangeAround(new Range(distance - 1)))).toImmutable();
            if (cloneLocations.isEmpty()) {
                return setRandomLocation(action, getEnemySightLocations()) ||
                        setRandomLocation(action, getEnemySliceLocations()) ||
                        setRandomLocation(action);
            } else {
                return setClosestLocation(action, getEnemySightLocations(), cloneLocations) ||
                        setClosestLocation(action, getEnemySliceLocations(), cloneLocations) ||
                        setClosestLocation(action, cloneLocations);
            }
        }

        protected boolean setAvoidEnemiesLocation(ReadonlyAction action) {
            Point2D location = chooseFarthest(Sets.mutable.ofAll(action.availableLocations())
                    .with(delegate.getLocation()).difference(getEnemySliceLocations()), getEnemyLocations());
            if (location == null || location.equals(delegate.getLocation())) {
                return false;
            } else {
                return setLocation(action, location);
            }
        }

        protected boolean setBlockEnemiesLocation(ReadonlyAction action) {
            return setLocation(action, chooseRandom(action.availableLocations().intersect(getEnemyBlockingLocations())));
        }

        protected boolean setExploreLocation(ReadonlyAction action) {
            return visibleEnemies.size() < enemies.size() && getTeamHiddenLocations().notEmpty() &&
                    setClosestLocation(action, getEnemyStepSightLocations(), getTeamHiddenLocations());
        }

        protected boolean setSliceTarget(ReadonlyAction action, double minHealthReserve) {
            MutableSet<Pair<ReadonlyCharacter, Double>> pairs = action.availableTargets()
                    .collect(t -> Tuples.pair(t, calcSliceRetaliationDamage(t)));
            Pair<ReadonlyCharacter, Double> smallest = chooseSmallest(pairs, (o1, o2) -> {
                int c = Double.compare(o1.getTwo(), o2.getTwo());
                return c == 0 ? Double.compare(o1.getOne().getHealth(), o2.getOne().getHealth()) : c;
            });
            if (smallest == null || smallest.getTwo() > delegate.getHealth() - minHealthReserve) {
                return false;
            } else {
                return setTarget(action, smallest.getOne());
            }
        }

        protected boolean setPoisonTarget(ReadonlyAction action) {
            return setTarget(action, chooseSmallest(action.availableTargets().reject(c -> hasAbility(c, Immune.class)),
                    HEALTH_COMPARATOR));
        }

        protected final ImmutableSet<Point2D> getEnemyLocations() {
            if (enemyLocations == null) {
                enemyLocations = visibleEnemies.keysView().toSet().toImmutable();
            }
            return enemyLocations;
        }

        protected final ImmutableSet<Point2D> getEnemySliceLocations() {
            if (enemySliceLocations == null) {
                enemySliceLocations = visibleEnemies.keyValuesView()
                        .flatCollect(c -> c.getTwo().rangeAround(c.getTwo().getSliceRange(), c.getOne())).toSet()
                        .toImmutable();
            }
            return enemySliceLocations;
        }

        protected final ImmutableSet<Point2D> getEnemySightLocations() {
            if (enemySightLocations == null) {
                enemySightLocations = visibleEnemies.keyValuesView()
                        .flatCollect(c -> c.getTwo().rangeAround(c.getTwo().getSightRange(), c.getOne())).toSet()
                        .toImmutable();
            }
            return enemySightLocations;
        }

        protected final ImmutableSet<Point2D> getEnemyStepSightLocations() {
            if (enemyStepSightLocations == null) {
                enemyStepSightLocations = visibleEnemies.keyValuesView()
                        .flatCollect(c -> Sets.mutable.ofAll(c.getTwo().rangeAround(c.getTwo().getStepRange(), c.getOne()))
                                .with(c.getOne()).flatCollect(r -> c.getTwo().rangeAround(c.getTwo().getSightRange(), r)))
                        .toSet().toImmutable();
            }
            return enemyStepSightLocations;
        }

        protected final ImmutableSet<Point2D> getEnemyHiddenLocations() {
            if (enemyHiddenLocations == null) {
                enemyHiddenLocations = MAP_LOCATIONS.difference(getEnemySightLocations());
            }
            return enemyHiddenLocations;
        }

        protected final ImmutableSet<Point2D> getEnemyBlockingLocations() {
            if (enemyBlockingLocations == null) {
                enemyBlockingLocations = visibleEnemies.keyValuesView()
                        .flatCollect(c -> c.getTwo().rangeAround(BLOCKING_RANGE, c.getOne())).toSet().toImmutable();
            }
            return enemyBlockingLocations;
        }

        protected final ImmutableSet<Point2D> getTeamHiddenLocations() {
            if (teamHiddenLocations == null) {
                teamHiddenLocations = MAP_LOCATIONS.difference(team.flatCollect(c -> c.rangeAround(c.getSightRange())));
            }
            return teamHiddenLocations;
        }

        protected final ImmutableSet<Point2D> getTeamBlockingLocations() {
            if (teamBlockingLocations == null) {
                teamBlockingLocations = team.flatCollect(c -> c.rangeAround(BLOCKING_RANGE)).toImmutable();
            }
            return teamBlockingLocations;
        }

        protected final ImmutableSet<Point2D> getSliceLocations() {
            if (sliceLocations == null) {
                sliceLocations = visibleEnemies.keyValuesView()
                        .flatCollect(c -> c.getTwo().rangeAround(delegate.getSliceRange(), c.getOne())).toSet().toImmutable();
            }
            return sliceLocations;
        }

        protected final ImmutableSet<Point2D> getStaticLocations() {
            if (staticLocations == null) {
                staticLocations = visibleEnemies.keyValuesView()
                        .flatCollect(c -> c.getTwo().rangeAround(STATIC_RANGE, c.getOne())).toSet().toImmutable();
            }
            return staticLocations;
        }

        protected final ImmutableMap<Point2D, Double> getEnemySliceDamage() {
            if (enemySliceDamage == null) {
                MutableMap<Point2D, Double> tmp = MAP_LOCATIONS.toMap(l -> l, l -> 0.0);
                for (Pair<Point2D, EnemyCharacter> p : visibleEnemies.keyValuesView()) {
                    double damage = calcSliceDamage(p.getTwo());
                    for (Point2D l : p.getTwo().rangeAround(p.getTwo().getSliceRange(), p.getOne())) {
                        tmp.put(l, tmp.get(l) + damage);
                    }
                }
                enemySliceDamage = tmp.toImmutable();
            }
            return enemySliceDamage;
        }
    }

    protected ImmutableMap<ReadonlyCharacter, Character> characters = Maps.immutable.empty();

    private ImmutableMap<Class<?>, ReadonlyAction> actions = null;
    protected ReadonlyAction step = null;
    protected ReadonlyAction slice = null;
    protected ReadonlyAction smile = null;

    private ImmutableSet<Point2D> enemyLocations = null;
    private ImmutableSet<Point2D> enemySliceLocations = null;
    private ImmutableSet<Point2D> enemySightLocations = null;
    private ImmutableSet<Point2D> enemyStepSightLocations = null;
    private ImmutableSet<Point2D> enemyHiddenLocations = null;
    private ImmutableSet<Point2D> enemyBlockingLocations = null;
    private ImmutableSet<Point2D> teamHiddenLocations = null;
    private ImmutableSet<Point2D> teamBlockingLocations = null;
    private ImmutableSet<Point2D> sliceLocations = null;
    private ImmutableSet<Point2D> staticLocations = null;
    private ImmutableMap<Point2D, Double> enemySliceDamage = null;

    protected final <T> T chooseRandom(Collection<T> collection) {
        if (!collection.isEmpty()) {
            int i = getRandom().nextInt(collection.size());
            for (T t : collection) {
                if (i == 0) {
                    return t;
                }
                --i;
            }
        }
        return null;
    }

    protected final <T> T chooseSmallest(Collection<T> collection, Comparator<? super T> comparator) {
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

    protected final Point2D chooseClosest(Collection<Point2D> available, RichIterable<Point2D> targets) {
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

    protected final Point2D chooseFarthest(Collection<Point2D> available, RichIterable<Point2D> targets) {
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

    protected int countCharacters(Class<?> clazz) {
        return characters.count(c -> c.getClass().equals(clazz));
    }

    protected ReadonlyAction getAction(Class<?> clazz) {
        return actions.get(clazz);
    }

    protected abstract Character createCharacter(ReadonlyCharacter delegate);

    @Override
    public final ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
        characters = team.collect(c -> characters.getIfAbsentWith(c, this::createCharacter, c))
                .groupByUniqueKey(c -> c.delegate).toImmutable();

        this.actions = Sets.immutable.ofAll(actions).groupByUniqueKey(ReadonlyAction::actionClass);
        step = getAction(Step.class);
        slice = getAction(Slice.class);
        smile = getAction(Smile.class);

        enemyLocations = null;
        enemySliceLocations = null;
        enemySightLocations = null;
        enemyStepSightLocations = null;
        enemyHiddenLocations = null;
        enemyBlockingLocations = null;
        teamHiddenLocations = null;
        teamBlockingLocations = null;
        sliceLocations = null;
        staticLocations = null;
        enemySliceDamage = null;

        return characters.get(character).choose();
    }
}