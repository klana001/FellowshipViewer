package fellowship;

import com.nmerrill.kothcomm.game.maps.Point2D;
import fellowship.abilities.ActionAbility;
import fellowship.abilities.mobility.Dash;
import fellowship.abilities.stats.Smart;
import fellowship.abilities.stats.Strong;
import fellowship.abilities.stats.Weak;
import fellowship.abilities.vision.FarSight;
import fellowship.abilities.vision.Invisible;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.damage.Zap;
import fellowship.actions.defensive.Ghost;
import fellowship.actions.defensive.Heal;
import fellowship.actions.vision.Hide;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.set.MutableSet;

public class CowardlyWeakSmartSniper extends Player{

	private final static boolean DEBUG=false; 
	private static Point2D lastAttackedEnemyLocation = null;
    private final double STANDARD_VISION_MOVEMENT_BUFFER = 3;
    private final double MIN_VISION_DISTANCE = 2;
    private final double HEAL_INCREMENT = 20;
    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(1, 1, 3,
                    new ActionAbility(Zap::new),
                    new FarSight(),
                    new FarSight(),
                    new Weak(),
           //         new Strong(),
                    new Smart(),
//                    new ActionAbility(Heal::new),
//                    new Dash()));
                    new ActionAbility(Hide::new)));
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
    	
    	HashMap<Integer, ReadonlyAction> validActions = new HashMap<>();
    	HashMap<Integer, String> actionString = new HashMap<>();
    	
		double closestVisionDistance = Double.MAX_VALUE;
		
		// see if we have arrived at the last attack location of the enemy
		if (character.getLocation().equals(lastAttackedEnemyLocation))
		{
			lastAttackedEnemyLocation = null;
		}
		
		for ( Point2D enemyLocation : visibleEnemies.keySet())
		{
			final int enemyVisibiltyRange = visibleEnemies.get(enemyLocation).getSightRange().getRange();
			double visionDistanceDiff = character.getLocation().diagonalDistance(enemyLocation)-enemyVisibiltyRange;
			if (visionDistanceDiff< closestVisionDistance)
			{
				closestVisionDistance = visionDistanceDiff;
			}
		}
    	
        for (ReadonlyAction action: actions){
			
			int priority=-1;
			String message = "";
        	switch (action.getName())
        	{
        		case "Heal":
//        			if (character.getHealth()<0.75 *character.getMaxHealth())
//        			{
//        				priority = 200;
//        			}
//        			else if (character.getHealth()<0.5 *character.getMaxHealth())
//        			{
//        				priority = 200;
//        			}
//        			else
//        			{
//	        			if (closestVisionDistance > 4 )
//	        			{
//	        				priority = 200;	        				
//	        			}
//        			}
        			// are we outside the sight range of an enemy
    				if (closestVisionDistance > STANDARD_VISION_MOVEMENT_BUFFER )
    				{
    					// heal team members
//						MutableSet<ReadonlyCharacter> teamMates = action.availableTargets();
        				ReadonlyCharacter chosenTarget = null;
        				double chosenTargetHealth = Double.MAX_VALUE;
        				for (ReadonlyCharacter target : action.availableTargets())
        				{
        					if (DEBUG) System.out.println(""+(target.getHealth()+HEAL_INCREMENT)+" " +target.getMaxHealth());
        					if (target.getHealth()+HEAL_INCREMENT < target.getMaxHealth() -1 && target.getHealth() < chosenTargetHealth)
        					{
        						chosenTargetHealth = target.getHealth();
        						chosenTarget = target;
        					}
        				}
        				
        				if (chosenTarget != null)
        				{
            				priority = 900;
        					action.setTarget(chosenTarget);
        				}
    				}
        		
        		break;
        	
        		case "Hide":
        			// are we, or will we be within sight range of an enemy
    				if (closestVisionDistance < STANDARD_VISION_MOVEMENT_BUFFER )
    				{
    					if (!character.isInvisible())
     					{
    	        			message = ""+closestVisionDistance;
    						priority = 1000;
    					}
    				}
    				break;
        	
        		case "Step":
        			
    				Point2D chosenLocation = null;
   				
    				// are we within sight range of an enemy
    				if (closestVisionDistance < MIN_VISION_DISTANCE)
    				{
    					message = "Fleeing "+ closestVisionDistance;
    					priority = 800;
            			if (character.isInvisible())
            			{
            				priority = 500;
            			}
            			
        				// avoid enemies try, to spread out
  				
        				double furthestDistance = 0;
        				
        				for ( Point2D enemyLocation : visibleEnemies.keySet())
        				{
	        				for (Point2D location : action.availableLocations())
	        				{
	        					if (location.diagonalDistance(enemyLocation) > furthestDistance)
	        					{
	        						furthestDistance = location.diagonalDistance(enemyLocation);
	        						chosenLocation = location;
	        					}
	        				}
        				}
        				
        				if (chosenLocation == null)
        				{
        					priority = -1;
        					break;
        				}
    				}
    				else if (lastAttackedEnemyLocation !=null)
    				{
    					priority = 20;
    					message = "Tracking "+ closestVisionDistance;
    					// head toward new last attacked enemy location
    					double distance = Integer.MAX_VALUE;
        				for (Point2D location : action.availableLocations())
        				{
        					if (location.diagonalDistance(lastAttackedEnemyLocation) < distance)
        					{
        						distance = location.diagonalDistance(lastAttackedEnemyLocation);
        						chosenLocation = location;
        					}
        				}
    				}
    				// are we outside the sight range of an enemy
    				else if (closestVisionDistance > STANDARD_VISION_MOVEMENT_BUFFER)
    				{
//    					break;
//    					if (!character.isInvisible())
//    					{
//    						// do not move if we are not invisible
//    						break;
//    					}
//    					else
    					{
    						priority = 10;
    						message = "Scouting "+ closestVisionDistance;
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
        			}
    				else
    				{
    					break;
    				}
        			
        			action.setLocation(chosenLocation);
        			break;
        		
        		case "Zap":
//        			if (character.isInvisible())
        			{
        				message = ""+closestVisionDistance;
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
            				priority = 100;
        					action.setTarget(chosenTarget);
        					lastAttackedEnemyLocation = chosenTarget.getLocation();
        				}
        				else
        				{
        					// nothing to target
        				}
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

        if (DEBUG) System.out.println(this+"("+System.identityHashCode(character)+"): "+chosen.getName()+ " "+message +" H: "+character.getHealth()+" M: "+character.getMana() +(character.isInvisible()?" InVis":" Vis") +" x: "+character.getLocation().getX()+" y: "+character.getLocation().getY());
        return chosen;
    }
}