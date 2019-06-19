package it.polimi.se2019.controller;

import it.polimi.se2019.Logger;
import it.polimi.se2019.Observer;
import it.polimi.se2019.Priority;
import it.polimi.se2019.controller.events.IncorrectEvent;
import it.polimi.se2019.model.*;
import it.polimi.se2019.model.actions.Action;
import it.polimi.se2019.model.actions.Reload;
import it.polimi.se2019.model.board.Color;
import it.polimi.se2019.model.board.Tile;
import it.polimi.se2019.model.cards.Moment;
import it.polimi.se2019.model.cards.PowerUp;
import it.polimi.se2019.view.SelectableOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static it.polimi.se2019.controller.ReceivingType.PLAYERS;
import static it.polimi.se2019.controller.ReceivingType.STOP;
import static it.polimi.se2019.model.ThreeState.*;

/**
 * Controller class related to a single game.
 * Handles the game flow, using {@link #match} properties.
 */
public class GameController extends Observer {
    private Match match;
    private LobbyController lobbyController;
    private int actionCounter=0;
    private Player currentPlayer;
    private ActionController actionController;
    private List<Player> spawnablePlayers;
    private TimerCostrainedEventHandler timerCostrainedEventHandler;
    private AcceptableTypes acceptableTypes;
    private boolean matchEnd;
    private boolean turnEnd;
    private boolean skip;
    private boolean action;
    private PowerUp toDiscard;
    private CountDownLatch countDownLatch;

    /**
     * Handles the disconnection of a player, communicating to other players that the player got disconnected.
     * Calls the related checkEnd method, checking if the game needs to stop.
     * @param username
     */
    public synchronized void checkEnd(String username) {
        if (!matchEnd) {
            match.getUpdateSender().sendPopupMessage(String.format("Player %s is offline!", username));
            checkEnd();
        }
    }

    /**
     * Checks if the game needs to stop prematurely for lack of players.
     * Notify the players sending the winners if the game ends.
     */
    public synchronized void checkEnd() {
        if (!matchEnd && match.getPlayers().stream().filter(p -> !p.getDominationSpawn() && p.getOnline()).count() < 3) {
            matchEnd = true;
            sendWinners();
        }
    }

    /**
     * Handles receiving an action from the client, starting the related {@link #actionController}.
     * @param action chose by the corresponding client.
     */
    @Override
    public void updateOnAction(Action action){
        this.action = true;
        actionController = new ActionController(match,this);
        actionController.updateOnAction(action);
    }

    /**
     * Handles receiving a List of players from the client.
     * Used for domination spawn overkill, in Domination mode.
     * @param players
     */
    @Override
    public void updateOnPlayers(List<Player> players) {
        if (acceptableTypes.getSelectablePlayers().checkForCoherency(players)) {
            currentPlayer.getVirtualView().getRequestDispatcher().clear();
            players.get(0).receiveShot(currentPlayer, 1, 0, true);

        }
        else {
            throw new IncorrectEvent("Wrong players!");
        }
        countDownLatch.countDown();
    }

