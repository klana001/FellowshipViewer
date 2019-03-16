package fellowship;


import fellowship.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.nmerrill.kothcomm.game.maps.Point2D;

import fellowship.abilities.defensive.Spikes;
import fellowship.abilities.stats.Regenerate;
import fellowship.abilities.stats.Strong;
import fellowship.actions.ReadonlyAction;
import fellowship.characters.CharacterTemplate;
import fellowship.characters.ReadonlyCharacter;

public class Spiky extends Player {

    @Override
    public List<CharacterTemplate> createCharacters() {
        List<CharacterTemplate> templates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            templates.add(new CharacterTemplate(40, 0, 0,
                    new Strong(),
                    new Strong(),
                    new Regenerate(),
                    new Spikes()));
        }
        return templates;
    }

    @Override
    public ReadonlyAction choose(Set<ReadonlyAction> actions, ReadonlyCharacter character) {

        ReadonlyAction chosen = null;
        Boolean canSlice = false;
        for (ReadonlyAction action: actions) {
            if (action.getName().equals("Slice")) {
                canSlice = true;
            }
        }

        for (ReadonlyAction action: actions) {
             if (action.getName().equals("Slice")) {
                 chosen = action;
                 chosen.setTarget(action.availableTargets().minBy(ReadonlyCharacter::getHealth));
             }
             if (!canSlice && action.getName().equals("Step")){
                 int x = ThreadLocalRandom.current().nextInt(3, 6 + 1);
                 int y = ThreadLocalRandom.current().nextInt(3, 6 + 1);
                 chosen = action;
                 Point2D destination = null;
                 if (visibleEnemies.isEmpty()){
                     destination = action.availableLocations().minBy(p1 -> p1.cartesianDistance(new Point2D(x, y)));
                 } else {
                     destination = action.availableLocations().minBy(p1 -> p1.cartesianDistance(visibleEnemies.keysView().minBy(p1::cartesianDistance)));
                 }
                 chosen.setLocation(destination);
             }
        }

        return chosen;
    }
}