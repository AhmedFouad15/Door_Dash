package game.engine.cards;

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
		// The base amount to steal is bounded by the opponent's current energy
		int toSteal = Math.min(this.energy, opponent.getEnergy());

		// We must check the shield BEFORE we attack them, because alterEnergy removes the shield!
		boolean wasShielded = opponent.isShielded();

		// Apply the attack to the opponent (this triggers their Dynamo/Schemer passives)
		this.modifyCanisterEnergy(opponent, -toSteal);

		// If they were not shielded, the steal is successful!
		// Give the player the base stolen amount.
		if (!wasShielded && toSteal > 0) {
			this.modifyCanisterEnergy(player, toSteal);
		}
	}

	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		monster.alterEnergy(canisterValue);
	}
}