    /**
     * Handles receiving a List of powerUps from the client.
     * Used for:
     * <li>Spawning players at the start of the Game</li>
     * <li>Choosing powerUps, and start the related {@code #effectController}.</li>
     * @param powerUps
     * @param discard
     */
    @Override
    public void updateOnPowerUps(List<PowerUp> powerUps, boolean discard) {
        if (acceptableTypes.getSelectablePowerUps().checkForCoherency(powerUps)) {
            if (currentPlayer.getAlive() == OPTIONAL) {
                currentPlayer.getVirtualView().getRequestDispatcher().clear();
                currentPlayer.setAlive(TRUE);
                Tile tile = match.getBoard().getTiles().stream().flatMap(List::stream).
                        filter(t -> t != null && t.isSpawn() && t.getRoom().equals(Color.valueOf(powerUps.get(0).getDiscardAward().toString()))).findFirst().orElseThrow(() -> new IncorrectEvent("Error in PowerUps!"));
                currentPlayer.setTile(tile);
                currentPlayer.discardPowerUp(powerUps.get(0), false);
                if (!skip)
                    playTurn();
                else {
                    skip = false;
                    endTurn(true);
                }
            }
            else {
                this.action = false;
                /*AcceptableTypes tilesAccepted = new AcceptableTypes(Collections.singletonList(ReceivingType.TILES));
                List<Tile> tiles = new ArrayList<>(match.getBoard().getTiles().stream().flatMap(List::stream).filter(t -> t!= null).collect(Collectors.toList()));
                tilesAccepted.setSelectableTileCoords(new SelectableOptions<>(tiles, 1 , 1, "Select a tile to move!"));
                Choice tileRequest = new Choice(currentPlayer.getVirtualView().getRequestDispatcher(), tilesAccepted);
                switch (tileRequest.getReceivingType()) {
                    case STOP:
                        updateOnStopSelection(TRUE);
                        break;
                    case TILES:
                        currentPlayer.discardPowerUp(powerUps.get(0), false);
                        currentPlayer.setTile(tiles.get(0));
                        break;
                }
                if(currentPlayer.hasPowerUp(Moment.OWNROUND) || actionCounter < currentPlayer.getMaxActions()){
                    playTurn();
                }
                else {
                    endTurn(false);
                }*/
                currentPlayer.getVirtualView().getRequestDispatcher().clear();
                toDiscard = (powerUps.get(0));
                currentPlayer.discardPowerUp(toDiscard, false);
                EffectController effectController = new EffectController(powerUps.get(0).getEffect(), null, match, currentPlayer, match.getPlayers(), this);
                effectController.nextStep();
            }
        }
        else {
            throw new IncorrectEvent("PowerUps not acceptable!");
        }
    }


    /**
     * Creates a GameController istance, managing the flow of the game.
     * Contains the information required to create a new Match, the mode of the new Match, and the related {@link #lobbyController}.
     * @param players clients playing the game
     * @param boardName refers to the name of the board (chosen randomly) used
     * @param numSkulls number of max skulls in the board.
     * @param domination whether the mode is domination or not
     * @param lobbyController
     */
    public GameController(List<Player> players, String boardName, int numSkulls, boolean domination, LobbyController lobbyController) {
        this.lobbyController = lobbyController;
        if(!domination){
            match = new NormalMatch(players,boardName,numSkulls);
        }else{
            match = new DominationMatch(players,boardName,numSkulls);
        }
        currentPlayer = match.getPlayers().get(match.getCurrentPlayer());
        matchEnd = false;
        turnEnd = false;
        skip = false;
        acceptableTypes = new AcceptableTypes(new ArrayList<>());
    }

    public void startTurn(){
        if (matchEnd) {
            Logger.log(Priority.DEBUG, "Game ended, stopping turn");
        }
        else if(currentPlayer.getAlive() == ThreeState.OPTIONAL){
            for(int i = 0; i < 2; i++){
                currentPlayer.addPowerUp(match.getBoard().drawPowerUp(),false);
            }
            List<ReceivingType> receivingTypes = new ArrayList<>(Collections.singleton(ReceivingType.POWERUP));
            acceptableTypes = new AcceptableTypes(receivingTypes);
            acceptableTypes.setSelectablePowerUps(new SelectableOptions<>(currentPlayer.getPowerUps(), 1,1, "Seleziona un PowerUp!"));
            timerCostrainedEventHandler = new TimerCostrainedEventHandler(
                    this,
                    currentPlayer.getVirtualView().getRequestDispatcher(),
                    acceptableTypes);
            timerCostrainedEventHandler.start();
        }
        else if (!currentPlayer.getOnline() || skip) {
            skip = false;
            endTurn(true);
        }
        else {
            playTurn();
        }
    }

