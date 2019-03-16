package fellowship;

import com.nmerrill.kothcomm.game.maps.Point2D;
import fellowship.abilities.ActionAbility;
import fellowship.abilities.mobility.Dash;
import fellowship.abilities.stats.Clever;
import fellowship.abilities.stats.Smart;
import fellowship.abilities.stats.Strong;
import fellowship.abilities.stats.Weak;
import fellowship.abilities.vision.FarSight;
import fellowship.abilities.vision.Invisible;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.damage.Lightning;
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

public class LightningBot extends Player{

	private final static boolean DEBUG=false; 
    private final double STANDARD_VISION_MOVEMENT_BUFFER = 3;
    private final double MIN_VISION_DISTANCE = 2;
    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(1, 1, 18,
                    new FarSight(),
                    new ActionAbility(Lightning::new),
                    new Smart(),
                    new Smart()));
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
    	
    	HashMap<Integer, ReadonlyAction> validActions = new HashMap<>();
    	HashMap<Integer, String> actionString = new HashMap<>();
    	
		double closestVisionDistance = Double.MAX_VALUE;
		
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
//        		case "Lightning":
//        			//if (character.getMana()>character.getMaxMana()/2)
//        			{
//        				priority = 50;
//        			}
//        			break;
        	
        		
        	
        		case "Step":
        			
    				Point2D chosenLocation = null;
   				
    				// are we within sight range of an enemy
    				if (closestVisionDistance < MIN_VISION_DISTANCE)
    				{
    					message = "Fleeing "+ closestVisionDistance;
    					priority = 800;
    				}
    				else
    				{
    					message = "Avoiding "+ closestVisionDistance;
    					priority = 10;
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

        			
        			action.setLocation(chosenLocation);
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
        
        if (chosen.getName().equals("Lightning"))
        	System.out.println("Lightning!");
        if (DEBUG) System.out.println(this+"("+System.identityHashCode(character)+"): "+chosen.getName()+ " "+message +" H: "+character.getHealth()+" M: "+character.getMana() +(character.isInvisible()?" InVis":" Vis") +" x: "+character.getLocation().getX()+" y: "+character.getLocation().getY());
        return chosen;
    }
}