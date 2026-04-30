package game.engine.cells;

import game.engine.Role;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class DoorCell extends Cell implements CanisterModifier {
	private Role role;
	private int energy;
	private boolean activated;

	public DoorCell(String name, Role role, int energy) {
		super(name);
		this.role = role;
		this.energy = energy;
		this.activated = false;
	}

	public Role getRole() { return role; }
	public int getEnergy() { return energy; }
	public boolean isActivated() { return activated; }
	public void setActivated(boolean isActivated) { this.activated = isActivated; }

	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		if(monster.getRole() == getRole()) {
			monster.alterEnergy(canisterValue);
		} else {
			monster.alterEnergy(-canisterValue);
		}
	}

	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);
		if (!activated) {
			// Check if the landing monster's shield will protect the team
			boolean mismatch = (landingMonster.getRole() != this.role);
			boolean isShielded = landingMonster.isShielded();
			boolean teamProtected = mismatch && isShielded;

			boolean energyChanged = false;

			// 1. Apply to the landing monster
			int initialEnergy = landingMonster.getEnergy();
			modifyCanisterEnergy(landingMonster, energy);
			if (landingMonster.getEnergy() != initialEnergy) {
				energyChanged = true;
			}

			// 2. Apply to stationed monsters
			java.util.ArrayList<Monster> stationedList = game.engine.Board.getStationedMonsters();
			if (stationedList != null) {
				for (Monster stationed : stationedList) {
					// Prevent penalizing the landing monster twice, and check team shield
					if (stationed != landingMonster && stationed.getRole() == landingMonster.getRole()) {
						if (!teamProtected) {
							int initialStationed = stationed.getEnergy();
							modifyCanisterEnergy(stationed, energy);
							if (stationed.getEnergy() != initialStationed) {
								energyChanged = true;
							}
						}
					}
				}
			}

			if (energyChanged) {
				activated = true;
			}
		}
	}
}