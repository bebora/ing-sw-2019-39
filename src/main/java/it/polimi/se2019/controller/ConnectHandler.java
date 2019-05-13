package it.polimi.se2019.controller;

import it.polimi.se2019.network.ViewReceiverInterface;
import it.polimi.se2019.network.ViewUpdater;
import it.polimi.se2019.network.ViewUpdaterRMI;
import it.polimi.se2019.view.VirtualView;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Handler exposed by the RMI server to the client so that it can request to connect
 */
public class ConnectHandler extends UnicastRemoteObject implements ConnectInterface{
    private LobbyController lobbyController;

    @Override
    public void connect(String username, String password, boolean existingGame, String mode, ViewReceiverInterface receiver) {
        VirtualView virtualView = new VirtualView(lobbyController);
        ViewUpdater updater = new ViewUpdaterRMI(receiver);
        virtualView.setViewUpdater(updater);
        if (!existingGame)
            lobbyController.connectPlayer(username, password, mode, virtualView);
        else
            lobbyController.reconnectPlayer(username, password, virtualView);
    }

    @Override
    public RequestHandler getRequestHandler() {
        //TODO get and return requestHandler from lobbycontroller
        return null;
    }

    public ConnectHandler(LobbyController lobbyController) throws RemoteException {
        this.lobbyController = lobbyController;
    }
}