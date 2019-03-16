package fellowship;

import java.util.Arrays;
import java.util.List;

import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;

import com.nmerrill.kothcomm.game.maps.Point2D;

import fellowship.abilities.ActionAbility;
import fellowship.abilities.damage.Static;
import fellowship.abilities.vision.Invisible;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.other.Clone;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;

public class StaticCloud extends SleafarPlayer {
    private CharacterTemplate invisibleTemplate() {
        return new CharacterTemplate(0, 0, 20,
                new ActionAbility(Clone::new), new Invisible(), new Static());
    }

    private CharacterTemplate visibleTemplate() {
        return new CharacterTemplate(0, 0, 20,
                new ActionAbility(Clone::new), new Static(), new Static());
    }

    @Override
    public List<CharacterTemplate> createCharacters() {
        return Arrays.asList(visibleTemplate(), invisibleTemplate(), visibleTemplate());
    }

    private class InvisibleCloud extends Character {
        protected InvisibleCloud(ReadonlyCharacter delegate) {
            super(delegate);
        }

        @Override
        protected ReadonlyAction choose() {
            ReadonlyAction clone = getAction(Clone.class);
            if (clone != null && (isVisible() || !isInEnemySightRange())) {
                int invisibleCount = countCharacters(InvisibleCloud.class);
                if (invisibleCount > 8 && setClosestSafeLocation(clone, getStaticLocations())) {
                    return clone;
                } else if (setCloneLocation(clone, invisibleCount < 3 ? 3 : 1)) {
                    return clone;
                }
            }
            if (step != null && isVisible() && isInEnemySliceRange() &&
                    setClosestSafeLocation(step, getStaticLocations())) {
                return step;
            }
            if (slice != null && isVisible() && setSliceTarget(slice, 0.01)) {
                return slice;
            }
            if (step != null) {
                ImmutableSet<Point2D> avoidLocations = !isVisible() || isInEnemySliceRange() ?
                        Sets.immutable.empty() : getEnemySliceLocations();
                if ((isVisible() || clone != null) && !getEnemyHiddenLocations().isEmpty() &&
                        setClosestLocation(step, avoidLocations, getEnemyHiddenLocations())) {
                    return step;
                }
                if (!getStaticLocations().contains(getLocation()) &&
                        setClosestLocation(step, avoidLocations, getStaticLocations())) {
                    return step;
                }
            }
            return smile;
        }
    }

    private class VisibleCloud extends Character {
        protected VisibleCloud(ReadonlyCharacter delegate) {
            super(delegate);
        }

        @Override
        protected ReadonlyAction choose() {
            ReadonlyAction clone = getAction(Clone.class);
            if (clone != null) {
                int visibleCount = countCharacters(VisibleCloud.class);
                if (visibleCount > 5 && setClosestSafeLocation(clone, getStaticLocations())) {
                    return clone;
                } else if (setCloneLocation(clone, visibleCount < 3 ? 2 : 1)) {
                    return clone;
                }
            }
            if (step != null && isInEnemySliceRange() && setClosestSafeLocation(step, getStaticLocations())) {
                return step;
            }
            if (slice != null && setSliceTarget(slice, 0.01)) {
                return slice;
            }
            if (step != null && !getStaticLocations().contains(getLocation())) {
                if (isInEnemySliceRange() ? setClosestLocation(step, getStaticLocations()) :
                        setClosestSafeLocation(step, getStaticLocations())) {
                    return step;
                }
            }
            return smile;
        }
    }

    @Override
    protected Character createCharacter(ReadonlyCharacter delegate) {
        if (hasAbility(delegate, Invisible.class)) {
            return new InvisibleCloud(delegate);
        } else {
            return new VisibleCloud(delegate);
        }
    }
}
