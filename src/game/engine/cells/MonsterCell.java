package game.engine.cells;

import game.engine.monsters.*;

public class MonsterCell extends Cell {
	private Monster cellMonster;

	public MonsterCell(String name, Monster cellMonster) {
		super(name);
		this.cellMonster = cellMonster;
	}

	public Monster getCellMonster() {
		return cellMonster;
	}

	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);
		if (landingMonster.getRole() == cellMonster.getRole()) {
			landingMonster.executePowerupEffect(opponentMonster);
		} else {
			if (landingMonster.getEnergy() > cellMonster.getEnergy()) {
				int landingInitial = landingMonster.getEnergy();
				int cellInitial = cellMonster.getEnergy();

				// Calculate the penalty (this will be a negative number)
				int penalty = cellInitial - landingInitial;

				// Apply the penalty using alterEnergy so the shield can block it
				landingMonster.alterEnergy(penalty);

				// The cell monster ALWAYS gets the high energy
				cellMonster.setEnergy(landingInitial);
			}
		}
	}
}