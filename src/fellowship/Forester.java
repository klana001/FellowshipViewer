	package fellowship;
	
	import com.nmerrill.kothcomm.game.maps.Point2D;
	import fellowship.abilities.ActionAbility;
import fellowship.abilities.ReadonlyAbility;
import fellowship.abilities.stats.Clever;
import fellowship.abilities.stats.Strong;
import fellowship.abilities.vision.FarSight;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.defensive.Restore;
import fellowship.actions.other.Bear;
import fellowship.actions.statuses.Poison;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.EnemyCharacter;
import fellowship.characters.ReadonlyCharacter;
	import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
	import java.util.List;
	import java.util.Set;
import java.util.stream.Collectors;
	
	public class Forester extends Player{
	
		enum TeamMemberType
		{
			BEAR,
			FORESTER_VETERINARIAN,
			FORESTER_HERBALIST,
			FORESTER_SAGE;
			
			static TeamMemberType valueOf(ReadonlyCharacter character)
			{
//				for (ReadonlyAbility ability:character.getAbilities())
//				{
//					System.out.println("ability name: "+ability.name());
//					if (ability.name().equals("Ability Restore"))
//					{
//						System.out.println("asdf");
//					}
//				}
				
				// VETERINARIAN check
				if (character.getAbilities().stream().filter(ability->ability.name().equals("Ability Restore")).findAny().isPresent())
				{
					return TeamMemberType.FORESTER_VETERINARIAN;
				}
				
				// HERBALIST check
				if (character.getAbilities().stream().filter(ability->ability.name().equals("Ability Poison")).findAny().isPresent())
				{
					return TeamMemberType.FORESTER_HERBALIST;
				}
				
				return BEAR;
			}

			public static boolean isBear(ReadonlyCharacter enemy)
			{
				return enemy.getStat(Stat.STR)+enemy.getStat(Stat.AGI)+enemy.getStat(Stat.INT)==15;
			}
		}
		
		private final static boolean DEBUG=true; 
	
	    @Override
	    public List<CharacterTemplate> createCharacters() {
	        List<CharacterTemplate> templates = new ArrayList<>();

	            templates.add(new CharacterTemplate(0, 40, 10,
	            		new Strong(),
	                    new Strong(),
	                    new Strong(),
	                    new ActionAbility(Poison::new)));
	            
	            templates.add(new CharacterTemplate(0, 40, 10,
	            		new Strong(),
	                    new Strong(),
	                    new Strong(),
	                    new ActionAbility(Poison::new)));
//	            
//	            templates.add(new CharacterTemplate(0, 45, 5,
//	            		new Strong(),
//	                    new Strong(),
//	                    new Strong(),
//	                    new ActionAbility(Poison::new)));
//	            
//	            templates.add(new CharacterTemplate(0, 10, 10,
//	            		new Clever(),
//	                    new Clever(),
//	                    new ActionAbility(Restore::new),
//	                    new ActionAbility(Bear::new)));
	            
	            templates.add(new CharacterTemplate(0, 15, 15,
	            		new Strong(),
	                    new Clever(),
	                    new ActionAbility(Poison::new),
	                    new ActionAbility(Bear::new)));
	            
//	            templates.add(new CharacterTemplate(0, 45, 5,
//	            		new Strong(),
//	                    new Strong(),
//	                    new Strong(),
//	                    new ActionAbility(Poison::new)));
	        
	        return templates;
	    }
	    
	    @Override
	    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
	    	HashMap<Integer, ReadonlyAction> validActions = new HashMap<>();
	    	HashMap<Integer, String> actionString = new HashMap<>();
	    	TeamMemberType type = TeamMemberType.valueOf(character);

//			for (ReadonlyAction action : actions)
//	    	{
//	    		System.out.println(action.getName());
//	    	}
	    	
	    	switch (type)
	    	{
		    	case BEAR:
		    		chooseBear(actions, character, validActions, actionString);
		    		break;
		    	
		    	case FORESTER_VETERINARIAN:
		    		chooseForesterVeterinarian(actions, character, validActions, actionString);
		    		break;
		    		
		    	case FORESTER_HERBALIST:
		    		chooseForesterHerbalist(actions, character, validActions, actionString);
		    		break;
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
	        
	        if (DEBUG) 
	        	System.out.println(this+"("+System.identityHashCode(character)+"("+type+")): "+chosen.getName()+" H: "+character.getHealth()+" M: "+character.getMana() +(character.isInvisible()?" InVis":" Vis") +" x: "+character.getLocation().getX()+" y: "+character.getLocation().getY()+" "+message);
	        return chosen;
	    }
	    
	    void chooseBase(Set<ReadonlyAction> actions, ReadonlyCharacter character,HashMap<Integer, ReadonlyAction> validActions,	HashMap<Integer, String> actionString)
	    {
	    	ReadonlyAction smileAction = actions.stream().filter(action->action.getName().equals("Smile")).findAny().orElseGet(null);
	    	if (smileAction!=null)
	    	{
	    		validActions.put(0, smileAction);
	    	}
	    }
	    
	    void chooseForesterBase(Set<ReadonlyAction> actions, ReadonlyCharacter character,HashMap<Integer, ReadonlyAction> validActions,	HashMap<Integer, String> actionString)
	    {
	    	chooseBase(actions,character,validActions,actionString);
	    	
	    	for (ReadonlyAction action: actions)
	    	{
				int priority=-1;
				String message = "";
	        	Point2D chosenLocation=null;
	        	
	    		switch(action.getName())
	    		{
	    			case "Step":
	    				message = "Avoiding ";
	    				// simple enemy avoidance... chose location that is farthest away from closest enemy
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
	    				if (chosenLocation !=null)
	    				{
		    				priority=100;
		    				action.setLocation(chosenLocation);
	    				}
	    				break;
		        	case "Bear":
		        		message = "Bear Cooldown: "+action.getCooldown();
		        		action.setLocation(action.availableLocations().getFirst());
		        		priority=400;
		        		break;
	        	}
	        	
	        	// add the action to the collection of valid actions to perform
	        	if (priority >-1)
	        	{
	        		validActions.put(priority, action);
	        		actionString.put(priority, message);
	        	}
    		}
	    }
	
    	void chooseForesterVeterinarian(Set<ReadonlyAction> actions, ReadonlyCharacter character,HashMap<Integer, ReadonlyAction> validActions,	HashMap<Integer, String> actionString)
    	{
    		chooseForesterBase(actions, character, validActions, actionString);
    		
	        for (ReadonlyAction action: actions)
	        {
				String message = "";
				int priority=-1;
				switch (action.getName())
	        	{
					case "Restore":
						List<ReadonlyCharacter> bearTeam = team.stream().filter(member-> TeamMemberType.valueOf(member)==TeamMemberType.BEAR).collect(Collectors.toList());
						List<ReadonlyCharacter> foresterTeam = team.stream().filter(member->!bearTeam.contains(member)).collect(Collectors.toList());
						
						double totalBearTeamHealth =0;
						double maxBearTeamHealth =0;
						for (ReadonlyCharacter bear : bearTeam)
						{
							totalBearTeamHealth+=bear.getHealth();
							maxBearTeamHealth+=bear.getMaxHealth();
						}
						
						if (totalBearTeamHealth< maxBearTeamHealth/2 || foresterTeam.stream().filter(forester->forester.getHealth()<forester.getMaxHealth()-1).findAny().isPresent())
						{
							priority=800;
						}
						break;
	        	}
				
	        	// add the action to the collection of valid actions to perform
	        	if (priority >-1)
	        	{
	        		validActions.put(priority, action);
	        		actionString.put(priority, message);
	        	}
	        }
    	}
    	
    	void chooseForesterHerbalist(Set<ReadonlyAction> actions, ReadonlyCharacter character,HashMap<Integer, ReadonlyAction> validActions,	HashMap<Integer, String> actionString)
    	{
    		chooseForesterBase(actions, character, validActions, actionString);
    		
	        for (ReadonlyAction action: actions)
	        {
				String message = "";
				int priority=-1;
				switch (action.getName())
	        	{
					case "Poison":
						List<ReadonlyCharacter> poisonedEnemies = new ArrayList<>();
						List<ReadonlyCharacter> nonPoisonedEnemies = new ArrayList<>();
						for (ReadonlyCharacter enemy : action.availableTargets())
						{
							if (enemy.isPoisoned())
							{
								poisonedEnemies.add(enemy);
							}
							else
							{
								nonPoisonedEnemies.add(enemy);
							}
						}
						
						// try to choose poisoned non-bear enemies
						ReadonlyCharacter chosenTarget = poisonedEnemies.stream().filter(enemy->!TeamMemberType.isBear(enemy)).findFirst().orElse(null);
						
						// next try non-poisoned non-bear enemies
						if (chosenTarget==null) chosenTarget = nonPoisonedEnemies.stream().filter(enemy->!TeamMemberType.isBear(enemy)).sorted(new Comparator<ReadonlyCharacter>()
						{

							@Override
							public int compare(ReadonlyCharacter left, ReadonlyCharacter right)
							{
								return (int) (left.getHealth()-right.getHealth());
							}
						}).findFirst().orElse(null);
						
						//if we have a nice amount of mana to play with
						if (character.getMana()>character.getMaxMana()/2)
						{
							// next try already poisoned bears
							if (chosenTarget==null && poisonedEnemies.size() > 0) chosenTarget = poisonedEnemies.get(getRandom().nextInt(poisonedEnemies.size()));
							
							// lastly try to choose non-poisoned bears
							if (chosenTarget==null && nonPoisonedEnemies.size() > 0) chosenTarget = nonPoisonedEnemies.get(getRandom().nextInt(nonPoisonedEnemies.size()));
							
						}
						
						if (chosenTarget!=null)
						{
							action.setTarget(chosenTarget);
							priority = 350;
							message = "Target: "+System.identityHashCode(chosenTarget) + " poision damage: "+chosenTarget.getPoisonAmount();
						}
						
						break;
	        	}
				
	        	// add the action to the collection of valid actions to perform
	        	if (priority >-1)
	        	{
	        		validActions.put(priority, action);
	        		actionString.put(priority, message);
	        	}
	        }
    	}
    	
		void chooseBear(Set<ReadonlyAction> actions, ReadonlyCharacter character,HashMap<Integer, ReadonlyAction> validActions,	HashMap<Integer, String> actionString)
		{
	    	chooseBase(actions,character,validActions,actionString);
	    	
	        for (ReadonlyAction action: actions)
	        {
				String message = "";
	        	Point2D chosenLocation=null;
				int priority=-1;
				
	        	ReadonlyCharacter target;
				switch(action.getName())
	    		{
	    			case "Step":
	    				
//	    				if (!visibleEnemies.isEmpty())
//	    				{
//
//		    				// simple enemy attractor
//		    				double closestDistance = Double.MAX_VALUE;
//		    				
//		    				EnemyCharacter chosenTarget=null;
//							for ( Point2D enemyLocation : visibleEnemies.keySet())
//		    				{
//		        				for (Point2D location : action.availableLocations())
//		        				{
//		        					if (location.diagonalDistance(enemyLocation) < closestDistance)
//		        					{
//		        						closestDistance = location.diagonalDistance(enemyLocation);
//		        						chosenLocation = location;
//		        						chosenTarget = visibleEnemies.get(enemyLocation);
//		        					}
//		        				}
//		    				}
//		    				if (chosenLocation !=null)
//		    				{
//			    				priority=30;
//			    				action.setLocation(chosenLocation);
//		    					message = "Stalking Enemy: "+System.identityHashCode(chosenTarget);
//		    				}
//	    				}
//	    				else
	    				{
	        				// scout for an enemy
	    					
							priority = 10;
							message = "Scouting ";
							
							// dumb random location selection... not optimal but is sufficent.
							int index = getRandom().nextInt(action.availableLocations().size());
	        				for (Point2D location : action.availableLocations())
	        				{
	    						chosenLocation= location;
	        					if (--index == 0)
	        					{
	        						break;
	        					}
	        				}
		    				if (chosenLocation !=null)
		    				{
			    				action.setLocation(chosenLocation);
		    				}
	    				}
	    				
	    				break;
		        	case "Slice":
		        		target = action.availableTargets().getFirst();
		        		action.setTarget(target);
		        		message = "Slicing Target: "+System.identityHashCode(target);
		        		priority=1000;
		        		break;
	        	}
	        	
	        	// add the action to the collection of valid actions to perform
	        	if (priority >-1)
	        	{
	        		validActions.put(priority, action);
	        		actionString.put(priority, message);
	        	}
	        }
		}


		
	}