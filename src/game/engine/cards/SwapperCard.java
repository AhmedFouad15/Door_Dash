package game.engine.cards;

import game.engine.monsters.*;

public class SwapperCard extends Card {

	public SwapperCard(String name, String description, int rarity) {
		super(name, description, rarity, true);
	}

	public void performAction(Monster player, Monster opponent){
		int PlayerPosition = player.getPosition();
		int OpponentPosition = opponent.getPosition();

		if (PlayerPosition < OpponentPosition ) {
			int temp = PlayerPosition;
			player.setPosition(OpponentPosition);
			opponent.setPosition(temp);
		}
	}
	
}
