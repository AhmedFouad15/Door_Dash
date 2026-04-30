package game.engine;

import java.util.ArrayList;
import java.util.Collections;

import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.monsters.Monster;

import static game.engine.Constants.MONSTER_CELL_INDICES;
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
		int Contamination_index = 0;
		int conveyor_index = 0;
		int DoorCell_index = 1;


		for (int i = 0; i < stationedMonsters.size(); i++) {
			Monster currentMonster = stationedMonsters.get(i);
			int targetIndex = Constants.MONSTER_CELL_INDICES[i];

			currentMonster.setPosition(targetIndex);

			setCell(targetIndex, new MonsterCell(currentMonster.getName(), currentMonster));
		}

		for (int i = 0; i < Constants.CARD_CELL_INDICES.length; i++) {
			setCell(Constants.CARD_CELL_INDICES[i], new CardCell("Card Cell"));
		}


		for(int i = 0; i < specialCells.size(); i++){

			Cell cell = specialCells.get(i);

			if(cell instanceof ContaminationSock && Contamination_index < Constants.SOCK_CELL_INDICES.length){
				ContaminationSock x = (ContaminationSock) cell;
				int ind = Constants.SOCK_CELL_INDICES[Contamination_index];
				setCell(ind, x);
				Contamination_index++;
			}
			else if(cell instanceof ConveyorBelt && conveyor_index < Constants.CONVEYOR_CELL_INDICES.length) {
				ConveyorBelt x = (ConveyorBelt) cell;
				int ind = Constants.CONVEYOR_CELL_INDICES[conveyor_index];
				setCell(ind, x);
				conveyor_index++;
			}
			else if(cell instanceof DoorCell){
				DoorCell x = (DoorCell) cell;
				setCell(DoorCell_index, x);
				DoorCell_index+=2;
			}
		}

		for (int i = 0; i < Constants.BOARD_SIZE; i++) {
			if (getCell(i) == null) {
				setCell(i, new Cell("Normal Cell"));
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

	public static void reloadCards(){
		ArrayList<Card> temp = new ArrayList<>(originalCards);
		Collections.shuffle(temp);
		cards = temp;
	}

	public static Card drawCard(){
		if(cards.isEmpty()){
			reloadCards();
		}
		Card card = cards.get(0);
		cards.remove(0);
		return card;
	}

	public void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException {
		int old_Position = currentMonster.getPosition();

		int new_Position = (old_Position + roll) % Constants.BOARD_SIZE;
		currentMonster.setPosition(new_Position);

		Cell cell = getCell(currentMonster.getPosition());
		if (cell != null) {
			cell.onLand(currentMonster, opponentMonster);
		}

		if(currentMonster.getPosition() == opponentMonster.getPosition()){
			currentMonster.setPosition(old_Position);
			throw new InvalidMoveException();
		}

		if (currentMonster.isConfused()) {
			currentMonster.decrementConfusion();
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