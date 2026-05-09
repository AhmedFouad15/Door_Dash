package MainMenu;

import game.engine.Board;
import game.engine.cells.Cell;
import game.engine.monsters.Monster;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import java.util.HashMap;
import java.util.Map;

public class BoardRenderer {

    private GridPane boardGrid;
    private Map<Integer, StackPane> cellNodes;
    private int lastPlayerPos = -1;
    private int lastOpponentPos = -1;

    public BoardRenderer(GridPane boardGrid) {
        this.boardGrid = boardGrid;
        this.cellNodes = new HashMap<>();
    }

    public void renderInitialBoard(Board backendBoard) {
        boardGrid.getChildren().clear();
        cellNodes.clear();

        // 1. Get the 2D array directly from your backend
        Cell[][] boardCells = backendBoard.getBoardCells();

        for (int i = 0; i < 100; i++) {
            // 2. BACKEND MAPPING: Calculate row/col exactly like Board.java indexToRowCol
            int row = i / 10;
            int col = i % 10;
            if (row % 2 == 1) {
                col = 9 - col; // Reversing for odd rows (Snake Pattern)
            }

            // 3. RETRIEVE CELL: Get the actual DoorCell, CardCell, etc.
            Cell cellBackend = boardCells[row][col];

            // 4. CREATE VISUAL: Pass the backend cell to the factory
            StackPane visualCell = CellFactory.createVisualCell(i, cellBackend);
            cellNodes.put(i, visualCell);

            // 5. GUI PLACEMENT: In GridPane, Row 0 is top, Row 9 is bottom.
            // We want Row 0 of the backend (0-9) to be at the bottom (Row 9) of the screen.
            int guiRow = 9 - row;
            boardGrid.add(visualCell, col, guiRow);
        }
    }

    public StackPane getCellVisual(int index) {
        return cellNodes.get(index);
    }

    public void updateMonsterPositions(Monster player, Monster opponent, Monster currentActive) {
        StackPane newPlayerVisual = MonsterRenderer.createMonsterVisual(player, player == currentActive);
        StackPane newOpponentVisual = MonsterRenderer.createMonsterVisual(opponent, opponent == currentActive);

        handleMonsterPlacement(player, newPlayerVisual, lastPlayerPos, true);
        lastPlayerPos = player.getPosition();

        handleMonsterPlacement(opponent, newOpponentVisual, lastOpponentPos, false);
        lastOpponentPos = opponent.getPosition();
    }

    private void handleMonsterPlacement(Monster monster, StackPane newVisual, int lastPos, boolean isPlayer) {
        String tag = isPlayer ? "player_visual" : "opponent_visual";
        newVisual.setId(tag);

        StackPane targetCell = getCellVisual(monster.getPosition());

        if (lastPos == -1 || lastPos == monster.getPosition()) {
            targetCell.getChildren().removeIf(n -> tag.equals(n.getId()));
            targetCell.getChildren().add(newVisual);
        } else {
            StackPane oldCell = getCellVisual(lastPos);
            oldCell.getChildren().removeIf(n -> tag.equals(n.getId()));
            oldCell.getChildren().add(newVisual);

            AnimationManager.animateMonsterMove(newVisual, oldCell, targetCell, () -> {
                oldCell.getChildren().remove(newVisual);
                targetCell.getChildren().removeIf(n -> tag.equals(n.getId()));
                targetCell.getChildren().add(newVisual);
            });
        }
    }
}