package game.engine.monsters;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;

public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}

	private int stealEnergyFrom(Monster target) {

		if (target.isShielded()) {
			target.setShielded(false);   // consume shield
			return 0;                   // no energy stolen
		}

		int targetEnergy = target.getEnergy();
		int amountToSteal = Math.min(targetEnergy, Constants.SCHEMER_STEAL);

		target.setEnergy(targetEnergy - amountToSteal);

		return amountToSteal;
	}

	@Override
	public void setEnergy(int energy) {
		super.setEnergy(energy + Constants.SCHEMER_STEAL);
	}
	@Override
	public void executePowerupEffect(Monster opponentMonster) {
		int totalStolen = 0;

		totalStolen += stealEnergyFrom(opponentMonster);

		for (Monster stationed : Board.getStationedMonsters()) {
			if (stationed != opponentMonster) {
				totalStolen += stealEnergyFrom(stationed);
			}
		}

		this.setEnergy(this.getEnergy() + totalStolen);
	}

}
