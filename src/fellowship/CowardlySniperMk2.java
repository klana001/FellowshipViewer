	package fellowship;
	
	import com.nmerrill.kothcomm.game.maps.Point2D;
	import fellowship.abilities.ActionAbility;
	import fellowship.abilities.vision.FarSight;
	import fellowship.actions.ReadonlyAction;
	import fellowship.actions.damage.Zap;
	import fellowship.actions.vision.Hide;
	import fellowship.characters.CharacterTemplate;
	import fellowship.characters.ReadonlyCharacter;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Set;

import org.eclipse.collections.api.set.MutableSet;

import fellowship.*;
	
	public class CowardlySniperMk2 extends Player{
	
		private final static boolean DEBUG=false; 
		private static Point2D lastAttackedEnemyLocation = null;
		private static HashMap<ReadonlyCharacter, Boolean> rechargingManaMap = new HashMap<>();
	    private final double STANDARD_VISION_MOVEMENT_BUFFER = 3;
	    private final double MIN_VISION_DISTANCE = 2;
	    private final double MIN_ATTACK_DISTANCE = 0;
	
	    @Override
	    public List<CharacterTemplate> createCharacters() {
	        List<CharacterTemplate> templates = new ArrayList<>();
	        for (int i = 0; i < 3; i++) {
	            templates.add(new CharacterTemplate(8, 8, 4,
	                    new ActionAbility(Zap::new),
	                    new FarSight(),
	                    new FarSight(),
	                    new ActionAbility(Hide::new)));
	        }
	        return templates;
	    }
	
	    @Override
	    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {

	    	// get last flag for recharging mana
	    	Boolean rechargingMana = rechargingManaMap.get(character);
	    	if (rechargingMana == null || rechargingMana)
	    	{
	    		rechargingMana = !(character.getMana()>0.90*character.getMaxMana());
	    	}
	    	else
	    	{
	    		rechargingMana = character.getMana()<0.10*character.getMaxMana();
	    	}
	    	
	    	rechargingManaMap.put(character,rechargingMana);
	    	
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
	        	switch (action.getName())
	        	{
	        		case "Hide":
	        			// are we, or will we be within sight range of an enemy
	    				if (closestEnemyVisionDistance < STANDARD_VISION_MOVEMENT_BUFFER )
	    				{
	    					if (!character.isInvisible())
	     					{
	    	        			message = ""+closestEnemyVisionDistance;
	    						priority = 900;
	    					}
	    				}
	    				break;
	        	
	        		case "Step":
	        			
	    				Point2D chosenLocation = null;
	   				
	    				// are we within sight range of an enemy or are we recharging mana?
					final MutableSet<Point2D> availableLocations = action.availableLocations();
					if (closestEnemyVisionDistance < MIN_VISION_DISTANCE || rechargingMana)
	    				{
	    					message = "Fleeing (Seen) "+ closestEnemyVisionDistance;
	    					priority = 800;
	    					
	            			if (character.isInvisible())
	            			{
	            				message = "Fleeing (UnSeen) "+ closestEnemyVisionDistance;
	            				priority = 500;
	            			}
	            			
	        				// simple enemy avoidance... chose location that is farthest away from closest enemy
	        				double furthestDistance = 0;
	        				
	        				for ( Point2D enemyLocation : visibleEnemies.keySet())
	        				{
		        				for (Point2D location : availableLocations)
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
	        					// no moves are better than staying in current location
	        					priority = -1;
	        					break;
	        				}
	    				}
	    				// are we "tracking" an enemy?
	    				else if (lastAttackedEnemyLocation !=null)
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
	    				// are we outside the sight range of all enemies?
	    				else if (closestEnemyVisionDistance > STANDARD_VISION_MOVEMENT_BUFFER)
	    				{
	        				// scout for an enemy
	    					
							priority = 10;
							message = "Scouting "+ closestEnemyVisionDistance;
							
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
	    				else
	    				{
	    					// we are in the sweet zone... just out of enemy sight range but within our sight range
	    					break;
	    				}
	        			
	        			action.setLocation(chosenLocation);
	        			break;
	        		
	        		case "Zap":
	    				message = ""+closestEnemyVisionDistance;
	    				ReadonlyCharacter chosenTarget = null;
	    				double chosenTargetHealth = Double.MAX_VALUE;
	    				
	    				// target the weakest enemy
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
	    					
	    					// there are either clones or bears in the world... it will be very hard to keep stand off distance for all enemies
	    					// so try to thin out their numbers if reasonably safe to do so
	    					if (closestEnemyVisionDistance >= MIN_ATTACK_DISTANCE && visibleEnemies.size()>3)
	    					{
	    						priority = 950;
	    					}
	        				
	    					action.setTarget(chosenTarget);
	    					lastAttackedEnemyLocation = chosenTarget.getLocation();
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
	        
	        if (DEBUG) System.out.println(this+"("+System.identityHashCode(character)+"): "+chosen.getName()+ (rechargingMana?" Mana_charge":" Mana_usable")+" H: "+character.getHealth()+" M: "+character.getMana() +(character.isInvisible()?" InVis":" Vis") +" x: "+character.getLocation().getX()+" y: "+character.getLocation().getY()+" "+message);
	        return chosen;
	    }
	}