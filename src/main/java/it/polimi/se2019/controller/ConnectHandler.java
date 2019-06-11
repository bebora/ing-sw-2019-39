package it.polimi.se2019.controller;

import it.polimi.se2019.Logger;
import it.polimi.se2019.Priority;
import it.polimi.se2019.network.ViewReceiverInterface;
import it.polimi.se2019.network.ViewUpdater;
import it.polimi.se2019.network.ViewUpdaterRMI;
import it.polimi.se2019.view.VirtualView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Handler exposed by the RMI server to the client so that it can request to connect
 */
public class ConnectHandler extends UnicastRemoteObject implements ConnectInterface{
    private transient LobbyController lobbyController;

    @Override
    public void connect(String username, String password, boolean existingGame, String mode, ViewReceiverInterface receiver) throws RemoteException{
        VirtualView virtualView = new VirtualView(lobbyController);
        virtualView.setUsername(username);
        ViewUpdater updater = new ViewUpdaterRMI(receiver, virtualView);
        virtualView.setViewUpdater(updater);
        if (!existingGame)
            lobbyController.connectPlayer(username, password, mode, virtualView);
        else
            lobbyController.reconnectPlayer(username, password, virtualView);
        ((ViewUpdaterRMI) updater).getPinger().start();
    }

    @Override
    public RequestDispatcher getRequestHandler(String username, String password) throws RemoteException {
        String token = String.format("%s$%s", username, password.hashCode());
        return lobbyController.getRequestHandler(token);
    }

    public ConnectHandler(LobbyController lobbyController) throws RemoteException {
        this.lobbyController = lobbyController;
        try {
            Logger.log(Priority.DEBUG, "Server hostname: "+InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            Logger.log(Priority.DEBUG, e.getMessage());
        }
    }
}
