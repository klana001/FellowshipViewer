package fellowship;

import fellowship.abilities.ActionAbility;
import fellowship.abilities.attacking.Flexible;
import fellowship.abilities.attacking.Ranged;
import fellowship.actions.ReadonlyAction;
import fellowship.actions.damage.KO;
import fellowship.actions.damage.Zap;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlinkingBot extends Player{

    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(10, 5, 5,
                    new Ranged(),
                    new Flexible(),
                    new ActionAbility(KO::new),
                    new ActionAbility(Zap::new)));
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {
        int minPriority = Integer.MAX_VALUE;
        ReadonlyAction chosen = null;
        for (ReadonlyAction action: actions){
        	
        	if (action .getName().equals("Smile"))
        			{
        		return action;
        			}
        
        }

        return null;
    }

}
