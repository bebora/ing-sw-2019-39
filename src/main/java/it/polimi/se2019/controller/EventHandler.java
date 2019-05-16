package it.polimi.se2019.controller;

import it.polimi.se2019.model.Player;
import it.polimi.se2019.model.actions.Action;
import it.polimi.se2019.model.board.Color;
import it.polimi.se2019.model.board.Tile;
import it.polimi.se2019.model.cards.PowerUp;
import it.polimi.se2019.model.cards.Weapon;

import java.util.List;

public interface EventHandler {
    void receiveRoom(Color color);

    void receivePlayer(List<Player> players);

    void receiveAction(Action action);

    void receiveTiles(List<Tile> tiles);

    void receiveWeapon(Weapon weapon);

    void receiveDiscardPowerUps(List<PowerUp> powerUps);

    void receiveEffect(String effect);

    void receiveStop(boolean reverse);

}