	package fellowship;
	
	import com.nmerrill.kothcomm.game.maps.Point2D;
	import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Critical;
import fellowship.abilities.attacking.Swipe;
import fellowship.abilities.defensive.Pillar;
import fellowship.abilities.vision.FarSight;
	import fellowship.actions.ReadonlyAction;
import fellowship.actions.damage.Drain;
import fellowship.actions.damage.Zap;
import fellowship.actions.other.Steal;
import fellowship.actions.vision.Hide;
	import fellowship.characters.CharacterTemplate;
	import fellowship.characters.ReadonlyCharacter;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Set;

import org.eclipse.collections.api.set.MutableSet;

import fellowship.*;
	
	public class LuckyDevil extends Player{
	
		private final static boolean DEBUG=true; 
		private static Point2D lastAttackedEnemyLocation = null;

	
	    @Override
	    public List<CharacterTemplate> createCharacters() {
	        List<CharacterTemplate> templates = new ArrayList<>();
	        for (int i = 0; i < 3; i++) {
	            templates.add(new CharacterTemplate(20, 0, 0,
	                    new Critical(),
	                    new Critical(),
	                    new Swipe(),
	                    new Pillar()));
	        }
	        return templates;
	    }
	
	    @Override
	    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {

	    	HashMap<Integer, ReadonlyAction> validActions = new HashMap<>();
	    	HashMap<Integer, String> actionString = new HashMap<>();
	    	
			// see if we have arrived at the last attack location of the enemy
			if (character.getLocation().equals(lastAttackedEnemyLocation))
			{
				lastAttackedEnemyLocation = null;
			}
			
			double closestEnemyVisionDistance = Double.MAX_VALUE;
			for ( Point2D enemyLocation : visibleEnemies.keySet())
			{
				final int enemyVisibiltyRange = visibleEnemies.get(enemyLocation).getSightRange().getRange();
				double visionDistanceDiff = character.getLocation().diagonalDistance(enemyLocation)-enemyVisibiltyRange;
				if (visionDistanceDiff< closestEnemyVisionDistance)
				{
					closestEnemyVisionDistance = visionDistanceDiff;
				}
			}
	
	        for (ReadonlyAction action: actions){
				
				int priority=-1;
				String message = "";
				
				final MutableSet<Point2D> availableLocations = action.availableLocations();
				
	        	switch (action.getName())
	        	{
//	        		case "Drain":
//	        			if (action.getRemainingCooldown() == 0)
//	        			{
//    						priority = 100;
//    						
//    	        			ReadonlyCharacter target = action.availableTargets().stream().findFirst().orElse(null);
//    	        			if (target != null)
//    	        			{
//    	        				action.setTarget(target);
//    	        				lastAttackedEnemyLocation = target.getLocation();
//    	        				message = "Drain";
//    	        			}
//	    				}
//	    				break;
	    				
	        		case "Slice":
//        				for (Point2D location : availableLocations)
//        				{
//        					for ( Point2D enemyLocation : visibleEnemies.keySet())
//        					{
//        						if (enemyLocation.getX() == location.getX() && enemyLocation.getY() == location.getY())
//        						{
//        							priority=90;
//        							action.setLocation(location);
//        							message = "Slicing "+ location;
//        							break;
//        						}
//        					}
//        				}
	        			ReadonlyCharacter target = action.availableTargets().stream().findFirst().orElse(null);
	        			if (target != null)
	        			{
	        				action.setTarget(target);
	        				lastAttackedEnemyLocation = target.getLocation();
	        				priority=90;
	        				message = "Slicing " + target.getLocation();
	        			}
	        			
	    				break;
	        	
	        		case "Step":
	        			
	    				Point2D chosenLocation = null;
	   				

					if (lastAttackedEnemyLocation !=null)
	    				{
	    					priority = 20;
	    					message = "Tracking "+ closestEnemyVisionDistance;
	    					
	    					// head toward last attacked enemy location
	    					double distance = Integer.MAX_VALUE;
	        				for (Point2D location : availableLocations)
	        				{
	        					if (location.diagonalDistance(lastAttackedEnemyLocation) < distance)
	        					{
	        						distance = location.diagonalDistance(lastAttackedEnemyLocation);
	        						chosenLocation = location;
	        					}
	        				}
	    				}
	    				else 
	    				{
	            			
	        				// chose location that is closet to closest enemy
	        				double closestDistance = Integer.MAX_VALUE;
	        				
	        				for ( Point2D enemyLocation : visibleEnemies.keySet())
	        				{
		        				for (Point2D location : availableLocations)
		        				{
		        					if (location.diagonalDistance(enemyLocation) < closestDistance)
		        					{
		        						closestDistance = location.diagonalDistance(enemyLocation);
		        						chosenLocation = location;
		        					}
		        				}
	        				}
	        				
	        				if (chosenLocation != null)
	        				{
	        					priority = 15;
	        					message = "Scouting (Enemy Spotted)";
	        					lastAttackedEnemyLocation = chosenLocation;
	        				}
	        				else
	        				{
		    					
		        				// scout for an enemy
		    					
								priority = 10;
								message = "Scouting (random)";
								
								// dumb random location selection... not optimal but is sufficent.
								int index = getRandom().nextInt(availableLocations.size());
		        				for (Point2D location : availableLocations)
		        				{
		    						chosenLocation= location;
		        					if (--index == 0)
		        					{
		        						break;
		        					}
		        				}
	        				}
	        			}
	        			
	        			action.setLocation(chosenLocation);
	        			break;
	        			
	        		case "Smile":
	        			priority = 0;
	        			break;
	        	}
	        	
	        	// add the action to the collection of valid actions to perform
	        	if (priority >-1)
	        	{
	        		validActions.put(priority, action);
	        		actionString.put(priority, message);
	        	}
	        	
	        }
	    	
	
	        int highestPriority = -1;
	        ReadonlyAction chosen = null;
	        
	        // choose the highest priority action
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
	        
	        if (DEBUG) System.out.println(this+"("+System.identityHashCode(character)+"): "+chosen.getName()+" H: "+character.getHealth()+" M: "+character.getMana() +(character.isInvisible()?" InVis":" Vis") +" x: "+character.getLocation().getX()+" y: "+character.getLocation().getY()+" "+message);
	        return chosen;
	    }
	}