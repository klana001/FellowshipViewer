package fellowship;

import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Absorb;
import fellowship.abilities.attacking.Cleave;
import fellowship.abilities.attacking.Feast;
import fellowship.abilities.attacking.Flexible;
import fellowship.abilities.attacking.ManaSteal;
import fellowship.abilities.attacking.Ranged;
import fellowship.abilities.attacking.Reflexive;
import fellowship.abilities.attacking.Swipe;
import fellowship.abilities.damage.Static;
import fellowship.abilities.defensive.Spikes;
import fellowship.abilities.statuses.Cold;
import fellowship.actions.Action;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.attacking.Quick;
import fellowship.actions.attacking.Weave;
import fellowship.actions.damage.KO;
import fellowship.actions.damage.Lightning;
import fellowship.actions.damage.Trap;
import fellowship.actions.damage.Zap;
import fellowship.actions.defensive.ForceField;
import fellowship.actions.defensive.Ghost;
import fellowship.actions.defensive.Heal;
import fellowship.actions.defensive.Restore;
import fellowship.actions.defensive.Shield;
import fellowship.actions.mobility.Blink;
import fellowship.actions.mobility.Step;
import fellowship.actions.mobility.Swap;
import fellowship.actions.mobility.Teleport;
import fellowship.actions.other.Smile;
import fellowship.actions.other.Wall;
import fellowship.actions.statuses.Dispel;
import fellowship.actions.statuses.Duel;
import fellowship.actions.statuses.Knockout;
import fellowship.actions.statuses.Leash;
import fellowship.actions.statuses.Meteor;
import fellowship.actions.statuses.Silence;
import fellowship.characters.BaseCharacter;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.set.MutableSet;

import com.nmerrill.kothcomm.game.maps.Point2D;

public class QuickBot extends Player{

    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(10, 5, 5,
                    new ActionAbility(Trap::new),
                    new Swipe(),
                    new ActionAbility(Lightning::new),
                    new Static())); 
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
        int minPriority = Integer.MAX_VALUE;
        ReadonlyAction chosen = null;
        
        ReadonlyAction action = actions.stream().filter(act->act.getName().endsWith("Weave")).findAny().orElse(null);
        
        if (action!=null)
        {
        	return action;
        }
        
