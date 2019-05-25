package it.polimi.se2019.view.cli;

import it.polimi.se2019.Logger;
import it.polimi.se2019.Priority;
import it.polimi.se2019.model.board.Color;
import it.polimi.se2019.network.EventUpdater;
import it.polimi.se2019.view.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CliInputHandler implements Runnable{
    private BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    private String in;
    private CLI view;
    private EventUpdater eventUpdater;
    private static final String wrongInputMessage = "Wrong input!";

    private void tileInfoMode(BufferedReader input){
        String in = "InitialValue";
        String[] inSplit;
        int requestedX;
        int requestedY;
        CLI.moveCursor(1,AsciiBoard.boardBottomBorder+6);
        while(!in.equals("q")){
            try{
                in = input.readLine();
            }catch (IOException e){
                Logger.log(Priority.ERROR,"Failed to read stdin");
            }
            CLI.clearUntilEndOfLine(AsciiBoard.boardBottomBorder+6,AsciiBoard.boardBottomBorder+8,1);
            if(in.matches("^\\d+(,\\d+)")){
                inSplit = in.split(",");
                CLI.moveCursor(1, AsciiBoard.boardBottomBorder+6);
                CLI.cleanRow();
                requestedX = Math.abs(Integer.parseInt(inSplit[0]));
                requestedY = Math.abs(Integer.parseInt(inSplit[1]));
                AsciiBoard.requestTileInfo(requestedX,requestedY);
            }
            CLI.moveCursor(1, AsciiBoard.boardBottomBorder + 6);
            CLI.cleanRow();
        }
    }

    private void infoWeapon(int i){
        if(i < AsciiWeapon.displayedWeapons.size()){
            AsciiWeapon.drawWeaponInfo(i);
        }else{
            CLI.printMessage("The selected weapon does not exist!","R");
        }
    }

    private void parseSelection(String[] inSplit){
        String[] selectedElements = new String[inSplit.length];
        System.arraycopy(inSplit,2,selectedElements,0,inSplit.length);
        switch(inSplit[1]){
            case "PLAYER":
                parsePlayers(selectedElements);
                break;
            case "TILE":
                parseTiles(selectedElements);
                break;
            case "ROOM":
                parseRoom(inSplit[2]);
                break;
            case "WEAPON":
                eventUpdater.sendWeapon(inSplit[2]);
                break;
            case "DIRECTION":
                parseDirection(inSplit[2]);
                break;
            case "ACTION":
                parseAction(inSplit[2]);
                break;
            case "POWERUP":
                parsePowerUps(selectedElements);
                break;
            default:
                CLI.printMessage("This is not something you can select!","R");
                break;
        }
    }

    private void parsePlayers(String[] players){
        List<String> selectedViewPlayers = new ArrayList<>();
        boolean error = false;
        for(String p: players){
            ViewPlayer viewP = view.getPlayers().stream()
                    .filter(viewPlayer -> viewPlayer.getColor().equals(p))
                    .findAny().orElse(null);
            if(viewP != null){
                selectedViewPlayers.add(viewP.getId());
            }else {
                CLI.printMessage(wrongInputMessage, "R");
                error=true;
                break;
            }
        }
        if(!error)
            eventUpdater.sendPlayers(selectedViewPlayers);
    }

    private void parseTiles(String[] tiles){
        List<ViewTileCoords> selectedCoords = new ArrayList<>();
        int x;
        int y;
        boolean error = false;
        for(String tile: tiles){
            if(tile.matches(".+,.+")) {
                String[] coords = tile.split(",");
                x = Integer.parseInt(coords[0]);
                y = Integer.parseInt(coords[1]);
                selectedCoords.add(new ViewTileCoords(x,y));
            }else
            {
                CLI.printMessage(wrongInputMessage,"R");
                error = true;
                break;
            }
        }
        if(!error)
            eventUpdater.sendTiles(selectedCoords);
    }

    private void parseRoom(String room){
        if(Color.initialToColor(room.charAt(0)) != null)
            eventUpdater.sendRoom(room);
        else
            CLI.printMessage("Wrong input","R");
    }

    private void parseDirection(String direction){
        List<String> possibleDirections = Arrays.asList("NORTH", "SOUTH", "WEST", "EAST");
        if(possibleDirections.contains(direction))
            eventUpdater.sendDirection(direction);
        else
            CLI.printMessage(wrongInputMessage,"R");
    }

    private void parseAction(String action){
        List<String> actions = view.getSelf().getActions().stream()
                .map(ViewAction::getType)
                .collect(Collectors.toList());
        if(actions.contains(action))
            eventUpdater.sendAction(action);
        else
            CLI.printMessage(wrongInputMessage, "R");
    }

    private void parsePowerUps(String[] selection){
        List<ViewPowerUp> selectedPowerUps = new ArrayList<>();
        String[] powerUps = new String[selection.length - 1];
        boolean discard = Boolean.parseBoolean(powerUps[0]);
        boolean error = false;
        System.arraycopy(selection,1,powerUps,0,selection.length - 1);
        for(String p: powerUps){
            ViewPowerUp singlePowerUp = view.getPowerUps().stream()
                    .filter(viewPowerUp -> viewPowerUp.getName().equals(p))
                    .findAny().orElse(null);
            if(singlePowerUp != null){
                selectedPowerUps.add(singlePowerUp);
            }else{
                CLI.printMessage(wrongInputMessage, "R");
                error = true;
                break;
            }
        }
        if(!error){
            eventUpdater.sendPowerUp(selectedPowerUps,discard);
        }
    }

    private void lobbyInput(BufferedReader input){

    }
    public void run(){
        while(!in.equals("quit")){
            try{
                in = input.readLine();
            }catch(IOException e){
                Logger.log(Priority.ERROR,"Can't read from stdin");
            }
            if(in.matches(".+\\s.+")){
                in = in.toUpperCase();
                String[] inSplit = in.split("\\s");
                switch (inSplit[0]){
                    case "SELECT":
                        parseSelection(inSplit);
                        break;
                    case "TILE":
                        tileInfoMode(input);
                        break;
                    case "WEAPON":
                        if(inSplit.length > 2 && inSplit[2].matches("\\d"))
                            infoWeapon(Integer.parseInt(inSplit[2]));
                        else
                            CLI.printMessage("Wrong format", "R");
                        break;
                    default:
                        AsciiBoard.drawBoard(view.getPlayers());
                        break;
                }
            }
        }
    }
}