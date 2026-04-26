package game.engine.monsters;

import game.engine.Role;

public class Dasher extends Monster {
	private int momentumTurns;

	public Dasher(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
		this.momentumTurns = 0;
	}
	
	public int getMomentumTurns() {
		return momentumTurns;
	}
	
	public void setMomentumTurns(int momentumTurns) {
		this.momentumTurns = momentumTurns;
	}

	@Override
	public void move (int distance){
		if (this.momentumTurns > 0 ){
			super.move(distance*3);
			momentumTurns--;
		}
		else
			super.move(distance *2);
	}

	@Override
	public void executePowerupEffect(Monster opponentMonster) {
		this.setMomentumTurns(3);
	}


}