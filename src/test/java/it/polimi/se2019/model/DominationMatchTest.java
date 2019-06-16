package it.polimi.se2019.model;

import it.polimi.se2019.model.board.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DominationMatchTest {
    private DominationMatch match;
    private Player foo, poo, boo;
    private List<Player> spawnerPlayers;

    private void insertSpawnPoints() {
        int limit = match.getPlayers().size();
        for (int i = 0; i < limit; i++) {
            match.newTurn();
        }
    }
    @BeforeEach
    void prepareMatch() {
        foo = new Player();
        poo = new Player();
        boo = new Player();
        match = new DominationMatch(new ArrayList<>(Arrays.asList(foo,poo,boo)), "board1.btlb", 8);
        spawnerPlayers = match.getSpawnPoints();
    }

    @Test
    void insertSpawnPointsTest() {
        assertEquals(0, spawnerPlayers.size());
        insertSpawnPoints();
        spawnerPlayers = match.getSpawnPoints();
        assertEquals(3, spawnerPlayers.size());
        assertTrue(spawnerPlayers.stream().allMatch(p -> p.getDominationSpawn() && p.getUsername().equals(p.getColor().toString())));
    }

    @Test
    void checkPlayerDamaged() {
        insertSpawnPoints();
        spawnerPlayers = match.getSpawnPoints();
        Tile destTile = spawnerPlayers.get(match.getCurrentPlayer()).getTile();
        Player current = match.getPlayers().get(match.getCurrentPlayer());
        current.setTile(destTile);
        match.newTurn();
        assertEquals(1, current.getDamagesCount());
        assertEquals(current, current.getDamages().get(0));
    }

    @Test
    void checkSpawnDamaged() {
        insertSpawnPoints();
        //SpawnPoints have been generated after first round
        spawnerPlayers = match.getSpawnPoints();
        Tile destTile = spawnerPlayers.get(0).getTile();
        Tile otherTile = spawnerPlayers.get(1).getTile();
        Player current = match.getPlayers().get(match.getCurrentPlayer());
        //Move current player to spawn alone
        current.setTile(destTile);
        //Set other players to other spawn tile
        match.getPlayers().stream().filter(p -> !p.getDominationSpawn() && !p.equals(current)).forEach(p -> p.setTile(otherTile));
        match.newTurn();
        //A player alone in a spawn tile should give it one damage
        assertEquals(1, spawnerPlayers.get(0).getDamagesCount());
        //Other spawn should not have any damage
        assertEquals(0, spawnerPlayers.get(1).getDamagesCount());
        //Current player will be one of the other two, so now we can go to the next turn and see if the other spawn has been damaged
        match.newTurn();
        assertEquals(0, spawnerPlayers.get(1).getDamagesCount());
        Player newCurrent = match.getPlayers().get(match.getCurrentPlayer());
        //Move all player except the current one to another tile and check damage received by spawn
        match.getPlayers().stream().filter(p -> !p.getDominationSpawn() && !p.equals(newCurrent)).forEach(p -> p.setTile(destTile));
        spawnerPlayers.get(1).receiveShot(newCurrent, 4, 4, true);
        assertEquals(1, spawnerPlayers.get(1).getDamagesCount());
        spawnerPlayers.get(1).receiveShot(newCurrent, 4, 4, true);
        assertEquals(1, spawnerPlayers.get(1).getDamagesCount());
        match.newTurn();
        assertEquals(2, spawnerPlayers.get(1).getDamagesCount());
    }

    @Test
    void checkPointsGivenFromTracks() {
        //Setup 5 player match
        foo = new Player();
        poo = new Player();
        boo = new Player();
        Player doo = new Player();
        Player moo = new Player();
        match = new DominationMatch(new ArrayList<>(Arrays.asList(foo, poo, boo, doo, moo)), "board1.btlb", 8);
        insertSpawnPoints();
        Player spawn =  match.getSpawnPoints().get(0);
        spawn.getDamages().addAll(Collections.nCopies(3, foo));
        spawn.getDamages().addAll(Collections.nCopies(2, poo));
        spawn.getDamages().addAll(Collections.nCopies(2, boo));
        spawn.getDamages().addAll(Collections.nCopies(1, doo));
        spawn.getDamages().addAll(Collections.nCopies(1, moo));
        match.scoreSpawnPoint(spawn);
        assertEquals(8, foo.getPoints());
        assertEquals(6, poo.getPoints());
        assertEquals(6, boo.getPoints());
        assertEquals(2, doo.getPoints());
        assertEquals(2, moo.getPoints());
    }
}