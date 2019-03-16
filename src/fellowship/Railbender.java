package fellowship;

import com.nmerrill.kothcomm.game.maps.Point2D;
import fellowship.Player;
import fellowship.abilities.ActionAbility;
import fellowship.abilities.ReadonlyAbility;
import fellowship.abilities.attacking.Critical;
import fellowship.abilities.stats.Buff;
import fellowship.abilities.stats.Clever;
import fellowship.abilities.stats.Strong;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.attacking.Quick;
import fellowship.actions.damage.Zap;
import fellowship.actions.defensive.Restore;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class Railbender extends Player {
    private static final double CRITICAL_HEALTH_PCT = .175;

    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> list = new ArrayList<>();

        list.add(new CharacterTemplate(14, 3, 3,
                                       new Clever(),
                                       new Clever(),
                                       new ActionAbility(Restore::new),
                                       new ActionAbility(Zap::new)));

        for (int k = 0; k < 2; k++) {
            list.add(new CharacterTemplate(25, 2, 3,
                                           new Critical(),
                                           new Buff(),
                                           new ActionAbility(Quick::new),
                                           new Strong()));
        }
        return list;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
        List<ReadonlyAbility> abilities = character.getAbilities();
        ReadonlyAction action = null;

        for (ReadonlyAbility a : abilities) {
            String s = a.name();
            int i = s.lastIndexOf(".");
            if (i == -1)
                continue;
            s = s.substring(i+1, s.length());
            if (s.equals("Clever")) {
                action = getActionForChar1(character, actions);
                break;
            }
            else if (s.equals("Buff")) {
                action = getActionForChar2(character, actions);
                break;
            }
        }

        return action;
    }

    private ReadonlyAction getActionForChar1(ReadonlyCharacter character, Set<ReadonlyAction> actions) {
        int members = (int) team.stream().filter(c -> !c.isDead()).count();

        List<ReadonlyAction> list = actions.stream()
                                           .sorted(Comparator.comparingInt(this::getPriority))
                                           .collect(Collectors.toList());

        Point2D closestEnemy = getClosestEnemyPoint(character);

        for (ReadonlyAction a : list) {
            String name = a.getName();
            if (name.equals("Restore")) {
                for (ReadonlyCharacter teammate : team) {
                    if (teammate.getHealth() / teammate.getMaxHealth() < CRITICAL_HEALTH_PCT * (4 - members))
                        return a;
                }
            }
            else if (name.equals("Zap") && !a.availableTargets().isEmpty() && closestEnemy != null &&
                     character.getLocation().cartesianDistance(closestEnemy) <= 4) {
                a.setTarget(a.availableTargets()
                             .stream()
                             .reduce(
                                 BinaryOperator.minBy(
                                     Comparator.<ReadonlyCharacter>comparingDouble(e -> e.getHealth())
                                 )
                             )
                             .get()
                );
                return a;
            }
            else if (name.equals("Slice") && !a.availableTargets().isEmpty()) {
                a.setTarget(a.availableTargets().iterator().next());
                return a;
            }
            else if (name.equals("Smile"))
                return a;
        }
        throw new RuntimeException("No available actions");
    }

    private ReadonlyAction getActionForChar2(ReadonlyCharacter character, Set<ReadonlyAction> actions) {
        List<ReadonlyAction> list = actions.stream()
                                           .sorted(Comparator.comparingInt(this::getPriority))
                                           .collect(Collectors.toList());

        for (ReadonlyAction a : list) {
            String name = a.getName();
            if (name.equals("Quick") && !a.availableTargets().isEmpty()) {
                a.setTarget(a.availableTargets().minBy(ReadonlyCharacter::getHealth));
                return a;
            }
            else if (name.equals("Slice") && !a.availableTargets().isEmpty()) {
                a.setTarget(a.availableTargets().minBy(ReadonlyCharacter::getHealth));
                return a;
            }
            else if (name.equals("Step") && !a.availableLocations().isEmpty()) {
                Point2D e = getClosestEnemyPoint(character);
                if (e == null) {
                    Point2D p = character.getLocation();
                    if (p.getY() > 5) {
                        a.setLocation(a.availableLocations()
                                       .stream()
                                       .filter(x -> x.getY() < p.getY())
                                       .findFirst()
                                       .orElse(a.availableLocations().iterator().next()));
                    }
                    else if (p.getY() < 4) {
                        a.setLocation(a.availableLocations()
                                       .stream()
                                       .filter(x -> x.getY() > p.getY())
                                       .findFirst()
                                       .orElse(a.availableLocations().iterator().next()));
                    }
                    else
                        a.setLocation(randomLocation(new ArrayList<>(a.availableLocations())));
                }
                else {
                    int currentDistance = character.getLocation().cartesianDistance(e);
                    a.setLocation(a.availableLocations()
                                   .stream()
                                   .filter(x -> x.cartesianDistance(e) < currentDistance)
                                   .findFirst()
                                   .orElse(randomLocation(new ArrayList<>(a.availableLocations()))));
                }
                return a;
            }
            else if (name.equals("Smile"))
                return a;
        }
        throw new RuntimeException("No available actions");
    }

    private Point2D getClosestEnemyPoint(ReadonlyCharacter c) {
        return visibleEnemies.keySet()
                             .stream()
                             .reduce(
                                 BinaryOperator.minBy(
                                     Comparator.comparingInt(x -> x.cartesianDistance(c.getLocation()))
                                 )
                             )
                             .orElse(null);
    }

    private int getPriority(ReadonlyAction action) {
        switch (action.getName()) {
            case "Quick":
            case "Restore":
                return 1;
            case "Zap": return 2;
            case "Slice": return 3;
            case "Step": return 4;
            case "Smile": return 5;
        }
        throw new IllegalArgumentException(String.valueOf(action));
    }

    private Point2D randomLocation(List<Point2D> l) {
        return l.get((int) (Math.random() * l.size()));
    }
}
