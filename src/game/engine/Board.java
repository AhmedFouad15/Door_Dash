package game.engine;

import java.util.ArrayList;
import java.util.Collections;

import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.monsters.Monster;

import static game.engine.dataloader.DataLoader.*;

public class Board {
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters; 
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards) {
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		originalCards = readCards;
		cards = new ArrayList<Card>();
		setCardsByRarity();
		reloadCards();
	}
	
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	
	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		Board.stationedMonsters = stationedMonsters;
	}

	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	
	public static ArrayList<Card> getCards() {
		return cards;
	}
	
	public static void setCards(ArrayList<Card> cards) {
		Board.cards = cards;
	}

	private int[] indexToRowCol(int index){
		int row = (int) (index / Constants.BOARD_COLS);
		int col;
		if(row % 2 == 0)
			col = (index % Constants.BOARD_COLS);
		else
			col = Constants.BOARD_COLS - 1 - (index % Constants.BOARD_COLS);
		int[] x = new int[]{row,col};
		return x;
	}

	private Cell getCell(int index){
		int[] data = indexToRowCol(index);
		return boardCells[data[0]][data[1]];
	}

	private void setCell(int index, Cell cell){
		int[] place = indexToRowCol(index);
		boardCells[place[0]][place[1]] = cell;
	}

	public void initializeBoard(ArrayList<Cell> specialCells){
		for (int i = 0; i < Constants.BOARD_SIZE; i++) {
			if (i % 2 == 0) {
				setCell(i, new Cell("Normal Cell"));
			}
		}
		int Contamination_index = 0;
		int conveyor_index = 0;
		int DoorCell_index = 1;
		int CardCell_index = 0;
		int monsterCell_index = 0;



		for(int i = 0; i < specialCells.size(); i++){
			Cell cell = specialCells.get(i);

			if(cell instanceof ContaminationSock && Contamination_index < Constants.SOCK_CELL_INDICES.length){
				int ind = Constants.SOCK_CELL_INDICES[Contamination_index];
				setCell(ind, cell);
				Contamination_index++;
			}
			else if(cell instanceof ConveyorBelt && conveyor_index < Constants.CONVEYOR_CELL_INDICES.length) {
				int ind = Constants.CONVEYOR_CELL_INDICES[conveyor_index];
				setCell(ind, cell);
				conveyor_index++;
			}
			else if(cell instanceof DoorCell){
				setCell(DoorCell_index, cell);
				DoorCell_index+=2;
			}
			else if(cell instanceof CardCell){
				int ind =  Constants.CARD_CELL_INDICES[CardCell_index];
				CardCell_index++;
				setCell(ind, cell);
			}
			else if( cell instanceof MonsterCell) {
				int ind = Constants.MONSTER_CELL_INDICES[monsterCell_index];
				setCell(ind, cell);

				Monster stationedMonster = ((MonsterCell) cell).getCellMonster();
				if (stationedMonster != null) {
					stationedMonster.setPosition(ind);

					// If the Game class hasn't already populated the stationedMonsters list, add it here:
					if (!stationedMonsters.contains(stationedMonster)) {
						stationedMonsters.add(stationedMonster);
					}
				}

				monsterCell_index++;
			}

		}
	}

	private void setCardsByRarity(){
		ArrayList<Card> old = getOriginalCards();
		ArrayList<Card> new_one = new ArrayList<>();
		for(int i = 0; i < old.size(); i++){
			int rare = old.get(i).getRarity();
			for(int j = 0; j < rare; j++){
				new_one.add(old.get(i));
			}
		}
		originalCards = new_one;
	}

	static void reloadCards(){
		ArrayList<Card> temp = new ArrayList<>(originalCards);
		Collections.shuffle(temp);
		cards = temp;
	}

	public static Card drawCard(){
		if(cards.size() == 0){
			reloadCards();
		}
		Card card = cards.get(0);
		cards.remove(0);
		return card;
	}

	void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException {
		int old_Position = currentMonster.getPosition();
		currentMonster.setPosition(roll + old_Position);

		Cell cell = getCell(currentMonster.getPosition());
		cell.onLand(currentMonster, opponentMonster);

		if(currentMonster.getPosition() == opponentMonster.getPosition()){
			currentMonster.setPosition(old_Position);
			throw new InvalidMoveException();
		}
		if (currentMonster.isConfused() || opponentMonster.isConfused()) {
			currentMonster.decrementConfusion();
			opponentMonster.decrementConfusion();
		}

		updateMonsterPositions(currentMonster, opponentMonster);
	}

	private void updateMonsterPositions(Monster player, Monster opponent){
		for(int i = 0; i < Constants.BOARD_SIZE; i++){
			Cell cell = getCell(i);
			if (cell != null) {
				cell.setMonster(null);
			}
		}

		if(player != null){
			int x = player.getPosition();
			if(x >= 0 && x < Constants.BOARD_SIZE) {
				Cell i = getCell(x);
				i.setMonster(player);
			}
		}
		if(opponent != null){
			int x = opponent.getPosition();
			if(x >= 0 && x < Constants.BOARD_SIZE) {
				Cell i = getCell(x);
				i.setMonster(opponent);
			}
		}
	}

}
