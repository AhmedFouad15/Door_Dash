package game.engine.monsters;

import java.util.ArrayList;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;

public class Schemer extends Monster{
	public Schemer(String name, String description, Role role, int energy){
		super(name, description, role, energy);
	}

//new

	private int stealEnergyFrom(Monster target) {
		int value;
		if (target.getEnergy() < Constants.SCHEMER_STEAL)
			value = target.getEnergy();
		else
			value = Constants.SCHEMER_STEAL;
		target.setEnergy(target.getEnergy() - value);
		return value;
	}

	public void executePowerupEffect(Monster opponentMonster) {
		int total = stealEnergyFrom(opponentMonster);

		ArrayList<Monster> stationed = Board.getStationedMonsters();
		for (int i =0; i<stationed.size(); i++) {
			Monster m = stationed.get(i);
			total += stealEnergyFrom(m);
		}

		setEnergy(getEnergy() + total);
	}

	@Override
	public void setEnergy(int energy) {
		int currentEnergy = this.getEnergy();
		int difference = energy - currentEnergy;

		if (difference == 0) {
			return;
		}

		super.setEnergy(energy + Constants.SCHEMER_STEAL);
	}

}