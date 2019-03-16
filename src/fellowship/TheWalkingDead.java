package fellowship;

import java.util.Arrays;
import java.util.List;

import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Absorb;
import fellowship.abilities.defensive.Resurrect;
import fellowship.abilities.stats.Strong;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.other.Clone;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;

public class TheWalkingDead extends SleafarPlayer {
    private CharacterTemplate zombie1Template() {
        return new CharacterTemplate(20, 0, 10, new ActionAbility(Clone::new), new Resurrect(), new Strong());
    }

    private CharacterTemplate zombie2Template() {
        return new CharacterTemplate(10, 0, 10, new ActionAbility(Clone::new), new Resurrect(), new Absorb());
    }

    @Override
    public List<CharacterTemplate> createCharacters() {
        return Arrays.asList(zombie1Template(), zombie2Template(), zombie2Template());
    }

    private class Zombie extends Character {
        private int resurrectCountdown = 0;
        private double oldHealth;

        protected Zombie(ReadonlyCharacter delegate) {
            super(delegate);
            this.oldHealth = getHealth();
        }

        @Override
        protected ReadonlyAction choose() {
            if (getHealth() > oldHealth + getHealthRegen() + 0.1) {
                resurrectCountdown = 40;
            }
            if (resurrectCountdown > 0) {
                --resurrectCountdown;
            }
            oldHealth = getHealth();

            ReadonlyAction clone = getAction(Clone.class);
            if (resurrectCountdown > 0) {
                if (step != null && isInEnemySliceRange() && setAvoidEnemiesLocation(step)) {
                    return step;
                }
                if (clone != null && !getSliceLocations().isEmpty() && setClosestLocation(clone, getSliceLocations())) {
                    return clone;
                }
                if (clone != null && setCloneLocation(clone, 1)) {
                    return clone;
                }
                if (slice != null && setSliceTarget(slice, 0.01)) {
                    return slice;
                }
                if (step != null && setAvoidEnemiesLocation(step)) {
                    return step;
                }
            } else {
                if (clone != null && !getSliceLocations().isEmpty() && setClosestLocation(clone, getSliceLocations())) {
                    return clone;
                }
                if (clone != null && setCloneLocation(clone, 1)) {
                    return clone;
                }
                if (slice != null && setSliceTarget(slice, 0.01)) {
                    return slice;
                }
                if (step != null && !getSliceLocations().isEmpty() && setClosestLocation(step, getSliceLocations())) {
                    return step;
                }
            }
            return smile;
        }
    }

    @Override
    protected Character createCharacter(ReadonlyCharacter delegate) {
        return new Zombie(delegate);
    }
}
