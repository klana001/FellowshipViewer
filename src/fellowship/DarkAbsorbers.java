package fellowship;

import java.util.Arrays;
import java.util.List;

import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.set.ImmutableSet;

import com.nmerrill.kothcomm.game.maps.Point2D;

import fellowship.abilities.ActionAbility;
import fellowship.abilities.ReadonlyAbility;
import fellowship.abilities.attacking.Absorb;
import fellowship.abilities.attacking.Flexible;
import fellowship.abilities.attacking.Ranged;
import fellowship.abilities.vision.Darkness;
import fellowship.abilities.vision.TrueSight;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.attacking.Quick;
import fellowship.actions.damage.Zap;
import fellowship.actions.defensive.ForceField;
import fellowship.actions.other.Clone;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;

public class DarkAbsorbers extends SleafarPlayer {
    private ReadonlyCharacter zapTarget = null;

    private CharacterTemplate oracleAbsorberTemplate() {
        return new CharacterTemplate(20, 0, 0,
                new TrueSight(), new Flexible(), new Ranged(), new Absorb());
    }

    private CharacterTemplate quickAbsorberTemplate() {
        return new CharacterTemplate(20, 0, 0,
                new ActionAbility(Quick::new), new Flexible(), new Ranged(), new Absorb());
    }

    private CharacterTemplate darknessCloudTemplate() {
        return new CharacterTemplate(0, 0, 20,
                new ActionAbility(Clone::new), new ActionAbility(Zap::new), new Darkness());
    }

    @Override
    public List<CharacterTemplate> createCharacters() {
        return Arrays.asList(oracleAbsorberTemplate(), quickAbsorberTemplate(), darknessCloudTemplate());
    }

    private class Absorber extends Character {
        protected Absorber(ReadonlyCharacter delegate) {
            super(delegate);
        }

        @Override
        protected ReadonlyAction choose() {
            ReadonlyAction quick = getAction(Quick.class);

            if (quick != null && setSliceTarget(quick, 100.0)) {
                return quick;
            }
            if (slice != null && setSliceTarget(slice, 100.0)) {
                return slice;
            }

            ImmutableMap<Point2D, Double> damage = getEnemySliceDamage();
            ImmutableSet<Point2D> above5Damage = damage.select((k, v) -> v > 5.0).keysView().toSet().toImmutable();

            if (step != null && (above5Damage.contains(getLocation()) ||
                    (getHealth() <= 5.0 && isInEnemySliceRange())) && setAvoidEnemiesLocation(step)) {
                return step;
            }
            if (quick != null && setSliceTarget(quick, 0.01)) {
                return quick;
            }
            if (slice != null && setSliceTarget(slice, 0.01)) {
                return slice;
            }
            if (step != null && getSliceLocations().notEmpty() && setClosestLocation(step, getSliceLocations())) {
                return step;
            }
            if (step != null && setExploreLocation(step)) {
                return step;
            }
            return smile;
        }
    }

    private class DarknessCloud extends Character {
        private int zapCooldown = 0;
        private boolean zapNow = false;
        private boolean zapLater = false;

        protected DarknessCloud(ReadonlyCharacter delegate) {
            super(delegate);
        }

        private void updateZapFlags(double mana) {
            zapNow = zapCooldown == 0 && mana >= 15.0;
            zapLater = mana + 5 * getManaRegen() >= (zapNow ? 30.0 : 15.0);
        }

        private boolean isZappable(ReadonlyCharacter c, int zapNowCount, int zapLaterCount) {
            int forceFieldNow = 0;
            int forceFieldLater = 0;
            for (ReadonlyAbility a : c.getAbilities()) {
                if (a.abilityClass().equals(ForceField.class)) {
                    forceFieldNow = a.getRemaining();
                    forceFieldLater = 5;
                }
            }
            return c.getHealth() + c.getHealthRegen() <= (zapNowCount - forceFieldNow) * 30.0 ||
                    c.getHealth() + c.getHealthRegen() * 6 <= (zapNowCount + zapLaterCount - forceFieldNow - forceFieldLater) * 30.0;
        }

        @Override
        protected ReadonlyAction choose() {
            ReadonlyAction clone = getAction(Clone.class);
            ReadonlyAction zap = getAction(Zap.class);

            zapCooldown = zapCooldown > 0 ? zapCooldown - 1 : 0;
            updateZapFlags(getMana());
            int zapNowCount = characters.count(c -> c instanceof DarknessCloud && ((DarknessCloud) c).zapNow);
            int zapLaterCount = characters.count(c -> c instanceof DarknessCloud && ((DarknessCloud) c).zapLater);

            if (zap != null) {
                if (zapTarget != null && (!zap.availableTargets().contains(zapTarget) || zapTarget.isDead() ||
                        !isZappable(zapTarget, zapNowCount, zapLaterCount))) {
                    zapTarget = null;
                }
                if (zapTarget == null) {
                    zapTarget = chooseSmallest(zap.availableTargets().reject(c ->
                            isBear(c) || !isZappable(c, zapNowCount, zapLaterCount)), HEALTH_COMPARATOR);
                }
                if (zapTarget != null) {
                    zapCooldown = 5;
                    zapNow = false;
                    zap.setTarget(zapTarget);
                    return zap;
                }
            }

            ImmutableMap<Point2D, Double> damage = getEnemySliceDamage();
            ImmutableSet<Point2D> above5Damage = damage.select((k, v) -> v > 5.0).keysView().toSet().toImmutable();

            if (clone != null) {
                if (visibleEnemies.isEmpty()) {
                    if (setFarthestLocation(clone, getTeamHiddenLocations())) {
                        updateZapFlags(getMana() - 100.0);
                        return clone;
                    }
                } else {
                    if (setFarthestLocation(clone, above5Damage, getEnemyLocations()) ||
                            setLocation(clone, chooseSmallest(clone.availableLocations(),
                            (o1, o2) -> Double.compare(damage.get(o1), damage.get(o2))))) {
                        updateZapFlags(getMana() - 100.0);
                        return clone;
                    }
                }

                return clone;
            }
            if (step != null && (above5Damage.contains(getLocation()) ||
                    (getHealth() <= 5.0 && isInEnemySliceRange())) && setAvoidEnemiesLocation(step)) {
                return step;
            }
            if (slice != null && setSliceTarget(slice, 0.01)) {
                return slice;
            }
            if (step != null && !visibleEnemies.isEmpty() &&
                    setFarthestLocation(step, getEnemySliceLocations(), getEnemyLocations())) {
                return step;
            }
            return smile;
        }
    }

    @Override
    protected Character createCharacter(ReadonlyCharacter delegate) {
        if (hasAbility(delegate, Absorb.class)) {
            return new Absorber(delegate);
        } else if (hasAbility(delegate, Darkness.class)) {
            return new DarknessCloud(delegate);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
