package it.polimi.se2019.controller;

import it.polimi.se2019.Logger;
import it.polimi.se2019.Priority;
import it.polimi.se2019.model.Match;
import it.polimi.se2019.model.Mode;
import it.polimi.se2019.model.Player;
import it.polimi.se2019.network.AuthenticationErrorException;
import it.polimi.se2019.view.VirtualView;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manage new Players connection and reconnections.
 * Handles the creation and the linking to the VirtualViews, checking if the username is already used in an active game.
 */
public class LobbyController{
    private List<GameController> games;
    private Map<Mode, List<Player>> waitingPlayers;
    private Map<Mode, Timer> waitingTimers;


    /**
     * Create a lobby allowing creation of games for the modes {@code #modes}
     * Initialize timers for starting game on each Mode
     * @param modes allowed modes to play a game
     */
    public LobbyController(List<Mode> modes) {
        waitingPlayers = new EnumMap<>(Mode.class);
        for (Mode mode : modes) {
            waitingPlayers.put(mode, new ArrayList<>());
        }
        games = new ArrayList<>();
        waitingTimers = new EnumMap<>(Mode.class);

    }


    /**
     * Reconnect a player to a game already started
     * @param username
     * @param password
     * @param view Related client's VirtualView
     */
    public synchronized void reconnectPlayer(String username, String password, VirtualView view) {
        Player player = null;
        String token = String.format("%s$%s", username, password.hashCode());
        Match ownGame;
        List<String> allTokens = new ArrayList<>();
        for (GameController game : games) {
             allTokens.addAll(game.getMatch().getPlayers().stream().
                    filter(p -> !p.getOnline()).
                    map(Player::getToken).
                    collect(Collectors.toList()));
            if (allTokens.contains(token)) {
                player = game.getMatch().getPlayers().
                        stream().filter(p -> p.getToken().equals(token)).findFirst().orElse(null);
                ownGame = game.getMatch();
                player.setVirtualView(view);
                player.setOnline(true);
                view.getViewUpdater().sendPopupMessage("Reconnected succesfully!");
                player.getVirtualView().getViewUpdater().sendTotalUpdate(username,ownGame.getBoard(), ownGame.getPlayers(),
                        view.getIdView(), player.getPoints(), player.getPowerUps(),
                        player.getWeapons(), ownGame.getPlayers().get(ownGame.getCurrentPlayer()));
                break;
            }
        }
        if (player == null) {
            view.getViewUpdater().sendPopupMessage("Errore nel login!");
        }

    }

    /**
     * Add a player to the waiting Players list, linking the VirtualView to the Player.
     * Manage the start of the timeout to start the game if enough players are in.
     * Manage the start of the timer in case of {@link #waitingPlayers} greater or equal than 3; start game if they are 5.
     * @param username
     * @param password
     * @param mode
     */
    public synchronized void connectPlayer(String username, String password, String mode, VirtualView view) {
        mode = mode.toUpperCase();
        List<String> allUsername = new ArrayList<>();
        allUsername.addAll(getWaitingPlayers().values().stream().flatMap(List::stream).map(Player::getUsername).collect(Collectors.toList()));
        for (GameController game : games) {
            allUsername.addAll(game.getMatch().getPlayers().stream().
                    filter(p -> !p.getOnline()).
                    map(Player::getUsername).
                    collect(Collectors.toList()));
                //TODO throw exception to close the connection
        }
        if (allUsername.contains(username)) {
            view.getViewUpdater().sendPopupMessage("Username is already in use! Can't connect.");
            throw new AuthenticationErrorException();
        }
        String token = String.format("%s$%s", username, password.hashCode());
        Player player = new Player(token);
        player.setVirtualView(view);
        List<Player> modeWaiting = waitingPlayers.get(Mode.valueOf(mode));
        if (modeWaiting == null) {
            view.getViewUpdater().sendPopupMessage("Selected mode does not exist! Can't connect.");
            throw new AuthenticationErrorException();
        } else {
            Logger.log(Priority.DEBUG, "PLAYER CONNECTED");
            modeWaiting.add(player);

            view.getViewUpdater().sendPopupMessage("SUCCESS");
            if (modeWaiting.size() == 3) {
                Timer timer = new Timer();
                waitingTimers.put(Mode.valueOf(mode), timer);
                timer.schedule(new LobbyTask(this, Mode.valueOf(mode)), 5000);
                Logger.log(Priority.DEBUG, "TIMER STARTED");
            }
            else if (modeWaiting.size() >= 5) {
                waitingTimers.get(Mode.valueOf(mode)).cancel();
                startGame(Mode.valueOf(mode));
                /*Timer timer = new Timer();
                timer.schedule(new LobbyTask(this, Mode.valueOf(mode)), 0);*/
            }
        }
    }

    /**
     * Get the RequestHandler related to the {@code token}
     * @param token token of the related player
     * @return
     */
    public RequestDispatcher getRequestHandler(String token) {
        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(getWaitingPlayers().values().stream().flatMap(List::stream).collect(Collectors.toList()));
        allPlayers = allPlayers.stream().filter(Player::getOnline).collect(Collectors.toList());
        Player requestingPlayer = allPlayers.stream().filter(p -> p.getToken().equals(token)).findFirst().orElse(null);
        if (requestingPlayer == null) return null;
        return requestingPlayer.getVirtualView().getRequestDispatcher();
    }

    public Map<Mode, List<Player>> getWaitingPlayers() {
        return waitingPlayers;
    }

    /**
     * Start a game using the {@link GameController}.
     * Check and remove offline players from the waiting List
     * @param mode
     */
    public synchronized void startGame(Mode mode) {
        Logger.log(Priority.DEBUG, "GAME TRYING TO START");
        List<Player> currentWaiting = waitingPlayers.get(mode);
        currentWaiting.removeAll(currentWaiting.stream().filter(p -> !p.getOnline()).collect(Collectors.toList()));
        List<Player> playing = new ArrayList<>(currentWaiting);
        if (playing.size() > 5) {
            playing = new ArrayList<>(playing.subList(0,5));
        }
        else if (playing.size() < 3) {
            Logger.log(Priority.DEBUG, "GAME NOT STARTING");
            return;
        }
        else {
                playing = new ArrayList<>(playing);
        }
        waitingPlayers.get(mode).removeAll(playing);
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String nameDir = classloader.getResource("boards").getPath();
        File dir = new File(nameDir);
        File[] directoryListing = dir.listFiles();
        int rnd = new Random().nextInt(directoryListing.length);
        String boardName = directoryListing[rnd].getName();
        GameController gameController = new GameController(playing, boardName, 8, mode.equals(Mode.DOMINATION), this);
        games.add(gameController);
        playing.forEach(p -> p.getVirtualView().setGameController(gameController));
        gameController.getMatch().updateViews();
        gameController.startTurn();
    }

    public List<GameController> getGames() {
        return games;
    }
}