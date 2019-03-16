package fellowship;

import com.nmerrill.kothcomm.game.maps.Point2D;
import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Feast;
import fellowship.abilities.attacking.Reflexive;
import fellowship.abilities.attacking.Swipe;
import fellowship.abilities.mobility.Dash;
import fellowship.abilities.vision.FarSight;
import fellowship.abilities.vision.Invisible;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.damage.Zap;
import fellowship.actions.defensive.Restore;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Swiper extends Player{
	private final static HashMap<ReadonlyAction, Integer> uniqueIdMap = new HashMap<>();
//	private static int uniqueId=0;
    private final double CRITICAL_HEALTH = 20;
    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(18, 1, 1,
                    new Reflexive(),
                    new Swipe(),
                    new Feast(),
                    new ActionAbility(Restore::new)));
//                    new Invisible()));
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
    	
    	HashMap<Integer, ReadonlyAction> validActions = new HashMap<>();
    	HashMap<Integer, String> actionString = new HashMap<>();
    	
        for (ReadonlyAction action: actions){
			
			int priority=-1;
			String message = "";
        	switch (action.getName())
        	{
        		case "Step":
        			double closestDistance = Double.MAX_VALUE;
    				Point2D chosenLocation = null;
    				
    				for ( Point2D enemyLocation : visibleEnemies.keySet())
    				{
        				for (Point2D location : action.availableLocations())
        				{
        					if (location.diagonalDistance(enemyLocation) < closestDistance)
        					{
        						closestDistance = location.diagonalDistance(enemyLocation);
        						chosenLocation = location;
        					}
        				}
    				}
   				
    				// are we within sight range of an enemy
    				if (chosenLocation != null)
    				{
    					message = "moving to enemy "+ closestDistance;
    					priority = 100;
    				}
    				else
    				{
						priority = 10;
						message = "Scouting ";
        				// scout for an enemy
						
						// temporarily choosing random location...
						int index = getRandom().nextInt(action.availableLocations().size());
        				for (Point2D location : action.availableLocations())
        				{
    						chosenLocation= location;
        					if (--index == 0)
        					{
        						break;
        					}
        				}
        			}
        			
        			action.setLocation(chosenLocation);
        			break;
        		
        		case "Restore":
        			if (character.getHealth()<60)
        			{
        				priority = 1000;
        			}
        			break;
        			
        		case "Swipe":
    				ReadonlyCharacter chosenTarget = null;
    				double chosenTargetHealth = Double.MAX_VALUE;
    				for (ReadonlyCharacter target : action.availableTargets())
    				{
    					if (target.getHealth() < chosenTargetHealth)
    					{
    						chosenTargetHealth = target.getHealth();
    						chosenTarget = target;
    					}
    				}
    				
    				if (chosenTarget != null)
    				{
        				priority = 500;
    					action.setTarget(chosenTarget);
    				}
    				else
    				{
    					// nothing to target
    				}
        			break;
        			
        		case "Smile":
        			priority = 0;
        			break;
        	}
        	
        	if (priority >-1)
        	{
        		validActions.put(priority, action);
        		actionString.put(priority, message);
        	}
        	
        }
    	
        int highestPriority = -1;
        ReadonlyAction chosen = null;
        for (Integer priority : validActions.keySet())
        {
        	if (priority > highestPriority)
        	{
        		highestPriority = priority;
        		chosen = validActions.get(priority);
        	}
        }
        String message = actionString.get(highestPriority);
        
        if (chosen == null){
            throw new RuntimeException("No valid actions");
        }
        
        
//        int id;
//		if (character.getLastAction() == null)
//        {
//        	id = uniqueId++;
//        }
//        else
//        {
//        	id = uniqueIdMap.get(character.getLastAction());
//        	uniqueIdMap.remove(character.getLastAction());
//        }
//		
//		uniqueIdMap.put(chosen, id);

        System.out.println(this+"("+System.identityHashCode(character)+"): "+chosen.getName()+ " "+message +" H: "+character.getHealth()+" M: "+character.getMana() +(character.isInvisible()?" InVis":" Vis") +" x: "+character.getLocation().getX()+" y: "+character.getLocation().getY());
        return chosen;
    }
}