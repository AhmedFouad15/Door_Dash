package MainMenu;

import game.engine.Board;
import game.engine.cells.Cell;
import game.engine.monsters.Monster;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

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

    public void refreshBoard(Board backendBoard, Monster player, Monster opponent, Monster currentActive) {
        renderInitialBoard(backendBoard);
        lastPlayerPos = player.getPosition();
        lastOpponentPos = opponent.getPosition();

        StackPane playerCell = getCellVisual(player.getPosition());
        StackPane opponentCell = getCellVisual(opponent.getPosition());

        if (playerCell != null) {
            playerCell.getChildren().removeIf(n -> "player_visual".equals(n.getId()));
            StackPane playerVisual = MonsterRenderer.createMonsterVisual(player, player == currentActive);
            playerVisual.setId("player_visual");
            playerCell.getChildren().add(playerVisual);
        }

        if (opponentCell != null) {
            opponentCell.getChildren().removeIf(n -> "opponent_visual".equals(n.getId()));
            StackPane opponentVisual = MonsterRenderer.createMonsterVisual(opponent, opponent == currentActive);
            opponentVisual.setId("opponent_visual");
            opponentCell.getChildren().add(opponentVisual);
        }
    }

    public StackPane getCellVisual(int index) {
        return cellNodes.get(index);
    }

    public void updateMonsterPositions(Monster player, Monster opponent, Monster currentActive, Runnable onComplete) {
        updateMonsterPositions(player, opponent, currentActive, true, null, onComplete);
    }

    public void updateMonsterPositions(Monster player, Monster opponent, Monster currentActive,
                                       boolean followPlayer, IntConsumer cameraFollower, Runnable onComplete) {
        StackPane newPlayerVisual = MonsterRenderer.createMonsterVisual(player, player == currentActive);
        StackPane newOpponentVisual = MonsterRenderer.createMonsterVisual(opponent, opponent == currentActive);

        // We use an array to count when BOTH player and opponent updates are finished
        int[] completed = {0};
        Runnable checkFinished = () -> {
            completed[0]++;
            if (completed[0] == 2 && onComplete != null) {
                onComplete.run();
            }
        };

        handleMonsterPlacement(player, newPlayerVisual, lastPlayerPos, true, followPlayer ? cameraFollower : null, checkFinished);
        lastPlayerPos = player.getPosition();

        handleMonsterPlacement(opponent, newOpponentVisual, lastOpponentPos, false, followPlayer ? null : cameraFollower, checkFinished);
        lastOpponentPos = opponent.getPosition();
    }

    public void updateMonsterPositionsWithDirectGlide(Monster player, Monster opponent, Monster currentActive, boolean glidePlayer, Runnable onComplete) {
        updateMonsterPositionsWithDirectGlide(player, opponent, currentActive, glidePlayer, null, onComplete);
    }

    public void updateMonsterPositionsWithDirectGlide(Monster player, Monster opponent, Monster currentActive,
                                                      boolean glidePlayer, IntConsumer cameraFollower, Runnable onComplete) {
        StackPane newPlayerVisual = MonsterRenderer.createMonsterVisual(player, player == currentActive);
        StackPane newOpponentVisual = MonsterRenderer.createMonsterVisual(opponent, opponent == currentActive);

        int[] completed = {0};
        Runnable checkFinished = () -> {
            completed[0]++;
            if (completed[0] == 2 && onComplete != null) {
                onComplete.run();
            }
        };

        if (glidePlayer) {
            handleMonsterDirectGlide(player, newPlayerVisual, lastPlayerPos, true, cameraFollower, checkFinished);
        } else {
            handleMonsterPlacement(player, newPlayerVisual, lastPlayerPos, true, null, checkFinished);
        }
        lastPlayerPos = player.getPosition();

        if (glidePlayer) {
            handleMonsterPlacement(opponent, newOpponentVisual, lastOpponentPos, false, null, checkFinished);
        } else {
            handleMonsterDirectGlide(opponent, newOpponentVisual, lastOpponentPos, false, cameraFollower, checkFinished);
        }
        lastOpponentPos = opponent.getPosition();
    }

    private void handleMonsterPlacement(Monster monster, StackPane newVisual, int lastPos, boolean isPlayer, Runnable onComplete) {
        handleMonsterPlacement(monster, newVisual, lastPos, isPlayer, null, onComplete);
    }

    private void handleMonsterPlacement(Monster monster, StackPane newVisual, int lastPos, boolean isPlayer,
                                        IntConsumer cameraFollower, Runnable onComplete) {
        String tag = isPlayer ? "player_visual" : "opponent_visual";
        newVisual.setId(tag);

        int targetPos = monster.getPosition();

        // Detect if they looped around the board (e.g., from 98 back to 2) or used a Start Over Card
        boolean isWrapAround = (lastPos > 80 && targetPos < 20);

        // If it's the start, didn't move, OR wrapped around -> teleport instantly without animation
        if (lastPos == -1 || lastPos == targetPos || isWrapAround) {
            StackPane targetCell = getCellVisual(targetPos);
            targetCell.getChildren().removeIf(n -> tag.equals(n.getId()));
            targetCell.getChildren().add(newVisual);
            if (onComplete != null) onComplete.run();
        } else {
            // Otherwise, do the smooth step-by-step walk
            moveStepByStep(newVisual, lastPos, targetPos, tag, cameraFollower, onComplete);
        }
    }

    private void moveStepByStep(StackPane visual, int currentStep, int endStep, String tag,
                                IntConsumer cameraFollower, Runnable onComplete) {
        // Base Case: We arrived at the destination!
        if (currentStep == endStep) {
            if (onComplete != null) onComplete.run();
            return;
        }

        // Determine if we are moving forward or backward
        int stepDir = (currentStep < endStep) ? 1 : -1;
        int nextStep = currentStep + stepDir;

        StackPane currentCell = getCellVisual(currentStep);
        StackPane nextCell = getCellVisual(nextStep);

        // Ensure the visual is currently in the currentCell before moving
        if (!currentCell.getChildren().contains(visual)) {
            currentCell.getChildren().removeIf(n -> tag.equals(n.getId()));
            currentCell.getChildren().add(visual);
        }

        if (cameraFollower != null) {
            cameraFollower.accept(nextStep);
        }

        // Use your existing AnimationManager!
        AnimationManager.animateMonsterMove(visual, currentCell, nextCell, () -> {
            currentCell.getChildren().remove(visual);
            nextCell.getChildren().removeIf(n -> tag.equals(n.getId()));
            nextCell.getChildren().add(visual);

            // Recursively call the next step
            moveStepByStep(visual, nextStep, endStep, tag, cameraFollower, onComplete);
        });
    }

    private void handleMonsterDirectGlide(Monster monster, StackPane newVisual, int lastPos, boolean isPlayer, Runnable onComplete) {
        handleMonsterDirectGlide(monster, newVisual, lastPos, isPlayer, null, onComplete);
    }

    private void handleMonsterDirectGlide(Monster monster, StackPane newVisual, int lastPos, boolean isPlayer,
                                          IntConsumer cameraFollower, Runnable onComplete) {
        String tag = isPlayer ? "player_visual" : "opponent_visual";
        newVisual.setId(tag);

        int targetPos = monster.getPosition();
        StackPane startCell = getCellVisual(lastPos);
        StackPane targetCell = getCellVisual(targetPos);

        if (lastPos == -1 || lastPos == targetPos || startCell == null || targetCell == null) {
            if (targetCell != null) {
                targetCell.getChildren().removeIf(n -> tag.equals(n.getId()));
                targetCell.getChildren().add(newVisual);
            }
            if (onComplete != null) onComplete.run();
            return;
        }

        StackPane visual = null;
        for (javafx.scene.Node child : startCell.getChildren()) {
            if (tag.equals(child.getId()) && child instanceof StackPane) {
                visual = (StackPane) child;
                break;
            }
        }

        if (visual == null) {
            visual = newVisual;
            startCell.getChildren().removeIf(n -> tag.equals(n.getId()));
            startCell.getChildren().add(visual);
        }

        StackPane movingVisual = visual;
        if (cameraFollower != null) {
            cameraFollower.accept(targetPos);
        }
        AnimationManager.animateMonsterMove(movingVisual, startCell, targetCell, () -> {
            startCell.getChildren().remove(movingVisual);
            targetCell.getChildren().removeIf(n -> tag.equals(n.getId()));
            targetCell.getChildren().add(newVisual);
            if (onComplete != null) onComplete.run();
        });
    }
}