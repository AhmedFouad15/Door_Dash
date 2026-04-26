package game.engine.monsters;

import game.engine.Role;

public class Dynamo extends Monster {
	
	public Dynamo(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}

	@Override
	public void setEnergy(int energy) {
		int currentEnergy = this.getEnergy();
		int difference = energy - currentEnergy;
		super.setEnergy(currentEnergy + (2*difference));
	}

	@Override
	public void executePowerupEffect(Monster opponentMonster) {
		opponentMonster.setFrozen(true);
	}


}