action = actions.stream().filter(act->act.getName().equals("Trap")).findAny().orElse(null);
        
        if (action!=null)
        {
    		MutableSet<Point2D> targets = action.availableLocations();
    		if (targets.size()>0)
    		{
	    		int i=getRandom().nextInt(targets.size());
	    		for (Point2D target : targets)
	    		{
	    			action.setLocation(target);
	    			if (--i==0)
	    			{
	    				break;
	    			}
	    		}
	    		chosen = action;
	    		return chosen;
    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("KO")).findAny().orElse(null);
        
        if (action!=null)
        {
    		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
    		if (targets.size()>0)
    		{
	    		int i=getRandom().nextInt(targets.size());
	    		for (ReadonlyCharacter target : targets)
	    		{
	    			action.setTarget(target);
	    			if (--i==0)
	    			{
	    				break;
	    			}
	    		}
	    		chosen = action;
	    		return chosen;
    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("Lightning")).findAny().orElse(null);
        
        if (action!=null)
        {
//    		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
//    		if (targets.size()>0)
//    		{
//	    		int i=getRandom().nextInt(targets.size());
//	    		for (ReadonlyCharacter target : targets)
//	    		{
//	    			action.setTarget(target);
//	    			if (--i==0)
//	    			{
//	    				break;
//	    			}
//	    		}
	    		chosen = action;
	    		return chosen;
//    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("Shield")).findAny().orElse(null);
        
        if (action!=null)
        {
//    		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
//    		if (targets.size()>0)
//    		{
//	    		int i=getRandom().nextInt(targets.size());
//	    		for (ReadonlyCharacter target : targets)
//	    		{
//	    			action.setTarget(target);
//	    			if (--i==0)
//	    			{
//	    				break;
//	    			}
//	    		}
	    		chosen = action;
	    		return chosen;
//    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("Restore")).findAny().orElse(null);
        
        if (action!=null)
        {
//    		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
//    		if (targets.size()>0)
//    		{
//	    		int i=getRandom().nextInt(targets.size());
//	    		for (ReadonlyCharacter target : targets)
//	    		{
//	    			action.setTarget(target);
//	    			if (--i==0)
//	    			{
//	    				break;
//	    			}
//	    		}
	    		chosen = action;
	    		return chosen;
//    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("Heal")).findAny().orElse(null);
        
        if (action!=null)
        {
    		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
    		if (targets.size()>0)
    		{
	    		int i=getRandom().nextInt(targets.size());
	    		for (ReadonlyCharacter target : targets)
	    		{
	    			action.setTarget(target);
	    			if (--i==0)
	    			{
	    				break;
	    			}
	    		}
	    		chosen = action;
	    		return chosen;
    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("Ghost")).findAny().orElse(null);
        
        if (action!=null)
        {
//    		MutableSet<Point2D> targets = action.availableLocations();
//    		if (targets.size()>0)
//    		{
//	    		int i=getRandom().nextInt(targets.size());
//	    		for (Point2D target : targets)
//	    		{
//	    			action.setLocation(target);
//	    			if (--i==0)
//	    			{
//	    				break;
//	    			}
//	    		}
	    		chosen = action;
	    		return chosen;
//    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("ForceField")).findAny().orElse(null);
        
        if (action!=null)
        {
//    		MutableSet<Point2D> targets = action.availableLocations();
//    		if (targets.size()>0)
//    		{
//	    		int i=getRandom().nextInt(targets.size());
//	    		for (Point2D target : targets)
//	    		{
//	    			action.setLocation(target);
//	    			if (--i==0)
//	    			{
//	    				break;
//	    			}
//	    		}
	    		chosen = action;
	    		return chosen;
//    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("Meteor")).findAny().orElse(null);
        
        if (action!=null)
        {
//    		MutableSet<Point2D> targets = action.availableLocations();
//    		if (targets.size()>0)
//    		{
//	    		int i=getRandom().nextInt(targets.size());
//	    		for (Point2D target : targets)
//	    		{
//	    			action.setLocation(target);
//	    			if (--i==0)
//	    			{
//	    				break;
//	    			}
//	    		}
	    		chosen = action;
	    		return chosen;
//    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("Wall")).findAny().orElse(null);
        
        if (action!=null)
        {
    		MutableSet<Point2D> targets = action.availableLocations();
    		if (targets.size()>0)
    		{
	    		int i=getRandom().nextInt(targets.size());
	    		for (Point2D target : targets)
	    		{
	    			action.setLocation(target);
	    			if (--i==0)
	    			{
	    				break;
	    			}
	    		}
	    		chosen = action;
	    		return chosen;
    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("Slice")).findAny().orElse(null);
        
        if (action!=null)
        {
    		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
    		if (targets.size()>0)
    		{
	    		int i=getRandom().nextInt(targets.size());
	    		for (ReadonlyCharacter target : targets)
	    		{
	    			action.setTarget(target);
	    			if (--i==0)
	    			{
	    				break;
	    			}
	    		}
	    		chosen = action;
	    		return chosen;
    		}
        }
        
        action = actions.stream().filter(act->act.getName().equals("Duel")).findAny().orElse(null);
        
        if (action!=null)
        {
    		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
    		if (targets.size()>0)
    		{
	    		int i=getRandom().nextInt(targets.size());
	    		for (ReadonlyCharacter target : targets)
	    		{
	    			action.setTarget(target);
	    			if (--i==0)
	    			{
	    				break;
	    			}
	    		}
	    		chosen = action;
	    		return chosen;
    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("Leash")).findAny().orElse(null);
        
        if (action!=null)
        {
    		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
    		if (targets.size()>0)
    		{
	    		int i=getRandom().nextInt(targets.size());
	    		for (ReadonlyCharacter target : targets)
	    		{
	    			action.setTarget(target);
	    			if (--i==0)
	    			{
	    				break;
	    			}
	    		}
	    		chosen = action;
	    		return chosen;
    		}
        }
        
action = actions.stream().filter(act->act.getName().equals("Knockout")).findAny().orElse(null);
        
        if (action!=null)
        {
    		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
    		if (targets.size()>0)
    		{
	    		int i=getRandom().nextInt(targets.size());
	    		for (ReadonlyCharacter target : targets)
	    		{
	    			action.setTarget(target);
	    			if (--i==0)
	    			{
	    				break;
	    			}
	    		}
	    		chosen = action;
	    		return chosen;
    		}
        }
        
        action = actions.stream().filter(act->act.getName().equals("Quick")).findAny().orElse(null);
        
        if (action!=null)
        {
    		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
    		if (targets.size()>0)
    		{
	    		int i=getRandom().nextInt(targets.size());
	    		for (ReadonlyCharacter target : targets)
	    		{
	    			action.setTarget(target);
	    			if (--i==0)
	    			{
	    				break;
	    			}
	    		}
	    		chosen = action;
    		}
        }
        else if (visibleEnemies.size()>0)
        {
	        action = actions.stream().filter(act->act.getName().equals("Step")).findAny().orElse(null);
	        if (action!=null)
	        {					
					double closestDistance = Double.MAX_VALUE;
					
					Point2D chosenLocation = null;
					for ( Point2D enemyLocation : visibleEnemies.keySet())
					{
						for (Point2D location : action.availableLocations())
						{
							if (location.diagonalDistance(enemyLocation) < closestDistance)
							{
								closestDistance = location.diagonalDistance(enemyLocation);
								chosenLocation  = location;
							}
						}
					}
		    		chosen = action;
		    		action.setLocation(chosenLocation);
        	}
        }
        else
        {
        	action = actions.stream().filter(act->act.getName().equals("Teleport")).findAny().orElse(null);
        	
        	if (action !=null)
			{
        		if (chosen == null && Math.random()>0.5)
        		{
        		MutableSet<Point2D> targets = action.availableLocations();
        		int i=getRandom().nextInt(targets.size());
        		for (Point2D location : targets)
        		{
        			action.setLocation(location);
        			if (--i==0)
        			{
        				break;
        			}
        		}
        		chosen = action;
        		}
			}
        
        }

        if (chosen == null)
        {
        	chosen = actions.stream().filter(act->act.getName().equals("Smile")).findAny().orElse(null);
        }
        if (chosen == null)
        {
        	System.out.println("HELP");
        }
        return chosen;
    }

}
