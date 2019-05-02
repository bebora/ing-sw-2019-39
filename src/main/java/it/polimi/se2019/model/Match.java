package it.polimi.se2019.model;

import it.polimi.se2019.model.board.Board;
import it.polimi.se2019.model.board.BoardCreator;
import it.polimi.se2019.model.board.Color;
import it.polimi.se2019.model.board.Tile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;

/**
 * Container class for all the information of the Match being played
 */
public abstract class Match {

    public Match(List<Player> players, String boardFilename, int numSkulls) {
        this.players = players;
        finalFrenzy = false;
        turnEnd = false;
        firstPlayer = rand.nextInt(players.size());
        currentPlayer = firstPlayer;
        board = BoardCreator.parseBoard(boardFilename, numSkulls);
    }


    Random rand = new Random();

	/**
	 * True if player turn has ended
	 */
	private Boolean turnEnd;

	/**
	 * Board used for the Match
	 */
	private Board board;

	/**
	 * List of players playing the match
	 */
	private List <Player> players;

	/**
	 * Index of the player whose turn is the current
	 */
	private int currentPlayer;

	/**
	 * Index of the firstPlayer
	 */
	private int firstPlayer;

	/**
	 * If True, the activated mode is finalFrenzy
	 */
	private Boolean finalFrenzy;

	/**
	 * The game can be played in:
	 * <li>Normal Mode </li>
	 * <li> Domination Mode</li>
	 */


	public void addPlayer(Player player) {
		players.add(player);
	}

	public void startFrenzy() {
		finalFrenzy = TRUE;
		Boolean afterFirst;
		// Update actions
		if (firstPlayer < currentPlayer) {
			for (Player p : players) {
				afterFirst = !(players.indexOf(p) >= currentPlayer || players.indexOf(p) < firstPlayer);
				p.notifyFrenzy(afterFirst);
			}
		} else if (firstPlayer > currentPlayer) {
			for (Player p : players) {
				afterFirst = !(players.indexOf(p) >= currentPlayer && players.indexOf(p) < firstPlayer);
				p.notifyFrenzy(afterFirst);
			}
		}
		else {
			for (Player p : players)
				p.notifyFrenzy(true);
		}

		// Update reward points
		List<Player> toUpdate = players.stream().filter(p->!p.getDominationSpawn()).collect(Collectors.toList());
		for (Player p : toUpdate) {
			p.setFirstShotReward(false);
			if (!p.getRewardPoints().isEmpty())
				p.getRewardPoints().subList(1,p.getRewardPoints().size()).clear();
			p.getRewardPoints().addAll(new ArrayList<Integer>(Arrays.asList(2,1,1,1)));
		}
	}

	public void newTurn() {
		List<Player> deadPlayers = players.stream().
				filter(p -> p.getAlive() == ThreeState.FALSE).collect(Collectors.toList());
		for (Player p : deadPlayers) {
			scorePlayerBoard(p);
			p.resetPlayer(board.drawPowerUp());
		}

        if (deadPlayers.stream().filter(p -> !p.getDamages().get(11).getDominationSpawn()).count() > 1)
            players.get(currentPlayer).addPoints(1);

		board.refreshWeapons();
		board.refreshAmmos();

		currentPlayer = (currentPlayer + 1) %
				players.
					stream().
					filter(p->!p.getDominationSpawn()).
					collect(Collectors.toList()).
					size();
		if (checkFrenzy())
			startFrenzy();
	}


	public void scorePlayerBoard(Player player) {
		// first blood
		if (player.getFirstShotReward() == TRUE)
			player.getDamages().get(0).addPoints(1);
		Player maxPlayer = null;
		int currentReward = 0;

		// damages scoring
		Map<Player, Long> frequencyDamages =
				player.getDamages().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		Comparator<Player> givenDamages = Comparator.comparing(frequencyDamages::get);
		Comparator<Player> indices = Comparator.comparing(player.getDamages()::indexOf);


		Set<Player> damagingPlayers = new HashSet<>(player.getDamages());

		List<Player> damageOrder = damagingPlayers.stream().sorted(givenDamages.thenComparing(indices)).collect(Collectors.toList());
		while (damageOrder.isEmpty()) {
			if (currentReward < player.getRewardPoints().size()) {
				damageOrder.get(0).addPoints(player.getRewardPoints().get(currentReward));
			} else {
				damageOrder.get(0).addPoints(1);
			}

			damageOrder.remove(0);
			currentReward++;
		}
		if (player.getDamages().size() >= 11) {
			scoreDeadShot(player);
		}
	}

	public abstract void scoreDeadShot(Player player);
	public List<Player> getPlayersInTile(Tile tile){
		return this.getPlayers().stream()
				.filter(p -> p.getTile() == tile)
				.collect(Collectors.toList());
	}

	public List<Player> getPlayersInRoom(Color room){
		return this.getPlayers().stream()
				.filter(p -> p.getTile().getRoom() == room)
				.collect(Collectors.toList());
	}

	public boolean checkFrenzy() {
		return getBoard().getKillShotTrack().size() >= getBoard().getSkulls() * 2;
	}

	public abstract List<Player> getWinners();
	public List<Player> getPlayers(){ return players;}
	public int getCurrentPlayer(){return currentPlayer;}
	public Board getBoard(){return board; }

	public int getFirstPlayer() {
		return firstPlayer;
	}

	public Boolean getFinalFrenzy() {
		return finalFrenzy;
	}

}