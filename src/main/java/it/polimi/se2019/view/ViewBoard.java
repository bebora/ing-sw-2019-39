package it.polimi.se2019.view;
import java.util.*;

public class ViewBoard {

	public ViewBoard() {
	}

	private List<List<ViewTile>> tiles;

	private List<String> killShotTrack;

	private List<ViewDoor> doors;

	private int skulls;

	public List<List<ViewTile>> getTiles() {
		return tiles;
	}
}