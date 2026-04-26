package game.engine.monsters;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;

public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}

	private int stealEnergyFrom(Monster target) {
		int targetEnergy = target.getEnergy();
		int amountToSteal;
		if (targetEnergy < Constants.SCHEMER_STEAL) {
			amountToSteal = targetEnergy;
		} else {
			amountToSteal = Constants.SCHEMER_STEAL;
		}
		target.setEnergy(targetEnergy - amountToSteal);
		return amountToSteal;
	}
	@Override
	public void setEnergy(int energy) {
		if (energy != this.getEnergy()) {
			super.setEnergy(energy + Constants.SCHEMER_STEAL);
		} else {
			super.setEnergy(energy);
		}
	}
	@Override
	public void executePowerupEffect(Monster opponentMonster) {
		int totalStolen = 0;

		//  Steal from the main opponent
		totalStolen += stealEnergyFrom(opponentMonster);

		//  Steal from everyone else on the board
		//and we have the access to them through the Board's list of stationed monsters
		for (Monster stationed : Board.getStationedMonsters()) {
			totalStolen += stealEnergyFrom(stationed);
		}

		//  Finally, add all the stolen energy from all the monsters to the schemer
		this.setEnergy(this.getEnergy() + totalStolen);
	}

}
