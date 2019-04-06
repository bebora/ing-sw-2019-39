package model;

import java.util.*;
import java.util.stream.Collectors;


public class Board {
	/**
	 * List of tiles that make up the Board
	 */
	private List<List<Tile>> tiles;
	/**
	 * Adjacency list of doors between tiles
	 */
	private List<Door> doors;
	/**
	 * Number of skulls remaining on the Board
	 */
	private int skulls;

	/**
	 * List of weapons remaining to be drawn
	 */
	private List<Weapon> weaponsDeck;

	/**
	 * List of players that got a Kill and an Overkill
	 * Every kill is composed by two elements added to the list:
	 * <li>
	 *     Player who gave the KillShot
	 * </li>
	 * <li>
	 *     Player who gave the OverKill; null gets added if no overkill.
	 * </li>
	 */
	private List<Player> killShotTrack;



	public static class Builder {

		private List<List<Tile>> tiles;
		private List<Door> doors;
		private int skulls;
		private List<Weapon> weaponsDeck;
		private List<Player> killShotTrack = new ArrayList<>();

		public Builder(int skulls) {
			this.skulls = skulls;
		}

		public Builder setDoors(List<Door> doors) {
			this.doors = doors;
			return this;

		}

		public Builder setTiles(List<List<Tile>> tiles) {
			this.tiles = tiles;
			return this;

		}

		public Builder setWeapon(List<Weapon> weaponsDeck) {
			this.weaponsDeck = weaponsDeck;
			return this;
		}

		public Board build() {
			return new Board(this);
		}
	}

	public Board(Builder builder) {
		this.tiles = builder.tiles;
		this.doors = builder.doors;
		this.skulls = builder.skulls;
		this.weaponsDeck = builder.weaponsDeck;
		this.killShotTrack = builder.killShotTrack;
	}

	/**
	 * Check if two tiles are linked by:
	 * <li>
	 *     A door
	 * </li>
	 * <li>
	 *     Being in the same room and having distance 1
	 * </li>
	 * @param tile1 first tile
	 * @param tile2 second tile
	 * @return true if the tiles are connected
	 */
	public boolean isLinked(Tile tile1,Tile tile2) {
		return doors.contains(new Door(tile1,tile2)) || (tile1.getRoom() == tile2.getRoom() && Tile.cabDistance(tile1,tile2) == 1);
	}

	/**
	 * Visible tiles by the pointOfView
	 * All tiles that are or in the same room, or in the same room of a tile connected by a Door to POV are visibleTiles
	 * @param pointOfView square used to calculate visibility
	 * @return a set containing visibleTiles tiles
	 */
	public Set<Tile> visibleTiles(Tile pointOfView) {
		Set<Color> visibleColors = tiles.stream().
				flatMap(List::stream).filter(t -> t != null).
				filter(t -> isLinked(t, pointOfView)).
				map(Tile::getRoom).
				collect(Collectors.toSet());
		return tiles.stream().
				flatMap(List::stream).filter(t -> t != null).
				filter(t -> visibleColors.contains(t.getRoom())).
				collect(Collectors.toSet());
	}

	public Set<Tile> reachable(Tile pointOfView, int minDistance, int maxDistance) {
	    Set<Tile> totalTiles = tiles.stream().
				flatMap(List::stream).filter(t -> t != null).collect(Collectors.toSet());
		List<Set<Tile>> reachableTiles= new ArrayList<>();
		Set<Tile> tempTiles = new HashSet<>();
        tempTiles.add(pointOfView);
		Set<Tile> currTile = new HashSet<>();
		tempTiles.forEach(currTile::add);
		reachableTiles.add(new HashSet<>(currTile));
		for (int i = 0; i < maxDistance; i++) {
			for (Tile t1 : totalTiles) {
				for (Tile t2 : currTile)
					if (isLinked(t1, t2))
						tempTiles.add(t1);
			}
			currTile = new HashSet<>();
			tempTiles.forEach(currTile::add);
			reachableTiles.add(currTile);
		}
		if (minDistance > 0)
		    reachableTiles.get(maxDistance).removeAll(reachableTiles.get(minDistance-1));
		return reachableTiles.get(maxDistance);
	}
    @Override
    public String toString() {
	    //TODO print string with format 3x3 each, 9 spaces means to have space for 5 players, 1 color, 3 ammos
		return "";
    }

    public Tile getTile(int posy, int posx) {
	    return tiles.get(posy).get(posx);
    }

    public List<List<Tile>> getTiles() {
		return tiles;
	}

	public int getSkulls() {
		return skulls;
	}

	public List<Player> getKillShotTrack() {
		return killShotTrack;
	}

	public void setKillShotTrack(List<Player> killShotTrack) {
		this.killShotTrack = killShotTrack;
	}
}