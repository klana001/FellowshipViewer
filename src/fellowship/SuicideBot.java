package fellowship;

import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Flexible;
import fellowship.abilities.attacking.Ranged;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.attacking.Quick;
import fellowship.actions.damage.KO;
import fellowship.actions.damage.Zap;
import fellowship.actions.mobility.Blink;
import fellowship.actions.mobility.Swap;
import fellowship.actions.mobility.Teleport;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.set.MutableSet;

import com.nmerrill.kothcomm.game.maps.Point2D;

public class SuicideBot extends Player{

    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(10, 5, 5,
                    new ActionAbility(Quick::new),
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
        ReadonlyAction smile = null;
        for (ReadonlyAction action: actions){
//        	if (action .getName().equals("Swap"))
//			{
//        		MutableSet<ReadonlyCharacter> targets = action.availableTargets();
//        		action.setTarget(targets.getFirst());
//        		chosen = action;
//			}
        	
        	if (action .getName().equals("Smile"))
        			{
        		smile = action;
        			}
//        	if (action .getName().equals("Blink"))
//			{
//        		if (chosen == null && Math.random()>0.5)
//        		{
//        		MutableSet<Point2D> targets = action.availableLocations();
//        		action.setLocation(targets.getFirst());
//        		chosen = action;
//        		}
//			}
        	
        	if (action .getName().equals("Teleport"))
			{
        		if (chosen == null && Math.random()>0.5)
        		{
        		MutableSet<Point2D> targets = action.availableLocations();
        		int i=getRandom().nextInt(targets.size()-1);
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
        	chosen = smile;
        }
        return chosen;
    }

}
