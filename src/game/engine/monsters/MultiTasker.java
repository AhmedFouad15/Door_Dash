package game.engine.monsters;

import game.engine.Constants;
import game.engine.Role;

public class MultiTasker extends Monster {
	private int normalSpeedTurns;

	public MultiTasker(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
		this.normalSpeedTurns = 0;
	}

	public int getNormalSpeedTurns() { return normalSpeedTurns; }
	public void setNormalSpeedTurns(int normalSpeedTurns) { this.normalSpeedTurns = normalSpeedTurns; }

	@Override
	public void move(int distance) {
		if (this.normalSpeedTurns > 0) {
			super.move(distance);
			normalSpeedTurns--;
		} else {
			super.move(distance / 2);
		}
	}

	@Override
	public void setEnergy(int energy) {
		// Blindly add the +200 bonus to whatever the new energy target is!
		super.setEnergy(energy + Constants.MULTITASKER_BONUS);
	}

	@Override
	public void executePowerupEffect(Monster opponentMonster) {
		this.setNormalSpeedTurns(2);
	}
}