    /**
     * Handles the start of the turn
     * Prompt the client with an action and/or powerups and/or the chance to stop the current turn.
     * Check if the turn is ended (if the player used a Reload action)
     */
    public void playTurn() {
        turnEnd = false;
        List<ReceivingType> receivingTypes = new ArrayList<>();
        acceptableTypes = new AcceptableTypes(receivingTypes);
        //Check for the presence
        if(actionCounter < currentPlayer.getMaxActions()) {
            receivingTypes.add(ReceivingType.ACTION);
            acceptableTypes.setSelectableActions(new SelectableOptions<>(currentPlayer.getActions(), 1, 1, "Select an Action!"));
        } else if (!match.getFinalFrenzy() && currentPlayer.getWeapons().stream().anyMatch(w -> !w.getLoaded())) {
            turnEnd = true;
            receivingTypes.add(ReceivingType.ACTION);
            Action reload = new Reload();
            currentPlayer.getActions().add(reload);
            acceptableTypes.setSelectableActions(new SelectableOptions<>(Arrays.asList(reload), 1, 1, "Select an Action!"));
        }
        else turnEnd = true;
        if (currentPlayer.getPowerUps().stream().
                filter(p -> p.getApplicability() == Moment.OWNROUND).
                count() >= 1) {
            receivingTypes.addAll(Arrays.asList(ReceivingType.POWERUP));
            List<PowerUp> usablePowerups = currentPlayer.getPowerUps().stream().
                    filter(p -> p.getApplicability() == Moment.OWNROUND).collect(Collectors.toList());
            acceptableTypes.setSelectablePowerUps(new SelectableOptions<>(usablePowerups, 1, 1, "Select a PowerUp!"));
        }
        if (turnEnd && !receivingTypes.isEmpty()) {
            receivingTypes.add(STOP);
            acceptableTypes.setStop(false, "End turn");
        }
        timerCostrainedEventHandler = new TimerCostrainedEventHandler(
                this,
                currentPlayer.getVirtualView().getRequestDispatcher(),
                acceptableTypes);
        timerCostrainedEventHandler.start();
    }

    public Match getMatch() {
        return match;
    }

    private boolean checkEndTurn() {
        if ((actionCounter == currentPlayer.getMaxActions() && (!currentPlayer.hasPowerUp(Moment.OWNROUND) && (!match.getFinalFrenzy()) && !currentPlayer.canReload())) || actionCounter == currentPlayer.getMaxActions() +1)
            return true;
        else return false;
    }

    @Override
    public void updateOnConclusion(){
        if (action)
            actionCounter++;
        actionController = null;
        match.getPlayers().stream().filter(p -> p.getVirtualView() != null && p.getVirtualView().getRequestDispatcher() != null).map(p -> p.getVirtualView().getRequestDispatcher()).forEach(rq -> rq.setEventHelper(match));
        if(currentPlayer.hasPowerUp(Moment.OWNROUND) || !checkEndTurn()){
            playTurn();
        }
        else {
            endTurn(false);
        }
    }

