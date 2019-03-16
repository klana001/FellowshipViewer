package fellowship;

import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Flexible;
import fellowship.abilities.attacking.Ranged;
import fellowship.actions.Action;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.attacking.Quick;
import fellowship.actions.attacking.Weave;
import fellowship.actions.damage.KO;
import fellowship.actions.damage.Zap;
import fellowship.actions.mobility.Blink;
import fellowship.actions.mobility.Step;
import fellowship.actions.mobility.Swap;
import fellowship.actions.mobility.Teleport;
import fellowship.actions.other.Smile;
import fellowship.characters.BaseCharacter;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.set.MutableSet;

import com.nmerrill.kothcomm.game.maps.Point2D;

public class WeaveBot extends Player{

    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(10, 5, 5,
                    new ActionAbility(Weave::new),
                    new ActionAbility(Blink::new),
                    new ActionAbility(Swap::new),
                    new ActionAbility(Teleport::new)));
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
        int minPriority = Integer.MAX_VALUE;
        ReadonlyAction chosen = null;
        ReadonlyAction action;
        if (visibleEnemies.size()>0)
        {
            action = actions.stream().filter(act->act.getName().equals("Weave")).findAny().orElse(null);
            
            if (action!=null)
            {
    	    	chosen = action;
            }
            else
            {
	        	
	        	action = actions.stream().filter(act->act.getName().equals("Step")).findAny().orElse(null);
					
	        	if (action != null && action.availableLocations() !=null &&action.availableLocations().size()>0)
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
        return chosen;
    }

}
