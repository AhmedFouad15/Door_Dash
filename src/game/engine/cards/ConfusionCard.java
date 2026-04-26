package game.engine.cards;

import game.engine.Role;
import game.engine.monsters.*;

public class ConfusionCard extends Card {
	private int duration;
	
	public ConfusionCard(String name, String description, int rarity, int duration) {
		super(name, description, rarity, false);
		this.duration = duration;
	}
	
	public int getDuration() {
		return duration;
	}

	//monsters temporarily switch their roles, they switch turns for "duration" times
	@Override
	public void performAction(Monster player, Monster opponent) {
		Role temp = player.getRole();
		player.setRole(opponent.getRole());
		opponent.setRole(temp);

		player.setConfusionTurns(this.getDuration());
		opponent.setConfusionTurns(this.getDuration());
	}

}
