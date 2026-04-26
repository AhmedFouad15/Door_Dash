package game.engine.cards;

import game.engine.monsters.*;

public class StartOverCard extends Card {

	public StartOverCard(String name, String description, int rarity, boolean lucky) {
		super(name, description, rarity, lucky);
	}

	//law el lucky is true fa el opponent will go back to position 0, w el3aks
	public void performAction(Monster player, Monster opponent) {
		if (this.isLucky()) {
			player.setPosition(0);
		}
		else
		{
			opponent.setPosition(0);
		}
	}

}
