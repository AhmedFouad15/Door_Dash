package game.engine;

import java.io.IOException;

import game.engine.cards.Card;
import game.engine.exceptions.*;
import java.util.ArrayList;
import java.util.Collections;
import game.engine.dataloader.DataLoader;
import game.engine.monsters.*;
import game.engine.Board.*;
import game.engine.cells.*;

import static game.engine.Board.setStationedMonsters;
import static game.engine.dataloader.DataLoader.readCells;

public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters; 
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	public Game(Role playerRole) throws IOException {
		this.board = new Board(DataLoader.readCards());
		
		this.allMonsters = DataLoader.readMonsters();
		
		this.player = selectRandomMonsterByRole(playerRole);
		this.opponent = selectRandomMonsterByRole(playerRole == Role.SCARER ? Role.LAUGHER : Role.SCARER);
		this.current = player;

		allMonsters.remove(player);
		allMonsters.remove(opponent);

		setStationedMonsters(allMonsters);

		ArrayList<Card> x = DataLoader.readCards();

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
		Collections.shuffle(allMonsters);
	    return allMonsters.stream()
	    		.filter(m -> m.getRole() == role)
	    		.findFirst()
	    		.orElse(null);
	}

	private Monster getCurrentOpponent() {
		if (current == player)
			return opponent;
		else
			return player;
	}

	private int rollDice() {
		return (int)(Math.random()*6) + 1;
	}

	void usePowerup() throws OutOfEnergyException{
		if (current.getEnergy()<Constants.POWERUP_COST) {
			throw new OutOfEnergyException();
		}
		Monster opponent = getCurrentOpponent();
		current.executePowerupEffect(opponent);
		current.setEnergy(current.getEnergy() - Constants.POWERUP_COST);
	}

	void playTurn() throws InvalidMoveException{
		if (current.isFrozen()) { // frozen is true
			current.setFrozen(false);
			switchTurn();
			return;
		}
		int roll = rollDice();
		Monster opponent = getCurrentOpponent();
		board.moveMonster(current, roll, opponent);
		switchTurn();
	}

	private void switchTurn() {
		if (current == player)
			current = opponent;
		else
			current = player;
	}

	private boolean checkWinCondition(Monster monster) {
		if(monster.getPosition()==Constants.WINNING_POSITION && monster.getEnergy()>=Constants.WINNING_ENERGY)
			return true;
		return false;
	}

	public Monster getWinner() {
		if (checkWinCondition(player))
			return player;

		if (checkWinCondition(opponent))
			return opponent;

		return null;
	}

}