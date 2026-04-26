package game.engine.cards;

import game.engine.interfaces.CanisterModifier;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.*;

public class EnergyStealCard extends Card implements CanisterModifier {
	private int energy;

	public EnergyStealCard(String name, String description, int rarity, int energy) {
		super(name, description, rarity, true);
		this.energy = energy;
	}
	
	public int getEnergy() {
		return energy;
	}

	@Override
	public void performAction(Monster player, Monster opponent) {
		int toSteal = Math.min(energy, opponent.getEnergy());
		int InitialOpponent = opponent.getEnergy();
		opponent.alterEnergy(-toSteal);
		int finalOpponent = opponent.getEnergy();
		int actualStolen = InitialOpponent-finalOpponent;
		player.alterEnergy(actualStolen);
		// won't check the shielded value as the alterEnergy will do
	}

	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		monster.alterEnergy(-canisterValue + energy);
	}

}
