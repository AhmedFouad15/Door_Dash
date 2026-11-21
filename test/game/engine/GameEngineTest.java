package game.engine;

import game.engine.cards.*;
import game.engine.cells.*;
import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    @Test
    void dataLoaderLoadsExpectedCsvCounts() throws IOException {
        ArrayList<Card> cards = DataLoader.readCards();
        ArrayList<Cell> cells = DataLoader.readCells();
        ArrayList<Monster> monsters = DataLoader.readMonsters();

        assertEquals(8, cards.size());
        assertEquals(60, cells.size());
        assertEquals(8, monsters.size());

        long doorCount = cells.stream().filter(c -> c instanceof DoorCell).count();
        long conveyorCount = cells.stream().filter(c -> c instanceof ConveyorBelt).count();
        long sockCount = cells.stream().filter(c -> c instanceof ContaminationSock).count();

        assertEquals(50, doorCount);
        assertEquals(5, conveyorCount);
        assertEquals(5, sockCount);
    }

    @Test
    void gameInitializationPopulatesBoardAndTeams() throws IOException {
        Game game = new Game(Role.SCARER, true);

        assertNotNull(game.getPlayer());
        assertNotNull(game.getOpponent());
        assertEquals(Role.SCARER, game.getPlayer().getRole());
        assertEquals(Role.LAUGHER, game.getOpponent().getRole());
        assertSame(game.getPlayer(), game.getCurrent());
        assertEquals(6, Board.getStationedMonsters().size());
        assertEquals(25, Board.getOriginalCards().size());
        assertEquals(25, Board.getCards().size());

        Cell[][] grid = game.getBoard().getBoardCells();
        assertEquals(Constants.BOARD_ROWS, grid.length);
        assertEquals(Constants.BOARD_COLS, grid[0].length);

        for (int idx : Constants.CARD_CELL_INDICES) {
            assertInstanceOf(CardCell.class, cellAt(grid, idx));
        }
        for (int idx : Constants.CONVEYOR_CELL_INDICES) {
            assertInstanceOf(ConveyorBelt.class, cellAt(grid, idx));
        }
        for (int idx : Constants.SOCK_CELL_INDICES) {
            assertInstanceOf(ContaminationSock.class, cellAt(grid, idx));
        }
        for (int idx : Constants.MONSTER_CELL_INDICES) {
            assertInstanceOf(MonsterCell.class, cellAt(grid, idx));
        }
    }

    @Test
    void winConditionRequiresBothWinningPositionAndEnergy() throws IOException {
        Game game = new Game(Role.SCARER, true);
        Monster player = game.getPlayer();

        player.setPosition(Constants.WINNING_POSITION);
        player.setEnergy(Constants.WINNING_ENERGY - 100);
        assertTrue(player.getEnergy() < Constants.WINNING_ENERGY);
        assertNull(game.getWinner());

        player.setPosition(Constants.WINNING_POSITION - 1);
        player.setEnergy(Constants.WINNING_ENERGY);
        assertNull(game.getWinner());

        player.setPosition(Constants.WINNING_POSITION);
        player.setEnergy(Constants.WINNING_ENERGY);
        assertSame(player, game.getWinner());
    }

    @Test
    void powerupRequiresMinimumEnergyAndFreezesOpponentForDynamo() throws IOException, OutOfEnergyException, InvalidMoveException {
        Game game = new Game(Role.LAUGHER, true);
        assertInstanceOf(Dynamo.class, game.getPlayer());

        game.getPlayer().setEnergy(200);
        assertTrue(game.getPlayer().getEnergy() < Constants.POWERUP_COST);
        assertThrows(OutOfEnergyException.class, game::usePowerup);

        game.getPlayer().setEnergy(450);
        assertTrue(game.getPlayer().getEnergy() >= Constants.POWERUP_COST);
        game.usePowerup();

        assertTrue(game.getOpponent().isFrozen());

        game.setCurrent(game.getOpponent());
        int opponentPosBefore = game.getOpponent().getPosition();
        game.playTurn();

        assertFalse(game.getOpponent().isFrozen());
        assertEquals(opponentPosBefore, game.getOpponent().getPosition());
        assertSame(game.getPlayer(), game.getCurrent());
    }

    @Test
    void dasherAndMultiTaskerMovementModifiersApplyCorrectly() {
        Dasher dasher = new Dasher("Mike", "Fast", Role.LAUGHER, 600);
        dasher.move(3);
        assertEquals(6, dasher.getPosition());

        dasher.executePowerupEffect(null);
        dasher.move(3);
        assertEquals(15, dasher.getPosition());
        assertEquals(2, dasher.getMomentumTurns());

        MultiTasker multiTasker = new MultiTasker("Celia", "Organized", Role.LAUGHER, 100);
        multiTasker.move(5);
        assertEquals(2, multiTasker.getPosition());

        multiTasker.executePowerupEffect(null);
        multiTasker.move(5);
        assertEquals(7, multiTasker.getPosition());
        assertEquals(1, multiTasker.getNormalSpeedTurns());

        multiTasker.setEnergy(200);
        assertEquals(200 + Constants.MULTITASKER_BONUS, multiTasker.getEnergy());
    }

    @Test
    void cannotLandOnOpponentCell() throws IOException {
        Game game = new Game(Role.SCARER, true);
        Monster player = game.getPlayer();
        Monster opponent = game.getOpponent();

        player.setPosition(0);
        opponent.setPosition(3);

        assertThrows(InvalidMoveException.class, () -> game.getBoard().moveMonster(player, 3, opponent));
        assertEquals(0, player.getPosition());
    }

    @Test
    void cardEffectsWorkAsDesigned() {
        Monster player = new Dasher("P1", "Test", Role.SCARER, 200);
        Monster opponent = new Schemer("P2", "Test", Role.LAUGHER, 300);

        ShieldCard shieldCard = new ShieldCard("Super Shield", "Block", 1);
        shieldCard.performAction(player, opponent);
        assertTrue(player.isShielded());

        player.alterEnergy(-100);
        assertEquals(200, player.getEnergy());
        assertFalse(player.isShielded());

        ConfusionCard confusionCard = new ConfusionCard("Mind Scramble", "Confuse", 1, 2);
        confusionCard.performAction(player, opponent);
        assertEquals(Role.LAUGHER, player.getRole());
        assertEquals(Role.SCARER, opponent.getRole());
        assertEquals(2, player.getConfusionTurns());

        player.decrementConfusion();
        player.decrementConfusion();
        assertEquals(Role.SCARER, player.getRole());
        assertFalse(player.isConfused());

        player.setPosition(10);
        opponent.setPosition(45);
        SwapperCard swapper = new SwapperCard("Position Swap", "Swap", 1);
        swapper.performAction(player, opponent);
        assertEquals(45, player.getPosition());
        assertEquals(10, opponent.getPosition());

        StartOverCard startOverOpponent = new StartOverCard("2319 Alert", "Reset opponent", 1, true);
        startOverOpponent.performAction(player, opponent);
        assertEquals(Constants.STARTING_POSITION, opponent.getPosition());
    }

    private Cell cellAt(Cell[][] grid, int index) {
        int row = index / Constants.BOARD_COLS;
        int col = index % Constants.BOARD_COLS;
        if (row % 2 == 1) {
            col = Constants.BOARD_COLS - 1 - col;
        }
        return grid[row][col];
    }
}
