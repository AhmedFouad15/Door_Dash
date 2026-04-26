package game.engine.interfaces;

import game.engine.monsters.Monster;

public interface CanisterModifier {
    public void modifyCanisterEnergy(Monster monster, int canisterValue);

}
