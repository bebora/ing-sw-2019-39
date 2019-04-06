package model;

import static model.ThreeState.OPTIONAL;

public class Target {


	public static class Builder {
		ThreeState visibility = OPTIONAL;
		int maxTargets = -1;
		int minDistance = 0;
		int maxDistance = -1;
		Area areaDamage = Area.SINGLE;
		ThreeState cardinal = OPTIONAL;
		ThreeState checkTargetList = OPTIONAL;
		ThreeState differentSquare = OPTIONAL;
		ThreeState samePlayerRoom = OPTIONAL;
		ThreeState throughWalls = OPTIONAL;
		PointOfView pointOfView = PointOfView.OWN;
		ThreeState checkBlackList = OPTIONAL;

		public Builder setVisibility(ThreeState visibility) {
			this.visibility = visibility;
			return this;
		}

		public Builder setMaxTargets(int maxTargets) {
			this.maxTargets = maxTargets;
			return this;
		}

		public Builder setMinDistance(int minDistance) {
			this.minDistance = minDistance;
			return this;
		}

		public Builder setMaxDistance(int maxDistance) {
			this.maxDistance = maxDistance;
			return this;
		}

		public Builder setAreaDamage(Area areaDamage) {
			this.areaDamage = areaDamage;
			return this;
		}

		public Builder setCardinal(ThreeState cardinal) {
			this.cardinal = cardinal;
			return this;
		}

		public Builder setCheckTargetList(ThreeState checkTargetList) {
			this.checkTargetList = checkTargetList;
			return this;
		}

		public Builder setDifferentSquare(ThreeState differentSquare) {
			this.differentSquare = differentSquare;
			return this;
		}

		public Builder setSamePlayerRoom(ThreeState samePlayerRoom) {
			this.samePlayerRoom = samePlayerRoom;
			return this;
		}

		public Builder setThroughWalls(ThreeState throughWalls) {
			this.throughWalls = throughWalls;
			return this;
		}

		public Builder setPointOfView(PointOfView pointOfView) {
			this.pointOfView = pointOfView;
			return this;
		}

		public Builder setCheckBlackList(ThreeState checkBlackList) {
			this.checkBlackList = checkBlackList;
			return this;
		}
		public Target build() {
			return new Target(this);
		}
	}

	public Target(Builder builder) {
		this.visibility = builder.visibility;
		this.maxDistance = builder.maxDistance;
		this.minDistance = builder.minDistance;
		this.maxTargets = builder.maxTargets;
		this.areaDamage = builder.areaDamage;
		this.cardinal = builder.cardinal;
		this.checkTargetList = builder.checkTargetList;
		this.checkBlackList = builder.checkBlackList;
		this.differentSquare = builder.differentSquare;
		this.samePlayerRoom = builder.samePlayerRoom;
		this.throughWalls = builder.throughWalls;
		this. pointOfView = builder.pointOfView;
	}

	/**
	 * TRUE: target must be visible from POV
	 * FALSE: target must not be visible from POV
	 * OPTIONAL: target can be anywhere
	 */
	private ThreeState visibility;

	/**
	 * How many players can be selected
	 */
	private int maxTargets;

	/**
	 * Minimum distance from POV
	 * Ignored if -1
	 */
	private int minDistance;

	/**
	 * Maximum distance from POV
	 * Ignored if -1
	 */
	private int maxDistance;


	/**
	 * Select the Area of the damage:
	 * SINGLE: all the targets (to maxtargets) get selected
	 * TILE: all the targets in the selected tile
	 * ROOM: all the target in the selected room get selected
	 */
	private Area areaDamage;

	/**
	 * TRUE: target must be in same xcord or ycord of POV
	 * FALSE: target must not be in same xcord or ycord of POV
	 * OPTIONAL: not relevant
	 */
	private ThreeState cardinal;

	/**
	 * If used in DealDamage:
	 * TRUE: targets must be in tar getPlayers
	 * FALSE: targets must not be in targetPlayers
	 * OPTIONAL: not relevant
	 *
	 * If used in Move:
	 * TRUE: targets are the last $maxTargets of targetPlayers
	 * FALSE: targets must not be in targetPlayers
	 * OPTIONAL: not relevant
	 *
	 * In PowerUp:
	 * TRUE: target is the Player in player
	 * FALSE: target must not be the Player in player
	 * OPTIONAL: not relevant
	 */
	private ThreeState checkTargetList;

	/**
	 * If used in DealDamage:
	 * TRUE: targets must be in blackListPlayers
	 * FALSE: targets must not be in blackListPlayers
	 * OPTIONAL: not relevant
	 *
	 * If used in Move:
	 * TRUE: targets are the last $maxTargets of blackListPlayers
	 * FALSE: targets must not be in blackListPlayers
	 * OPTIONAL: not relevant
	 *
	 * In PowerUp, not relevant
	 */
	private ThreeState checkBlackList;

	/**
	 * TRUE: targets must be in different Tile
	 * FALSE: targets must not be in different Tiles
	 * OPTIONAL: not relevant
	 */
	private ThreeState differentSquare;

	/**
	 * TRUE: targets must be in the same room of the POV
	 * FALSE: targets must not be in the same room of the POV
	 * OPTIONAL: not relevant
	 */
	private ThreeState samePlayerRoom;

	/**
	 * TRUE: 
	 * FALSE: 
	 * OPTIONAL: not relevant
	 */
	private ThreeState throughWalls;

	/**
	 * Point of view from where the matching targets are selected
	 */
	private PointOfView pointOfView;


	public ThreeState getVisibility() {
		return visibility;
	}

	public void setVisibility(ThreeState visibility) {
		this.visibility = visibility;
	}

	public int getMaxTargets() {
		return maxTargets;
	}

	public void setMaxTargets(int maxTargets) {
		this.maxTargets = maxTargets;
	}

	public int getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(int minDistance) {
		this.minDistance = minDistance;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(int maxDistance) {
		this.maxDistance = maxDistance;
	}

	public Area getAreaDamage() {
		return areaDamage;
	}

	public void setAreaDamage(Area areaDamage) {
		this.areaDamage = areaDamage;
	}

	public ThreeState getCardinal() {
		return cardinal;
	}

	public void setCardinal(ThreeState cardinal) {
		this.cardinal = cardinal;
	}

	public ThreeState getCheckTargetList() {
		return checkTargetList;
	}

	public void setCheckTargetList(ThreeState checkTargetList) {
		this.checkTargetList = checkTargetList;
	}

	public ThreeState getDifferentSquare() {
		return differentSquare;
	}

	public void setDifferentSquare(ThreeState differentSquare) {
		this.differentSquare = differentSquare;
	}

	public ThreeState getSamePlayerRoom() {
		return samePlayerRoom;
	}

	public void setSamePlayerRoom(ThreeState samePlayerRoom) {
		this.samePlayerRoom = samePlayerRoom;
	}

	public ThreeState getThroughWalls() {
		return throughWalls;
	}

	public void setThroughWalls(ThreeState throughWalls) {
		this.throughWalls = throughWalls;
	}

	public PointOfView getPointOfView() {
		return pointOfView;
	}

	public void setPointOfView(PointOfView pointOfView) {
		this.pointOfView = pointOfView;
	}

	public ThreeState getCheckBlackList() {
		return checkBlackList;
	}

	public void setCheckBlackList(ThreeState checkBlackList) {
		this.checkBlackList = checkBlackList;
	}




}