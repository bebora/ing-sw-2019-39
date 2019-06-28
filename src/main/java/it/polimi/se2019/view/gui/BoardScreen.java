package it.polimi.se2019.view.gui;
import it.polimi.se2019.controller.ReceivingType;
import it.polimi.se2019.view.*;
import javafx.collections.FXCollections;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class BoardScreen extends HBox {
    BoardFX boardFX;
    PlayerBoardFX clientPlayer;
    ActionButtons actionButtons;
    PowerUpsBox powerUpsBox;
    WeaponsBox weaponsBox;
    VBox playerBoardZone;
    VBox playerBoardBox;
    DominationBoardFX dominationBoardFX;
    ScrollPane playerBoardScroller;
    SelectableOptionsWrapper selectableOptionsWrapper;
    SelectableOptionsWrapper oldSelectable;
    View guiView;
    ListView<String> messageBox;
    ChoiceDialog<String> ammoChoice;

    BoardScreen(View GUIView){
        guiView = GUIView;
        boardFX = new BoardFX();
        boardFX.setEventUpdater(GUIView.getEventUpdater());
        boardFX.setBoard(GUIView.getBoard());
        playerBoardZone = new VBox();
        playerBoardBox = new VBox();
        playerBoardScroller = new ScrollPane();
        playerBoardScroller.setPannable(true);
        playerBoardScroller.setPrefSize(492,240);
        playerBoardScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        playerBoardScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox boardZone = new VBox();
        boardZone.setSpacing(10);
        actionButtons = new ActionButtons(GUIView.getEventUpdater());
        powerUpsBox = new PowerUpsBox(GUIView.getEventUpdater(),actionButtons.getSenderButton());
        boardFX.setSenderButton(actionButtons.getSenderButton());
        weaponsBox = new WeaponsBox(GUIView.getEventUpdater());
        messageBox = new ListView<>();
        ScrollPane messageScroll = new ScrollPane(messageBox);
        messageScroll.setPrefSize(200,100);
        messageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messageScroll.setPannable(true);
        messageScroll.setFitToWidth(true);
        boardZone.getChildren().addAll(boardFX,actionButtons,messageScroll);
        clientPlayer = new PlayerBoardFX(guiView.getSelf().getUsername());
        for(ViewPlayer p: GUIView.getPlayers()) {
            if (!p.getDominationSpawn() && !p.getUsername().equals(GUIView.getSelf().getUsername())) {
                PlayerBoardFX temp = new PlayerBoardFX(p.getUsername());
                temp.updatePlayerInfo(p);
                playerBoardBox.getChildren().addAll(temp);
            }
        }
        playerBoardScroller.setContent(playerBoardBox);
        if(GUIView.getGameMode().equals("DOMINATION")) {
            dominationBoardFX = new DominationBoardFX();
            boardFX.setDominationPane(dominationBoardFX);
        }
        playerBoardZone.getChildren().addAll(clientPlayer,playerBoardScroller,powerUpsBox,weaponsBox);
        playerBoardZone.setSpacing(15);
        updateBoard(GUIView.getBoard(),GUIView.getPlayers());
        this.getChildren().addAll(boardZone,playerBoardZone);
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        Scale scale = new Scale();
        scale.setPivotX(0);
        scale.setPivotY(0);
        scale.setX(primaryScreenBounds.getMaxX()/(boardFX.getPrefWidth() + clientPlayer.getPrefWidth()));
        scale.setY(primaryScreenBounds.getMaxY()/(boardFX.getPrefHeight() + clientPlayer.getPrefHeight()));
        this.getTransforms().addAll(scale);
        this.setStyle("-fx-background-color: black");
    }

    void updateBoard(ViewBoard viewBoard,List<ViewPlayer> players){
        boardFX.updateBoard(viewBoard);
        boardFX.clearPlayers();
        boardFX.drawPlayers(players);
        boardFX.drawAmmoCard();
        boardFX.setYellowWeapons(getWeaponsFromColor("YELLOW",viewBoard));
        boardFX.setBlueWeapons(getWeaponsFromColor("BLUE",viewBoard));
        boardFX.setRedWeapons(getWeaponsFromColor("RED",viewBoard));
        int i = 0;
        for(ViewPlayer p: players){
            if (updatePlayer(p,i))
                i++;
        }
        List<ViewPlayer> dominationPlayers = players.stream()
                .filter(ViewPlayer::getDominationSpawn)
                .collect(Collectors.toList());
        if(guiView.getGameMode().equals("DOMINATION")) {
            dominationBoardFX.updateSkulls(viewBoard.getSkulls());
            dominationBoardFX.updateSpawns(dominationPlayers);
        }
    }

    private boolean updatePlayer(ViewPlayer p,int i){
        if(!p.getDominationSpawn()){
            if(!p.getUsername().equals(guiView.getSelf().getUsername())){
                PlayerBoardFX playerBoardFX = (PlayerBoardFX)playerBoardBox.getChildren().get(i);
                playerBoardFX.updatePlayerInfo(p);
                if(p == guiView.getCurrentPlayer())
                    GuiHelper.applyBorder(playerBoardFX,50);
                else
                    playerBoardFX.setEffect(null);
                return true;
            }else {
                clientPlayer.updatePlayerInfo(p);
                if(p == guiView.getCurrentPlayer())
                    GuiHelper.applyBorder(clientPlayer,50);
                else
                    clientPlayer.setEffect(null);
            }
        }
        return false;
    }

    void setSelectableOptionsWrapper(SelectableOptionsWrapper selectableOptionsWrapper) {
        if(this.selectableOptionsWrapper != null)
            oldSelectable = this.selectableOptionsWrapper;
        this.selectableOptionsWrapper = selectableOptionsWrapper;
        actionButtons.clearPossibleActions();
        for(ReceivingType ac: selectableOptionsWrapper.getAcceptedTypes()){
            switch (ac){
                case ACTION:
                    actionButtons.setPossibleActions(selectableOptionsWrapper.getSelectableActions().getOptions());
                    break;
                case TILES:
                    boardFX.setSelectableOptionsWrapper(selectableOptionsWrapper);
                    boardFX.showPossibleTiles(selectableOptionsWrapper.getSelectableTileCoords().getOptions());
                    break;
                case POWERUP:
                    powerUpsBox.highlightSelectablePowerUps(selectableOptionsWrapper.getSelectablePowerUps());
                    break;
                case WEAPON:
                    boardFX.setSelectableOptionsWrapper(selectableOptionsWrapper);
                    weaponsBox.highlightSelectableWeapons(selectableOptionsWrapper.getSelectableWeapons());
                    break;
                case PLAYERS:
                    boardFX.setSelectableOptionsWrapper(selectableOptionsWrapper);
                    break;
                case AMMO:
                    if(oldSelectable != null && !oldSelectable.getAcceptedTypes().contains(ReceivingType.AMMO))
                        showAmmoChoice(selectableOptionsWrapper.getSelectableAmmos().getOptions());
                    break;
                case ROOM:
                    boardFX.showPossibleRooms(selectableOptionsWrapper.getSelectableRooms().getOptions());
                    break;
                case DIRECTION:
                    boardFX.showPossibleDirections(selectableOptionsWrapper.getSelectableDirections().getOptions());
                    break;
                case EFFECT:
                    weaponsBox.setSelectableEffects(selectableOptionsWrapper.getSelectableEffects());
                    break;
                case STOP:
                    actionButtons.enableStop();
                    break;
                default:
                    break;
            }
        }
    }

    private List<String> getWeaponsFromColor(String color, ViewBoard viewBoard){
        return viewBoard.getTiles().stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(ViewTile::isSpawn)
                .filter(t->t.getRoom().equals(color))
                .map(ViewTile::getWeapons)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    void updatePowerUps(List<ViewPowerUp> viewPowerUps){
        powerUpsBox.setPowerUps(viewPowerUps);
    }

    void updateWeapons(List<ViewWeapon> weapons){
        List<String> weaponsNames = weapons.stream().map(ViewWeapon::getName).collect(Collectors.toList());
        weaponsBox.setWeapons(weaponsNames);
    }

    void updateMessages(List<String> messages){
        messageBox.setItems(FXCollections.observableList(messages));
    }

    private void showAmmoChoice(List<String> ammoChoiceOptions){
        ammoChoice = new ChoiceDialog<>(null, ammoChoiceOptions);
        ammoChoice.setContentText("Selectable ammos:");
        ammoChoice.setHeaderText("Select an ammo. If you prefer paying with a powerUp close this message and click on it:");
        Optional<String> ammo = ammoChoice.showAndWait();
        ammo.ifPresent(a->guiView.getEventUpdater().sendAmmo(a));
    }

}
