package it.polimi.se2019.controller;

import it.polimi.se2019.Logger;
import it.polimi.se2019.Observer;
import it.polimi.se2019.Priority;
import it.polimi.se2019.model.Player;
import it.polimi.se2019.model.actions.Action;
import it.polimi.se2019.model.board.Color;
import it.polimi.se2019.model.board.Tile;
import it.polimi.se2019.model.cards.Effect;
import it.polimi.se2019.model.cards.PowerUp;
import it.polimi.se2019.model.cards.Weapon;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimerCostrainedEventHandler extends Thread implements EventHandler {
    private long start;
    private int time;
    private boolean active;
    private Observer observer;
    private RequestDispatcher requestDispatcher;
    private List<ReceivingType> receivingTypes;

    public TimerCostrainedEventHandler(int time, Observer observer, RequestDispatcher requestDispatcher, List<ReceivingType> receivingTypes) {
        this.time = time;
        active = true;
        this.observer = observer;
        this.requestDispatcher = requestDispatcher;
        this.receivingTypes = receivingTypes;
    }

    public void endHandler() {
        active = false;
    }

    public synchronized void checkFinished() {
        if (System.currentTimeMillis() >= start + time*1000) {
            endHandler();
            //TODO OBSERVER NEXT TURN
        }

    }

    @Override
    public void run() {
        requestDispatcher.addReceivingType(receivingTypes, this);
        this.start = System.currentTimeMillis();
        while (!active) {
            checkFinished();
            try {
                TimeUnit.MINUTES.sleep(1);
            }
            catch (InterruptedException e) {
                Logger.log(Priority.WARNING, "COULDN'T SLEEP");
            }
        }
    }

    @Override
    public synchronized void receiveRoom(Color color) {
        if (active) {
            observer.updateOnRoom(color);
            endHandler();
        }
    }

    @Override
    public synchronized void receivePlayer(List<Player> players) {
        if (active) {
            observer.updateOnPlayers(players);
            endHandler();
        }
    }

    @Override
    public synchronized void receiveAction(Action action) {
        if (active) {
            //TODO ADD UPDATE ON ACTION!!!!!!!
            endHandler();
        }
    }

    @Override
    public synchronized void receiveTiles(List<Tile> tiles) {
        if (active) {
            observer.updateOnTiles(tiles);
            endHandler();
        }
    }

    @Override
    public synchronized void receiveWeapon(Weapon weapon) {
        if (active) {
            observer.updateOnWeapon(weapon);
            endHandler();
        }

    }

    @Override
    public synchronized void receiveDiscardPowerUps(List<PowerUp> powerUps) {
        if (active) {
            observer.updateOnPowerUps(powerUps);
            endHandler();
        }
    }

    @Override
    public synchronized void receiveEffect(Effect effect) {
        if (active) {
            observer.updateOnEffect(effect);
            endHandler();
        }
    }

    @Override
    public synchronized void receiveChoice(Choice choice) {
        if (active) {
            //TODO ADD CHOICE
            endHandler();
        }
    }

    public synchronized List<ReceivingType> getReceivingTypes () {
        return receivingTypes;
    }
}
