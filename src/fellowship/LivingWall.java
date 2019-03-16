package fellowship;

import com.nmerrill.kothcomm.game.maps.Point2D;
import fellowship.abilities.*;
import fellowship.abilities.attacking.*;
import fellowship.abilities.defensive.*;
import fellowship.abilities.vision.*;
import fellowship.abilities.stats.*;
import fellowship.abilities.statuses.*;
import fellowship.actions.*;
import fellowship.actions.attacking.*;
import fellowship.actions.damage.*;
import fellowship.actions.defensive.*;
import fellowship.actions.statuses.*;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;
import fellowship.characters.EnemyCharacter;
import fellowship.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LivingWall extends Player {
  @Override
  public List<CharacterTemplate> createCharacters() {
    List<CharacterTemplate> templates = new ArrayList<>();

    for (int i = 0; i < 2; i++)
      templates.add(new CharacterTemplate(30, 0, 0,
                                          new Absorb(),
                                          new Strong(),
                                          new Buff(),
                                          new Buff()));
    templates.add(new CharacterTemplate(20, 0, 0,
                                        new Absorb(),
                                        new TrueSight(),
                                        new Buff(),
                                        new Buff()));

    return templates;
  }

  private String lastIdentifier(String s) {
    String[] split = s.split("\\W");
    return split[split.length - 1];
  }

  private boolean hasAbility(ReadonlyCharacter character, String abilityName) {
    for (ReadonlyAbility ability : character.getAbilities()) {
      if (lastIdentifier(ability.name()).equals(abilityName))
        return true;
    }
    return false;
  }

  private boolean hasAbility(EnemyCharacter character, String abilityName) {
    for (ReadonlyAbility ability : character.getAbilities()) {
      if (lastIdentifier(ability.name()).equals(abilityName))
        return true;
    }
    return false;
  }

  private int goalX = 5;
  private int goalY = 5;

  @Override
  public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {

    /* If we're at the goal square, pick a new one. */
    if (goalX == character.getLocation().getX() &&
        goalY == character.getLocation().getY()) {
      int i = getRandom().nextInt(5);
      goalX = i < 2 ? 1 : i > 2 ? 9 : 5;
      goalY = i == 2 ? 5 : (i % 2) == 1 ? 1 : 9;
    }

    {
      int bestDistance = 99999;
      /* If there are visible enemies, place the goal square under the closest enemy to
         the team. */
      for (Point2D enemyLocation : visibleEnemies.keysView()) {
        int distance = 0;
        for (ReadonlyCharacter ally : team) {
          Point2D allyLocation = ally.getLocation();
          distance +=
            (allyLocation.getX() - enemyLocation.getX()) *
            (allyLocation.getX() - enemyLocation.getX()) +
            (allyLocation.getY() - enemyLocation.getY()) *
            (allyLocation.getY() - enemyLocation.getY());
        }
        if (distance < bestDistance) {
          goalX = enemyLocation.getX();
          goalY = enemyLocation.getY();
          bestDistance = distance;
        }
      }
    }

    /* We use a priority rule for actions. */
    int bestPriority = -2;
    ReadonlyAction bestAction = null;
    for (ReadonlyAction action : actions) {
      int priority = 0;
      if (lastIdentifier(action.getName()).equals("Slice")) {
        int damagePotential = 35;
        /* We use these abilities with very high priority to /kill/ an enemy
           who's weak enough to die from the damage. If they wouldn't die,
           we still want to attack them, but we might prefer to attack
           other enemies instead. The enemy on the goal square (if any)
           is a slightly preferred target, to encourage the team to focus
           on a single enemy. */
        ReadonlyCharacter chosenTarget = null;
        for (ReadonlyCharacter target : action.availableTargets()) {
          if (!isEnemy(target))
            continue;
          chosenTarget = target;
          if (target.getHealth() <= damagePotential) {
            priority = 18;
          } else
            priority = 14;
          if (target.getLocation().getX() == goalX &&
              target.getLocation().getY() == goalY)
            priority++;
        }
        if (chosenTarget == null)
          continue;
        action.setTarget(chosenTarget);
      } else if (lastIdentifier(action.getName()).equals("Smile")) {
        priority = 0;
      } else if (action.movementAction()) {
        /* Move towards the goal location. */
        int bestDistance = 99999;
        Point2D bestLocation = null;
        priority = 1;
        for (Point2D location :
               action.availableLocations().toList().shuffleThis(getRandom())) {
          int distance =
            (location.getX() - goalX) * (location.getX() - goalX) +
            (location.getY() - goalY) * (location.getY() - goalY);
          if (distance < bestDistance) {
            bestDistance = distance;
            bestLocation = location;
          }
        }
        if (bestLocation == null)
          continue;
        action.setLocation(bestLocation);
      } else
        throw new RuntimeException("unknown action" + action.getName());

      if (priority > bestPriority) {
        bestPriority = priority;
        bestAction = action;
      }
    }
    if (bestAction == null)
      throw new RuntimeException("no action?");

    return bestAction;
  }
}
