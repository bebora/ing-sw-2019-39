package it.polimi.se2019.controller;

import it.polimi.se2019.Logger;
import it.polimi.se2019.MyProperties;
import it.polimi.se2019.Observer;
import it.polimi.se2019.Priority;
import it.polimi.se2019.model.Player;
import it.polimi.se2019.model.ThreeState;
import it.polimi.se2019.model.actions.Action;
import it.polimi.se2019.model.board.Color;
import it.polimi.se2019.model.board.Tile;
import it.polimi.se2019.model.cards.Direction;
import it.polimi.se2019.model.cards.PowerUp;
import it.polimi.se2019.model.cards.Weapon;

import java.util.Collections;
import java.util.List;

public class TimerCostrainedEventHandler extends Thread implements EventHandler {
    private long start;
    private int time;
    private boolean active;
    private boolean blocked;
    private Observer observer;
    private RequestDispatcher requestDispatcher;
    private AcceptableTypes acceptableTypes;
    private boolean notifyOnEnd;

    public boolean isBlocked() {
        return blocked;
    }

    public TimerCostrainedEventHandler(Observer observer, RequestDispatcher requestDispatcher, AcceptableTypes acceptableTypes) {
        time = Integer.parseInt(MyProperties.getInstance().getProperty("time"));
        active = true;
        this.observer = observer;
        this.requestDispatcher = requestDispatcher;
        this.acceptableTypes = acceptableTypes;
        this.blocked = false;
        this.notifyOnEnd = true;
    }

    public void setNotifyOnEnd(boolean notifyOnEnd) {
        this.notifyOnEnd = notifyOnEnd;
    }

    public void endHandler() {
        active = false;
        blocked = true;
    }

    public synchronized void checkFinished() {
        if (System.currentTimeMillis() >= start + time*1000) {
            endHandler();
        }

    }

    public synchronized void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public void run() {
        requestDispatcher.addReceivingType(acceptableTypes.getAcceptedTypes(), this);
        requestDispatcher.getViewUpdater().sendAcceptableType(acceptableTypes);
        this.start = System.currentTimeMillis();
        while (active && !blocked) {
            checkFinished();
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                Logger.log(Priority.WARNING, "FINISHING SLEEP");
            }
            Logger.log(Priority.DEBUG, "Millis elapsed: " + (System.currentTimeMillis() - this.start));
        }
        synchronized (this) {
            if (!blocked && notifyOnEnd) {
                observer.updateOnStopSelection(ThreeState.TRUE);
            }
        }
    }

    @Override
    public synchronized void receiveAction(Action action) {
        if (active && acceptableTypes.getSelectableActions().checkForCoherency(Collections.singletonList(action))) {
            observer.updateOnAction(action);
            endHandler();
        }
    }

    @Override
    public void receiveDirection(Direction direction) {
        if (active && acceptableTypes.getSelectableDirections().checkForCoherency(Collections.singletonList(direction))) {
            observer.updateOnDirection(direction);
            endHandler();
        }
    }

    @Override
    public synchronized void receiveEffect(String effect) {
        if (active && acceptableTypes.getSelectableEffects().checkForCoherency(Collections.singletonList(effect))) {
            observer.updateOnEffect(effect);
            endHandler();
        }
    }

    @Override
    public synchronized void receivePlayer(List<Player> players) {
        if (active && acceptableTypes.getSelectablePlayers().checkForCoherency(players)) {
            observer.updateOnPlayers(players);
            endHandler();
        }
    }

    @Override
    public synchronized void receivePowerUps(List<PowerUp> powerUps, boolean discard) {
        if (active && acceptableTypes.getSelectablePowerUps().checkForCoherency(powerUps)) {
            endHandler();
            observer.updateOnPowerUps(powerUps, discard);
        }
    }

    @Override
    public synchronized void receiveRoom(Color color) {
        if (active && acceptableTypes.getSelectableRooms().checkForCoherency(Collections.singletonList(color))) {
            observer.updateOnRoom(color);
            endHandler();
        }
    }

    public synchronized void receiveStop() {
        if (active){
            observer.updateOnStopSelection(ThreeState.FALSE);
            setBlocked(true);
            endHandler();
        }
    }

    @Override
    public synchronized void receiveTiles(List<Tile> tiles) {
        if (active && acceptableTypes.getSelectableTileCoords().checkForCoherency(tiles)) {
            observer.updateOnTiles(tiles);
            endHandler();
        }
    }

    @Override
    public synchronized void receiveWeapon(Weapon weapon) {
        if (active && acceptableTypes.getSelectableWeapons().checkForCoherency(Collections.singletonList(weapon))) {
            observer.updateOnWeapon(weapon);
            endHandler();
        }
    }
}