    public synchronized void endTurn(boolean skip) {
        currentPlayer.getActions().removeIf(p -> p.toString().equals("RELOAD"));
        // Players which have been killed in this turn with overkill
        List<Player> overkillPlayers = match.
                getPlayers().
                stream().
                filter(p -> !p.getDominationSpawn() && p.getDamages().size() == 12 && !p.getDamages().get(11).equals(p)).
                collect(Collectors.toList());
        List<Player> spawnPoints = match.
                getPlayers().
                stream().
                filter(Player::getDominationSpawn).
                collect(Collectors.toList());
        // This if can be true only in domination mode
        if (!spawnPoints.isEmpty() && !overkillPlayers.isEmpty() && !skip && currentPlayer.getOnline()) {
            acceptableTypes = new AcceptableTypes(Collections.singletonList(PLAYERS));
            for (Player current : overkillPlayers) {
                acceptableTypes.setSelectablePlayers(new SelectableOptions<>(spawnPoints, 1, 1, String.format("Select a spawn point to deposit %s overkill", current.getUsername())));
                timerCostrainedEventHandler = new TimerCostrainedEventHandler(this, currentPlayer.getVirtualView().getRequestDispatcher(), acceptableTypes);
                countDownLatch = new CountDownLatch(1);
                timerCostrainedEventHandler.setNotifyOnEnd(false);
                timerCostrainedEventHandler.start();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    Logger.log(Priority.ERROR, "Join on domination overkill blocked by " + e.getMessage());
                }
                if (!timerCostrainedEventHandler.isBlocked()) {
                    Player spawnPoint = spawnPoints.stream().findAny().orElse(null);
                    if (spawnPoint != null) {
                        spawnPoint.receiveShot(currentPlayer, overkillPlayers.size(), 0, true);
                    }
                    break;
                } else {
                    overkillPlayers.remove(current);
                }
            }
        }
        // Same conditions as before but player offline
        else if (!overkillPlayers.isEmpty()) {
            Player spawnPoint = spawnPoints.stream().findAny().orElse(null);
            if (spawnPoint != null) {
                spawnPoint.receiveShot(currentPlayer, overkillPlayers.size(), 0, true);
            }
        }
        if (match.newTurn()) {
            matchEnd = true;
            sendWinners();
            lobbyController.getGames().remove(this);
            return;
        }
        actionCounter = 0;
        currentPlayer = match.getPlayers().get(match.getCurrentPlayer());
        spawnablePlayers = match.getPlayers().stream()
                .filter(p -> p.getAlive() == ThreeState.FALSE)
                .collect(Collectors.toList());

        if (!matchEnd) {
            if (!spawnablePlayers.isEmpty())
                startSpawning();
            startTurn();
        } else {
            Logger.log(Priority.DEBUG, "Game ending");
    }

    }

    public void sendWinners() {
        matchEnd = true;
        Logger.log(Priority.DEBUG, "Parsing winners");
        List<Player> players = match.getWinners();
        StringBuilder stringBuffer = new StringBuilder("WINNERS$Winners are ");
        for (Player p : players) {
            stringBuffer.append(p.getUsername() + ", ");
        }
        match.getPlayers().stream().filter(Player::getOnline).forEach(p -> p.getVirtualView().getViewUpdater().sendPopupMessage(stringBuffer.toString()));
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            assert false;
        }
        //TODO stop all the socket connections
        match.getPlayers().stream().filter(Player::getOnline).forEach(p -> p.setOnline(false));
    }

    public void startSpawning(){
        List<TimerCostrainedEventHandler> timerCostrainedEventHandlers = new ArrayList<>();
        for(Player p: spawnablePlayers){
            List<ReceivingType> receivingTypes = Collections.singletonList(ReceivingType.POWERUP);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Observer spawner = new Spawner(countDownLatch, p ,match.getBoard());
            acceptableTypes = new AcceptableTypes(receivingTypes);
            acceptableTypes.setSelectablePowerUps(new SelectableOptions<>(p.getPowerUps(), 1,1,"Select a PowerUp to discard!"));
            timerCostrainedEventHandlers.add(new TimerCostrainedEventHandler(spawner,p.getVirtualView().getRequestDispatcher(), acceptableTypes));
            timerCostrainedEventHandlers.forEach(t -> t.start());
            try {
                countDownLatch.await();
                Logger.log(Priority.DEBUG, "One spawning done!");
            }
            catch (Exception e) {
                Logger.log(Priority.DEBUG, "Ended handler spawner!");
            }
        }
    }

    @Override
    public void updateOnStopSelection(ThreeState skip){
        currentPlayer.getVirtualView().getRequestDispatcher().clear();
        if (currentPlayer.getAlive() == OPTIONAL) {
            this.skip = true;
            updateOnPowerUps(Arrays.asList(acceptableTypes.getSelectablePowerUps().getOptions().stream().findAny().orElse(null)), true);
        }
        else if (skip.toBoolean() || acceptableTypes.isReverse()) {
            if(action)
                actionCounter++;
            actionController = null;
            if (skip.toSkip() || checkEndTurn()) {
                endTurn(skip.toSkip());
            }
            else {
                playTurn();
            }
        }
        else if (skip == FALSE) {
            endTurn(false);
        }
    }

}
