package it.polimi.se2019.controller;

import it.polimi.se2019.controller.events.ConnectionRequest;
import it.polimi.se2019.controller.events.SelectAction;
import it.polimi.se2019.controller.events.SelectPlayers;
import it.polimi.se2019.model.Player;
import it.polimi.se2019.view.View;
import it.polimi.se2019.view.VirtualView;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manage new Players connection and reconnections.
 * Handles the creation and the linking to the VirtualViews, checking if the username is already used in an active game.
 */
public class LobbyController extends EventVisitor {
    private List<GameController> games;
    private GameController waitingGame;

    /**
     * Visit a ConnectionRequest handling the connection of a new user or the reconnection of a current one.
     * @param connectionRequest
     */
    @Override
    public synchronized void visit(ConnectionRequest connectionRequest) {
        String username = connectionRequest.getUsername();
        boolean found = false;
        for (GameController game : games) {
            List<String> allUsername = game.getMatch().getPlayers().stream().
                    filter(p -> !p.getOnline()).
                    map(Player::getUsername).
                    collect(Collectors.toList());
            if (allUsername.contains(username)) {
                found = true;
                reconnectPlayer(game, username, connectionRequest.getVv());
            }
        }
        if (!found) {
            connectPlayer(username, connectionRequest.getVv());
        }

    }

    /**
     * Reconnect a player to the game linking the player to the current VirtualView
     * @param game active game where the username was found
     * @param username username found in the game
     * @param vv New VirtualView to link to the corresponding player
     */
    public void reconnectPlayer(GameController game, String username, View vv) {
        Player player = game.getMatch().getPlayers().
                stream().filter(p -> p.getUsername().equals(username)).findFirst().orElse(null);
        if (player != null) {
            player.setVirtualView(vv);
            player.setOnline(true);

        }
        //TODO send popup update success
    }


    /**
     * Add a player to the waiting game, linking the VirtualView to the Player.
     * Manage the start of the timeout to start the game if enough players are in.
     * @param username
     * @param vv
     */
    public void connectPlayer(String username, View vv) {
        Player player = new Player(false, username);
        player.setVirtualView(vv);
        waitingGame.getMatch().addPlayer(player);
        //TODO start timer when players are 3
        //TODO send popup update success
    }
}

