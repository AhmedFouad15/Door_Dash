package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;

public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters; 
	private Monster player;
	private Monster opponent;
	private Monster current;
	private Queue<Integer> scriptedRolls;
	private boolean deterministicSelection;
	
	public Game(Role playerRole) throws IOException {
		this(playerRole, false);
	}

	public Game(Role playerRole, boolean deterministicSelection) throws IOException {
		this.deterministicSelection = deterministicSelection;
		this.board = new Board(DataLoader.readCards());
		if (deterministicSelection) {
			Board.reloadCardsDeterministic();
		}
		
		this.allMonsters = DataLoader.readMonsters();
		
		this.player = selectRandomMonsterByRole(playerRole);
		this.opponent = selectRandomMonsterByRole(playerRole == Role.SCARER ? Role.LAUGHER : Role.SCARER);
		this.current = player;
		
		allMonsters.remove(player);
		allMonsters.remove(opponent);
		
		Board.setStationedMonsters(allMonsters);
		board.initializeBoard(DataLoader.readCells());
	}
	
	public Board getBoard() {
		return board;
	}
	
	public ArrayList<Monster> getAllMonsters() {
		return allMonsters; 
	}
	
	public Monster getPlayer() {
		return player;
	}
	
	public Monster getOpponent() {
		return opponent;
	}
	
	public Monster getCurrent() {
		return current;
	}
	
	public void setCurrent(Monster current) {
		this.current = current;
	}
	
	private Monster selectRandomMonsterByRole(Role role) {
		if (deterministicSelection) {
			for (Monster monster : allMonsters) {
				if (monster.getRole() == role && role == Role.SCARER && monster instanceof Schemer) {
					return monster;
				}
			}
			for (Monster monster : allMonsters) {
				if (monster.getRole() == role && monster instanceof Dynamo) {
					return monster;
				}
			}
			for (Monster monster : allMonsters) {
				if (monster.getRole() == role) {
					return monster;
				}
			}
		}

		Collections.shuffle(allMonsters);
	    return allMonsters.stream()
	    		.filter(m -> m.getRole() == role)
	    		.findFirst()
	    		.orElse(null);
	}
	
	private Monster getCurrentOpponent() {
		return current == player ? opponent : player;
	}

	private int rollDice() {
		if (scriptedRolls != null && !scriptedRolls.isEmpty()) {
			return scriptedRolls.poll();
		}
		Random rand = new Random();
		return rand.nextInt(6) + 1;
	}

	public void setScriptedRolls(int... rolls) {
		scriptedRolls = new LinkedList<>();
		for (int roll : rolls) {
			if (roll >= 1 && roll <= 6) {
				scriptedRolls.add(roll);
			}
		}
	}

	public boolean hasScriptedRolls() {
		return scriptedRolls != null && !scriptedRolls.isEmpty();
	}

	public void clearScriptedRolls() {
		if (scriptedRolls != null) {
			scriptedRolls.clear();
		}
	}
	
	public void usePowerup() throws OutOfEnergyException {
		if (current.getEnergy() < Constants.POWERUP_COST)
			throw new OutOfEnergyException("Not enough energy to use powerup");
		
		current.executePowerupEffect(getCurrentOpponent());
		current.setEnergy(current.getEnergy() - Constants.POWERUP_COST);
	}
	
	public void playTurn() throws InvalidMoveException {
		if (current.isFrozen()) {
			System.out.println(current.getName() + " is frozen! Turn skipped.");
			current.setFrozen(false);
			switchTurn();
			return;
		}
		
		int roll = rollDice();
		
		board.moveMonster(current, roll, getCurrentOpponent());
		
		switchTurn();
	}
	
	private void switchTurn() {
		this.setCurrent(getCurrentOpponent());
	}
	
	private boolean checkWinCondition(Monster monster) {
		return monster.getPosition() == Constants.WINNING_POSITION && 
		       monster.getEnergy() >= Constants.WINNING_ENERGY;
	}
	
	public Monster getWinner() {
		if (checkWinCondition(player)) 
			return player;
		
		if (checkWinCondition(opponent)) 
			return opponent;
		
		return null;
	}
	
}
