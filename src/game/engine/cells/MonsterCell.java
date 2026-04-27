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
				int temp = landingMonster.getEnergy();
				landingMonster.setEnergy(cellMonster.getEnergy());
				cellMonster.setEnergy(temp);
			}
		}}}