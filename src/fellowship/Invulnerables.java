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

public class Invulnerables extends Player {
  @Override
  public List<CharacterTemplate> createCharacters() {
    List<CharacterTemplate> templates = new ArrayList<>();

    templates.add(new CharacterTemplate(0, 0, 20,
                                        new ActionAbility(Poison::new),
                                        new ActionAbility(ForceField::new),
                                        new Focused(),
                                        new Pillar()));

    templates.add(new CharacterTemplate(30, 0, 0,
                                        new ActionAbility(Weave::new),
                                        new Strong(),
                                        new FarSight(),
                                        new TrueSight()));

    templates.add(new CharacterTemplate(20, 0, 0,
                                        new ActionAbility(Weave::new),
                                        new ActionAbility(Knockout::new),
                                        new ManaSteal(),
                                        new Immune()));

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

  private int getSquareDanger(ReadonlyCharacter character, Point2D square) {
    /* A square's danger is basically equal to the number of hits we'd
       expect to take when standing there. Each hit is worth 1; a hit of
       25 damage or more is worth 2. */
    int sliceDanger = 0;
    int otherDanger = 0;
    int cx = square.getX();
    int cy = square.getY();
    for (Point2D enemyLocation : visibleEnemies.keysView()) {
      EnemyCharacter enemy = visibleEnemies.get(enemyLocation);
      if (enemy.isStunned())
        continue; /* approaching stunned enemies is a good thing */
      int dx = enemyLocation.getX() - cx;
      int dy = enemyLocation.getY() - cy;
      if (dx < 0)
        dx = -dx;
      if (dy < 0)
        dy = -dy;
      if (dx + dy <= 1) {
        /* We're in Static range. */
        if (hasAbility(enemy, "Static"))
          otherDanger++;
      }
      if (dx + dy <= enemy.getSliceRange().getRange() &&
          (dx * dy == 0 || !enemy.getSliceRange().isCardinal())) {
        int sliceMultiplier = 1;
        if (hasAbility(enemy, "Quick") && !hasAbility(character, "Pillar"))
          sliceMultiplier *= 2;
        if (enemy.getStat(enemy.primaryStat()) >= 25)
          sliceMultiplier *= 2;
        if (hasAbility(character, "Pillar")) {
          if (sliceDanger >= sliceMultiplier)
            continue;
          sliceDanger = 0;
        }
        sliceDanger += sliceMultiplier;
      }
    }
    return sliceDanger + otherDanger;
  }

  private ReadonlyAction[] forceFieldAction = new ReadonlyAction[3];
  private int goalX = 5;
  private int goalY = 5;

  @Override
  public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {

    /* Which character are we? */
    int characterNumber;
    if (hasAbility(character, "Focused"))
      characterNumber = 0;
    else if (hasAbility(character, "Immune"))
      characterNumber = 1;
    else if (hasAbility(character, "TrueSight"))
      characterNumber = 2;
    else
      throw new RuntimeException("Unrecognised character!");

    /* If we're at the goal square, pick a new one. */
    if (goalX == character.getLocation().getX() &&
        goalY == character.getLocation().getY()) {
      int i = getRandom().nextInt(5);
      goalX = i < 2 ? 1 : i > 2 ? 9 : 5;
      goalY = i == 2 ? 5 : (i % 2) == 1 ? 1 : 9;
    }

    /* If there are a lot of visible enemies, try to group up in a corner in order
       to prevent being surrounded. */
    if (visibleEnemies.size() > 3) {
      int xVotes = 0;
      int yVotes = 0;
      for (ReadonlyCharacter ally : team) {
        xVotes += ally.getLocation().getX() >= 5 ? 1 : -1;
        yVotes += ally.getLocation().getY() >= 5 ? 1 : -1;
      }
      goalX = xVotes > 0 ? 9 : 0;
      goalY = yVotes > 0 ? 9 : 0;
    }

    /* We need to know our Force Field cooldowns even between turns, so store the
       actions in a private field for later use (they aren't visible via the API) */
    for (ReadonlyAction action : actions) {
      if (action.getName().equals("ForceField"))
        forceFieldAction[characterNumber] = action;
    }

    /* If we know Force Field, ensure we always hang on to enough mana to cast it, and
       never allow our mana to dip low enough that it wouldn't regenerate in time. */
    double mpFloor = 0.0;
    if (forceFieldAction[characterNumber] != null) {
      double mpRegen = character.getStat(Stat.INT) / 10.0;
      if (hasAbility(character, "Focused"))
        mpRegen *= 2;
      mpFloor = forceFieldAction[characterNumber].getManaCost();
      mpFloor -= forceFieldAction[characterNumber].getRemainingCooldown() * mpRegen;
    }
    if (mpFloor > character.getMana())
      mpFloor = character.getMana();

    /* We use a priority rule for actions. */
    int bestPriority = -1;
    ReadonlyAction bestAction = null;
    for (ReadonlyAction action : actions) {
      int priority = 0;
      if (lastIdentifier(action.getName()).equals("ForceField"))
        priority = 20; /* top priority */
      else if (character.getMana() - action.getManaCost() < mpFloor) {
        continue; /* never spend mana if it'd block a force field */
      } else if (lastIdentifier(action.getName()).equals("Quick") ||
                 lastIdentifier(action.getName()).equals("Slice")) {
        int damagePotential =
          lastIdentifier(action.getName()).equals("Quick") ? 50 : 25;
        /* We use these abilities with very high priority to /kill/ an enemy
           who's weak enough to die from the damage. If they wouldn't die,
           we're much more wary about attacking; we do it only if we have
           nothing better to do and it's safe. */
        ReadonlyCharacter chosenTarget = null;
        for (ReadonlyCharacter target : action.availableTargets()) {
          if (!isEnemy(target))
            continue;
          if (target.getHealth() <= damagePotential) {
            chosenTarget = target;
            priority = (damagePotential == 25 ? 19 : 18);
            break; /* can't do beter than this */
          }
          if (hasAbility(target, "Spikes") ||
              hasAbility(target, "Reflexive"))
            /*  (target.getLastAction() != null &&
                target.getLastAction().getName().equals("Ghost")) */
            continue; /* veto the target */
          priority = (damagePotential == 25 ? 3 : 4);
          chosenTarget = target;
        }
        if (chosenTarget == null)
          continue;
        action.setTarget(chosenTarget);
      } else if (lastIdentifier(action.getName()).equals("Weave")) {
        priority = visibleEnemies.size() >= 3 ? 14 : 6;
      } else if (lastIdentifier(action.getName()).equals("Smile")) {
        /* If there's a stunned or poisoned enemy in view, we favour Smile
           as the idle action, rather than exploring, so that we don't
           move it out of view. Exception: if they're the only enemy;
           in that case, hunt them down. Another exception: if we're
           running into a corner. */
        for (EnemyCharacter enemy : visibleEnemies) {
          if (enemy.isStunned() || enemy.isPoisoned())
            if (visibleEnemies.size() > 1 && visibleEnemies.size() < 4)
              priority = 2;
        }
        /* otherwise we leave it as 0, and Smile only as a last resort */
      } else if (lastIdentifier(action.getName()).equals("Knockout")) {
        /* Use this only on targets who have more than 50 HP. It doesn't
           matter where they are: if we can see them now, knocking them
           out will guarantee we can continue to see them. Of course, if
           they're already knocked out, don't use it (although this case
           should never come up). If there's only one enemy target in
           view, knocking it out has slightly higher priority, because
           we don't need to fear enemy attacks if all the enemies are
           knocked out.

           Mildly favour stunning poisoned enemies; this reduces the
           chance that they'll run out of sight and reset the poison. */
        ReadonlyCharacter chosenTarget = null;
        for (ReadonlyCharacter target : action.availableTargets())
          if ((target.getHealth() > 50 || target.isPoisoned()) &&
              !target.isStunned() && isEnemy(target)) {
            chosenTarget = target;
            if (target.isPoisoned())
              break;
          }
        if (chosenTarget == null)
          continue;
        action.setTarget(chosenTarget);
        priority = visibleEnemies.size() == 1 ? 17 : 15;
      } else if (lastIdentifier(action.getName()).equals("Poison")) {
        /* Use this preferentially on stronger enemies, and preferentially
           on enemies who are more poisoned. We're willing to poison
           almost anyone, although weak enemies who aren't poisoned
           are faster to kill via slicing. The cutoff is at 49, not 50,
           so that in the case of evasive enemies who we can't hit any
           other way, we can wear them one at a time using poison. */
        ReadonlyCharacter chosenTarget = null;
        int chosenTargetPoisonLevel = -1;
        for (ReadonlyCharacter target : action.availableTargets()) {
          int poisonLevel = 0;

          if (!isEnemy(target))
            continue;
          if (target.isPoisoned())
            poisonLevel = target.getPoisonAmount() + 1;
          if (poisonLevel < chosenTargetPoisonLevel)
            continue;
          if (poisonLevel == 0 && target.getHealth() <= 49)
            continue; /* prefer stronger targets */
          if (poisonLevel == 0 && target.getHealth() == 50 &&
              chosenTarget != null)
            continue; /* we poison at 50, but not with other options */
          chosenTarget = target;
          chosenTargetPoisonLevel = poisonLevel;
          priority = 12;
        }
        if (chosenTarget == null)
          continue;
        action.setTarget(chosenTarget);
      } else if (action.movementAction()) {
        /* A move to a significantly safer square is worth 16.
           A move to a mildly safer square is worth 8.
           Otherwise, move to group, either with the enemy,
           the team, or the goal, at priority 1, if we
           safely can; that's our "idle" action. */
        int currentSquareDanger =
          getSquareDanger(character, character.getLocation());
        int bestSquareDanger = currentSquareDanger;
        int bestGroupiness = 0;
        Point2D bestLocation = null;
        priority = 1;
        for (Point2D location :
               action.availableLocations().toList().shuffleThis(getRandom())) {
          int danger = getSquareDanger(character, location);
          if (danger > bestSquareDanger)
            continue;
          else if (danger < bestSquareDanger) {
            priority = (currentSquareDanger - danger > 2)
              ? 16 : 8;
            bestSquareDanger = danger;
            bestLocation = location;
            bestGroupiness = 0; /* reset the tiebreak */
          }

          int cx = character.getLocation().getX();
          int xDelta = location.getX() - cx;
          int cy = character.getLocation().getY();
          int yDelta = location.getY() - cy;
          int groupiness = 0;
          /* Always hunt down a visible enemy when they're the only
             remaining enemy and doing so is safe. Otherwise, still
             favour hunting them down, but in that situation also
             consider factors like grouping and exploration. */
          for (Point2D enemyLocation : visibleEnemies.keysView())
            if (xDelta * (enemyLocation.getX() - cx) > 0 ||
                yDelta * (enemyLocation.getY() - cy) > 0)
              groupiness += (visibleEnemies.size() == 1 ? 99 : 5);
          /* If there are 4 or more visible enemies, then grouping is
             vitally important (so as to not get surrounded).
             Otherwise, it's more minor. */
          for (ReadonlyCharacter ally : team)
            if (xDelta * (ally.getLocation().getX() - cx) > 0 ||
                yDelta * (ally.getLocation().getY() - cy) > 0)
              groupiness += (visibleEnemies.size() > 3 ? 99 : 3);
          /* When exploring, we bias towards random map locations,
             changing location when we reach them. This helps us beat
             enemies that hide in the corners. When there are a lot
             of visible enemies, this changes to a bias to hide in a
             corner. */
          if (xDelta * (goalX - cx) > 0 ||
              yDelta * (goalY - cy) > 0)
            groupiness += (visibleEnemies.size() > 3 ? 99 : 4);
          if (groupiness >= bestGroupiness) {
            bestLocation = location;
            bestGroupiness = groupiness;
            /* leave priority, safety untouched */
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
