package it.polimi.se2019.view;

import it.polimi.se2019.Logger;
import it.polimi.se2019.Priority;
import it.polimi.se2019.controller.updatemessage.*;
import it.polimi.se2019.network.ViewReceiverInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class UpdateVisitor {
    private ViewReceiverInterface concreteViewReceiver;

    public UpdateVisitor(ViewReceiverInterface concreteViewReceiver) {
        this.concreteViewReceiver = concreteViewReceiver;
    }

    public void visit(PingUpdate pingUpdate) {
        try {
            concreteViewReceiver.receivePing();
        }
        catch (RemoteException e) {
            Logger.log(Priority.ERROR, "Unexpected RemoteException while calling local method");
        }
    }

    public void visit(SelectableOptionsUpdate selectableOptionsWrapper) {
        SelectableOptionsWrapper selectableOptions = selectableOptionsWrapper.getSelectableOptionsWrapper();
        try {
            concreteViewReceiver.receiveSelectablesWrapper(selectableOptions);
        }
        catch (RemoteException e) {
            Logger.log(Priority.ERROR, "Unexpected RemoteException while calling local method");
        }
    }

    public void visit(AmmosTakenUpdate update) {
        List<String> ammos= update.getPlayerAmmos();
        String playerId = update.getPlayerId();
        try {
            concreteViewReceiver.receiveAmmosTaken(playerId, new ArrayList<>(ammos));
        }
        catch (RemoteException e) {
            Logger.log(Priority.ERROR, "Unexpected RemoteException while calling local method");
        }
    }
    public void visit(AttackPlayerUpdate update) {
        String attackerId = update.getAttackerId();
        String receiverId = update.getReceiverId();
        int damageAmount = update.getDamageAmount();
        int marksAmount = update.getMarksAmount();
        try {
            concreteViewReceiver.receiveAttackPlayer(attackerId, receiverId, damageAmount,marksAmount);
        }
        catch (RemoteException e) {
            Logger.log(Priority.ERROR, "Unexpected RemoteException while calling local method");
        }
    }

    public void visit(MovePlayerUpdate update) {
        String playerId = update.getPlayerId();
        int posx = update.getPosx();
        int posy = update.getPosy();
        try {
            concreteViewReceiver.receiveMovePlayer(playerId, new ViewTileCoords(posy, posx));
        }
        catch (RemoteException e) {
            Logger.log(Priority.ERROR, "Unexpected RemoteException while calling local method");
        }
    }
    public void visit(PopupMessageUpdate update) {
        String message = update.getMessage();
        try {
            concreteViewReceiver.receivePopupMessage(message);
        }
        catch (RemoteException e) {
            Logger.log(Priority.ERROR, "Unexpected RemoteException while calling local method");
        }
    }


    public void visit(TileUpdate update) {
        List<String> ammos = update.getAmmos();
        List<String> weapons = update.getWeapons();
        ViewTileCoords coords = update.getCoords();
        ViewTile viewTile = new ViewTile(ammos, weapons, coords);
        try {
            concreteViewReceiver.receiveTile(viewTile);
        }
        catch (RemoteException e) {
            Logger.log(Priority.ERROR, "Unexpected RemoteException while calling local method");
        }
    }
    public void visit(TotalUpdate update) {
        String username = update.getUsername();
        ViewBoard board = update.getBoard();
        ViewTileCoords perspective = update.getPerspective();
        List<ViewPlayer> players = update.getPlayers();
        String idView = update.getIdView();
        int points = update.getPoints();
        List<ViewPowerUp> powerUps = update.getPowerUps();
        List<ViewWeapon> loadedWeapons = update.getLoadedWeapons();
        String currentPlayerId = update.getCurrentPlayerId();
        try {
            concreteViewReceiver.receiveTotalUpdate(username,
                    board,
                    perspective,
                    new ArrayList<>(players),
                    idView,
                    points,
                    new ArrayList<>(powerUps),
                    new ArrayList<>(loadedWeapons),
                    currentPlayerId);
        }
        catch (RemoteException e) {
            Logger.log(Priority.ERROR, "Unexpected RemoteException while calling local method");
        }
    }
    public void visit(WeaponTakenUpdate update) {
        ViewWeapon takenWeapon = update.getTakenWeapon();
        ViewWeapon discardedWeapon = update.getDiscardedWeapon();
        String playerId = update.getPlayerId();
        try {
            concreteViewReceiver.receiveWeaponTaken(takenWeapon, discardedWeapon, playerId);
        }
        catch (RemoteException e) {
            Logger.log(Priority.ERROR, "Unexpected RemoteException while calling local method");
        }

    }
}
