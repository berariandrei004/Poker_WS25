package at.ac.hcw.UI;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class PokerTableView implements ServerMessageListener{
    private ImageView h1;
    private ImageView h2;
    private ImageView com1;
    private ImageView com2;
    private ImageView com3;
    private ImageView com4;
    private ImageView com5;
    private Pane animationLayer;
    private ImageView deckImage;
    private StackPane overlay;
    private VBox helpWindow;
    private Button foldBtn;
    private Button checkBtn;
    private Button raiseBtn;
    private Button allInBtn;


    public Parent createView() {
        PokerClient client = App.getSceneController().getClient();
        client.setOnNewRound(() -> {
            Platform.runLater(this::onNewRound);
        });

        client.setOnFlop(cards ->
                Platform.runLater(() -> showFlop(cards))
        );

        client.setOnTurn(card ->
                Platform.runLater(() -> showTurn(card))
        );

        client.setOnRiver(card ->
                Platform.runLater(() -> showRiver(card))
        );
        BorderPane root = new BorderPane();
        animationLayer = new Pane();
        animationLayer.setPickOnBounds(false);
        deckImage = new ImageView(
                new Image(getClass().getResourceAsStream("/cards/backside.jpg"))
        );

        root.setStyle("-fx-background-color: darkgreen;");

        //Community Cards
        HBox communityCards = new HBox(10);
        communityCards.setAlignment(Pos.CENTER);

        Image back = new Image(getClass().getResourceAsStream("/cards/backside.jpg"));

        com1 = new ImageView(back);
        com2 = new ImageView(back);
        com3 = new ImageView(back);
        com4 = new ImageView(back);
        com5 = new ImageView(back);

        for (ImageView c : new ImageView[]{com1, com2, com3, com4, com5}) {
            c.setFitWidth(90);
            c.setFitHeight(130);

            c.setImage(back);
            c.setVisible(false);
        }

        communityCards.getChildren().addAll(com1, com2, com3, com4, com5);

        //Pot
        Label potLabel = new Label("Pot: 0");
        potLabel.setStyle("-fx-font-size: 18; -fx-text-fill: white;");

        //player top
        VBox topPlayer = createPlayerBox(
                "Player 2", 1500, false,
                null, null
        );


        //player left
        VBox leftPlayer = createPlayerBox(
                "Player 3", 1200, false,
                null, null
        );

        ((Circle) leftPlayer.getChildren().get(0)).setVisible(true);
        setDealer(leftPlayer);

        //player right
        VBox rightPlayer = createPlayerBox(
                "Player 4", 1800, false,
                null, null
        );

        List<VBox> players = Arrays.asList(
                leftPlayer,
                topPlayer,
                rightPlayer
        );

        //Player hand cards
        HBox playerHand = new HBox(10);
        playerHand.setAlignment(Pos.CENTER);

        h1 = new ImageView(back);
        h2 = new ImageView(back);

        h1.setFitWidth(90);
        h1.setFitHeight(130);
        h1.setPreserveRatio(true);

        h2.setFitWidth(90);
        h2.setFitHeight(130);
        h2.setPreserveRatio(true);

        h1.setVisible(false);
        h2.setVisible(false);

        playerHand.getChildren().addAll(h1, h2);

        //Placeholder for cards
        HBox leftCards = (HBox) leftPlayer.getChildren().get(6);
        HBox topCards = (HBox) topPlayer.getChildren().get(6);
        HBox rightCards = (HBox) rightPlayer.getChildren().get(6);

        ImageView left1 = (ImageView) leftCards.getChildren().get(0);
        ImageView left2 = (ImageView) leftCards.getChildren().get(1);

        ImageView top1 = (ImageView) topCards.getChildren().get(0);
        ImageView top2 = (ImageView) topCards.getChildren().get(1);

        ImageView right1 = (ImageView) rightCards.getChildren().get(0);
        ImageView right2 = (ImageView) rightCards.getChildren().get(1);

        //Buttons
        VBox rightControlBox = new VBox(12);
        rightControlBox.setAlignment(Pos.BOTTOM_RIGHT);
        rightControlBox.setStyle(
                "-fx-background-color: rgba(0,0,0,0.35);" +
                        "-fx-padding: 12;" +
                        "-fx-background-radius: 15;"
        );

        foldBtn = new Button("Fold");
        checkBtn = new Button("Check / Call");
        raiseBtn = new Button("Raise");
        allInBtn = new Button("All-In");

        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_RIGHT);
        row1.getChildren().addAll(foldBtn, checkBtn);

        HBox row2 = new HBox(10);
        row2.setAlignment(Pos.CENTER_RIGHT);
        row2.getChildren().addAll(raiseBtn, allInBtn);

        for (Button b : new Button[]{foldBtn, checkBtn, raiseBtn, allInBtn}) {
            b.setMinWidth(120);
            b.setMinHeight(38);
            b.setStyle(
                    "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-color: linear-gradient(#ffffff, #cccccc);" +
                            "-fx-border-color: #222;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 10;"
            );
        }

        //Raise Slider
        Slider raiseSlider = new Slider(0, 500, 0);
        raiseSlider.setShowTickLabels(true);
        raiseSlider.setShowTickMarks(true);
        raiseSlider.setMajorTickUnit(50);
        raiseSlider.setMinorTickCount(5);
        raiseSlider.setBlockIncrement(10);

        Label raiseText = new Label("Raise auswählen:");
        raiseText.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        VBox raiseBox = new VBox(5);
        raiseBox.setAlignment(Pos.CENTER_RIGHT);
        raiseBox.getChildren().addAll(raiseText, raiseSlider);

        Label betLabel = new Label("Bet: 0");

        raiseSlider.valueProperty().addListener((obs, oldV, newV) ->
                betLabel.setText("Bet: " + newV.intValue())
        );

        rightControlBox.getChildren().addAll(row1, row2, raiseBox);
        BorderPane bottomArea = new BorderPane();
        bottomArea.setRight(rightControlBox);

        BorderPane bottom = new BorderPane();
        bottom.setCenter(playerHand);
        root.setRight(rightControlBox);

        root.setBottom(bottom);

        //Ovaler Tisch
        StackPane tableArea = new StackPane();
        tableArea.setAlignment(Pos.CENTER);

        //Tisch
        Region ovalTable = new Region();
        ovalTable.setPrefSize(700, 350);
        ovalTable.setStyle(
                "-fx-background-color: #0b6623;" +
                        "-fx-background-radius: 200;" +
                        "-fx-border-color: #2b2b2b;" +
                        "-fx-border-width: 6;" +
                        "-fx-border-radius: 200;" +
                        "-fx-effect: dropshadow(gaussian, black, 35, 0.6, 0, 0);"
        );

        //Deck
        ImageView deckImage = new ImageView(
                new Image(getClass().getResourceAsStream("/cards/backside.jpg"))
        );
        deckImage.setFitWidth(80);
        deckImage.setFitHeight(120);

        //deck-pos
        HBox boardRow = new HBox(25);
        boardRow.setAlignment(Pos.CENTER);
        boardRow.getChildren().addAll(communityCards, deckImage);

        //Pot
        VBox tableContent = new VBox(15);
        tableContent.setAlignment(Pos.CENTER);
        tableContent.getChildren().setAll(boardRow, potLabel);

        //forge
        tableArea.getChildren().addAll(ovalTable, tableContent);

        BorderPane table = new BorderPane();
        table.setCenter(tableArea);
        table.setLeft(leftPlayer);
        table.setRight(rightPlayer);
        table.setTop(topPlayer);
        table.setBottom(playerHand);

        root.setCenter(table);

        //Menu Button
        Button menuButton = new Button("=");
        menuButton.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        BorderPane topBar = new BorderPane();
        topBar.setRight(menuButton);
        topBar.setPadding(new Insets(10));

        root.setTop(topBar);

        tableArea.getChildren().setAll(ovalTable, tableContent);

        foldBtn.setOnAction(e -> {
            client.sendMessage("FOLD");
        });
        checkBtn.setOnAction(e -> {
            client.sendMessage("CHECK");
        });
        raiseBtn.setOnAction(e -> {
            int amount = (int) raiseSlider.getValue();
            client.sendMessage("RAISE " + amount);
        });
        allInBtn.setOnAction(e -> {
            client.sendMessage("ALLIN");
        });


        //Scene
        animationLayer.setPickOnBounds(false);

        StackPane rootStack = new StackPane(root, animationLayer);

        animationLayer.prefWidthProperty().bind(rootStack.widthProperty());
        animationLayer.prefHeightProperty().bind(rootStack.heightProperty());

        overlay = new StackPane();
        overlay.setVisible(false);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        VBox popup = new VBox(20);
        popup.setAlignment(Pos.CENTER);
        popup.setPrefSize(400, 250);
        popup.setStyle("-fx-background-color: lightgray; -fx-background-radius: 15;");

        Button helpButton = new Button("Hilfe");
        helpButton.setMinWidth(200);

        helpButton.setOnAction(e -> helpWindow.setVisible(true));

        Button exitButton = new Button("Spiel verlasen");
        exitButton.setMinWidth(200);

        popup.getChildren().addAll(helpButton, exitButton);
        overlay.getChildren().add(popup);

        rootStack.getChildren().add(overlay);

        menuButton.setOnAction(e -> overlay.setVisible(true));

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                helpWindow.setVisible(false);
                overlay.setVisible(false);
            }
        });

        exitButton.setOnAction(e -> {
            //Platform.exit();
            try {
                onExitGameClicked();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });


        helpWindow = new VBox(15);
        helpWindow.setAlignment(Pos.TOP_CENTER);
        helpWindow.setPrefSize(520, 420);
        helpWindow.setPadding(new Insets(18));
        helpWindow.setVisible(false);
        helpWindow.setStyle(
                "-fx-background-color: rgba(30,30,30,0.92)" +
                        "-fx-background-radius: 18;" +
                        "-fx-effect: dropshadow(gaussain, black, 25, 0.5, 0, 0);"
        );

        Label title = new Label("Hilfe & Pokerhände");
        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: white;");

        Image helpImage = new Image(
                getClass().getResourceAsStream("/pokerhands/poker_hands.png")
        );

        ImageView imageView = new ImageView(helpImage);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(460);
        imageView.setSmooth(true);

        VBox contentBox = new VBox(15);
        contentBox.setAlignment(Pos.TOP_CENTER);

        Label picTitle = new Label("Texas Hold'em - Händeübersicht");
        picTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16;");

        contentBox.getChildren().addAll(picTitle, imageView);

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        Button backButton = new Button("Zurück zum Spiel");
        backButton.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
        backButton.setOnAction(e -> hideHelp());

        helpWindow.getChildren().setAll(
                title,
                scroll,
                backButton
        );

        overlay.getChildren().add(helpWindow);

        //HIer waren stage.scene aufrufe

        //Dealer rotate
//        final int[] dealerIndex = {0};
//
//        int sbIndex = (dealerIndex[0] + 1) % players.size();
//        setSmallBlind(players.get(sbIndex));
//
//        int bbIndex = (dealerIndex[0] + 2) % players.size();
//        setBigBlind(players.get(bbIndex));
//
//        //highlight current player
//        final int[] currentPlayer = {0};
//        highlightPlayer(players.get(currentPlayer[0]), true);
//        raise.setOnAction(e -> {
//            int delay = 0;
//
//            Timeline timeline = new Timeline(
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(left1, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(top1, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(right1, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(h1, deckImage, animationLayer)),
//
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(left2, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(top2, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(right2, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(h2, deckImage, animationLayer))
//            );
//
//            timeline.play();
//
//            timeline.setOnFinished(ev -> {
//
//                PauseTransition pause = new PauseTransition(Duration.millis(400));
//
//                pause.setOnFinished(e2 -> {
//
//                    String cardA = deck.draw();
//                    String cardB = deck.draw();
//
//                    Image real1 = new Image(getClass().getResourceAsStream("/cards/" + cardA));
//                    Image real2 = new Image(getClass().getResourceAsStream("/cards/" + cardB));
//
//                    flipCard(h1, real1);
//                    flipCard(h2, real2);
//
//                });
//
//                pause.play();
//            });
//        });
//
//        //test buttons
//        Button flop = new Button("Flop");
//        rightControlBox.getChildren().add(flop);
//        Button turn = new Button("Turn");
//        rightControlBox.getChildren().add(turn);
//        Button river = new Button("River");
//        rightControlBox.getChildren().add(river);
//        Button newRound = new Button("New round");
//        rightControlBox.getChildren().add(newRound);
//        Button collect = new Button("Collect Cards");
//        rightControlBox.getChildren().add(collect);
//        Button playHand = new Button("Play Hand");
//        rightControlBox.getChildren().add(playHand);
//
//        flop.setOnAction(e -> dealFlop(deckImage, animationLayer, com1, com2, com3));
//        turn.setOnAction(e -> dealTurn(deckImage, animationLayer, com4));
//        river.setOnAction(e -> dealRiver(deckImage, animationLayer, com5));
//        newRound.setOnAction(e -> startNewRound(
//                players,
//                com1, com2, com3, com4, com5
//        ));
//        collect.setOnAction(e ->
//                collectAllCardsToDeck(
//                        animationLayer,
//                        deckImage,
//                        new ImageView[]{com1, com2, com3, com4, com5},
//                        players,
//                        h1, h2
//                )
//        );
//        playHand.setOnAction(e -> playFullHand(
//                deckImage,
//                players,
//                com1, com2, com3, com4, com5
//        ));
//
//        setControlsEnabled(false);
//
       return rootStack;
       }
//    public void start(Stage stage) {
//        deck = new Deck();
//        BorderPane root = new BorderPane();
//        animationLayer = new Pane();
//        animationLayer.setPickOnBounds(false);
//        deckImage = new ImageView(
//                new Image(getClass().getResourceAsStream("/cards/backside.jpg"))
//        );
//
//        root.setStyle("-fx-background-color: darkgreen;");
//
//        //Community Cards
//        HBox communityCards = new HBox(10);
//        communityCards.setAlignment(Pos.CENTER);
//
//        Image back = new Image(getClass().getResourceAsStream("/cards/backside.jpg"));
//
//        ImageView com1 = new ImageView(back);
//        ImageView com2 = new ImageView(back);
//        ImageView com3 = new ImageView(back);
//        ImageView com4 = new ImageView(back);
//        ImageView com5 = new ImageView(back);
//
//        for (ImageView c : new ImageView[]{com1, com2, com3, com4, com5}) {
//            c.setFitWidth(90);
//            c.setFitHeight(130);
//
//            c.setImage(back);
//            c.setVisible(false);
//        }
//
//        communityCards.getChildren().addAll(com1, com2, com3, com4, com5);
//
//        //Pot
//        Label potLabel = new Label("Pot: 0");
//        potLabel.setStyle("-fx-font-size: 18; -fx-text-fill: white;");
//
//        //player top
//        VBox topPlayer = createPlayerBox(
//                "Player 2", 1500, false,
//                null, null
//        );
//
//
//        //player left
//        VBox leftPlayer = createPlayerBox(
//                "Player 3", 1200, false,
//                null, null
//        );
//
//        ((Circle) leftPlayer.getChildren().get(0)).setVisible(true);
//        setDealer(leftPlayer);
//
//        //player right
//        VBox rightPlayer = createPlayerBox(
//                "Player 4", 1800, false,
//                null, null
//        );
//
//        java.util.List<VBox> players = java.util.Arrays.asList(
//                leftPlayer,
//                topPlayer,
//                rightPlayer
//        );
//
//        //Player hand cards
//        HBox playerHand = new HBox(10);
//        playerHand.setAlignment(Pos.CENTER);
//
//        h1 = new ImageView(back);
//        h2 = new ImageView(back);
//
//        h1.setFitWidth(90);
//        h1.setFitHeight(130);
//        h1.setPreserveRatio(true);
//
//        h2.setFitWidth(90);
//        h2.setFitHeight(130);
//        h2.setPreserveRatio(true);
//
//        h1.setVisible(false);
//        h2.setVisible(false);
//
//        playerHand.getChildren().addAll(h1, h2);
//
//        //Placeholder for cards
//        HBox leftCards = (HBox) leftPlayer.getChildren().get(6);
//        HBox topCards = (HBox) topPlayer.getChildren().get(6);
//        HBox rightCards = (HBox) rightPlayer.getChildren().get(6);
//
//        ImageView left1 = (ImageView) leftCards.getChildren().get(0);
//        ImageView left2 = (ImageView) leftCards.getChildren().get(1);
//
//        ImageView top1 = (ImageView) topCards.getChildren().get(0);
//        ImageView top2 = (ImageView) topCards.getChildren().get(1);
//
//        ImageView right1 = (ImageView) rightCards.getChildren().get(0);
//        ImageView right2 = (ImageView) rightCards.getChildren().get(1);
//
//        //Buttons
//        VBox rightControlBox = new VBox(12);
//        rightControlBox.setAlignment(Pos.BOTTOM_RIGHT);
//        rightControlBox.setStyle(
//                "-fx-background-color: rgba(0,0,0,0.35);" +
//                        "-fx-padding: 12;" +
//                        "-fx-background-radius: 15;"
//        );
//
//        Button fold = new Button("Fold");
//        Button check = new Button("Check / Call");
//        Button raise = new Button("Raise");
//        Button allIn = new Button("All-In");
//
//        HBox row1 = new HBox(10);
//        row1.setAlignment(Pos.CENTER_RIGHT);
//        row1.getChildren().addAll(fold, check);
//
//        HBox row2 = new HBox(10);
//        row2.setAlignment(Pos.CENTER_RIGHT);
//        row2.getChildren().addAll(raise, allIn);
//
//        for (Button b : new Button[]{fold, check, raise, allIn}) {
//            b.setMinWidth(120);
//            b.setMinHeight(38);
//            b.setStyle(
//                    "-fx-font-size: 14px;" +
//                            "-fx-font-weight: bold;" +
//                            "-fx-background-radius: 10;" +
//                            "-fx-background-color: linear-gradient(#ffffff, #cccccc);" +
//                            "-fx-border-color: #222;" +
//                            "-fx-border-width: 1;" +
//                            "-fx-border-radius: 10;"
//            );
//        }
//
//        //Raise Slider
//        Slider raiseSlider = new Slider(0, 500, 0);
//        raiseSlider.setShowTickLabels(true);
//        raiseSlider.setShowTickMarks(true);
//        raiseSlider.setMajorTickUnit(50);
//        raiseSlider.setMinorTickCount(5);
//        raiseSlider.setBlockIncrement(10);
//
//        Label raiseText = new Label("Raise auswählen:");
//        raiseText.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
//
//        VBox raiseBox = new VBox(5);
//        raiseBox.setAlignment(Pos.CENTER_RIGHT);
//        raiseBox.getChildren().addAll(raiseText, raiseSlider);
//
//        Label betLabel = new Label("Bet: 0");
//
//        raiseSlider.valueProperty().addListener((obs, oldV, newV) ->
//                betLabel.setText("Bet: " + newV.intValue())
//        );
//
//        rightControlBox.getChildren().addAll(row1, row2, raiseBox);
//        BorderPane bottomArea = new BorderPane();
//        bottomArea.setRight(rightControlBox);
//
//        BorderPane bottom = new BorderPane();
//        bottom.setCenter(playerHand);
//        root.setRight(rightControlBox);
//
//        root.setBottom(bottom);
//
//        //Ovaler Tisch
//        StackPane tableArea = new StackPane();
//        tableArea.setAlignment(Pos.CENTER);
//
//        //Tisch
//        Region ovalTable = new Region();
//        ovalTable.setPrefSize(700, 350);
//        ovalTable.setStyle(
//                "-fx-background-color: #0b6623;" +
//                        "-fx-background-radius: 200;" +
//                        "-fx-border-color: #2b2b2b;" +
//                        "-fx-border-width: 6;" +
//                        "-fx-border-radius: 200;" +
//                        "-fx-effect: dropshadow(gaussian, black, 35, 0.6, 0, 0);"
//        );
//
//        //Deck
//        ImageView deckImage = new ImageView(
//                new Image(getClass().getResourceAsStream("/cards/backside.jpg"))
//        );
//        deckImage.setFitWidth(80);
//        deckImage.setFitHeight(120);
//
//        //deck-pos
//        HBox boardRow = new HBox(25);
//        boardRow.setAlignment(Pos.CENTER);
//        boardRow.getChildren().addAll(communityCards, deckImage);
//
//        //Pot
//        VBox tableContent = new VBox(15);
//        tableContent.setAlignment(Pos.CENTER);
//        tableContent.getChildren().setAll(boardRow, potLabel);
//
//        //forge
//        tableArea.getChildren().addAll(ovalTable, tableContent);
//
//        BorderPane table = new BorderPane();
//        table.setCenter(tableArea);
//        table.setLeft(leftPlayer);
//        table.setRight(rightPlayer);
//        table.setTop(topPlayer);
//        table.setBottom(playerHand);
//
//        root.setCenter(table);
//
//        //Menu Button
//        Button menuButton = new Button("=");
//        menuButton.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
//        BorderPane topBar = new BorderPane();
//        topBar.setRight(menuButton);
//        topBar.setPadding(new Insets(10));
//
//        root.setTop(topBar);
//
//        tableArea.getChildren().setAll(ovalTable, tableContent);
//
//        fold.setOnAction(e -> System.out.println("Player folds"));
//        check.setOnAction(e -> System.out.println("Player checks / calls"));
//        allIn.setOnAction(e -> {
//            System.out.println("Player goes ALL-IN!");
//            setAllIn(leftPlayer, true);
//            setChips(leftPlayer, 0);
//
//            Image real1 = new Image(getClass().getResourceAsStream("/cards/5_of_clubs.jpg"));
//            Image real2  = new Image(getClass().getResourceAsStream("/cards/6_of_spades.jpg"));
//
//
//            flipCard(h1, real1);
//            flipCard(h2, real2);
//        });
//
//        //Scene
//        animationLayer.setPickOnBounds(false);
//
//        StackPane rootStack = new StackPane(root, animationLayer);
//
//        Scene scene = new Scene(rootStack, 900, 600);
//
//        animationLayer.prefWidthProperty().bind(scene.widthProperty());
//        animationLayer.prefHeightProperty().bind(scene.heightProperty());
//
//        overlay = new StackPane();
//        overlay.setVisible(false);
//        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
//
//        VBox popup = new VBox(20);
//        popup.setAlignment(Pos.CENTER);
//        popup.setPrefSize(400, 250);
//        popup.setStyle("-fx-background-color: lightgray; -fx-background-radius: 15;");
//
//        Button helpButton = new Button("Hilfe");
//        helpButton.setMinWidth(200);
//
//        helpButton.setOnAction(e -> helpWindow.setVisible(true));
//
//        Button exitButton = new Button("Spiel verlasen");
//        exitButton.setMinWidth(200);
//
//        popup.getChildren().addAll(helpButton, exitButton);
//        overlay.getChildren().add(popup);
//
//        rootStack.getChildren().add(overlay);
//
//        menuButton.setOnAction(e -> overlay.setVisible(true));
//
//        overlay.setOnMouseClicked(e -> {
//            if (e.getTarget() == overlay) {
//                helpWindow.setVisible(false);
//                overlay.setVisible(false);
//            }
//        });
//
//        exitButton.setOnAction(e -> {
//            Platform.exit();
//        });
//
//        helpWindow = new VBox(15);
//        helpWindow.setAlignment(Pos.TOP_CENTER);
//        helpWindow.setPrefSize(520, 420);
//        helpWindow.setPadding(new Insets(18));
//        helpWindow.setVisible(false);
//        helpWindow.setStyle(
//                "-fx-background-color: rgba(30,30,30,0.92)" +
//                        "-fx-background-radius: 18;" +
//                        "-fx-effect: dropshadow(gaussain, black, 25, 0.5, 0, 0);"
//        );
//
//        Label title = new Label("Hilfe & Pokerhände");
//        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: white;");
//
//        Image helpImage = new Image(
//                getClass().getResourceAsStream("/pokerhands/poker_hands.png")
//        );
//
//        ImageView imageView = new ImageView(helpImage);
//        imageView.setPreserveRatio(true);
//        imageView.setFitWidth(460);
//        imageView.setSmooth(true);
//
//        VBox contentBox = new VBox(15);
//        contentBox.setAlignment(Pos.TOP_CENTER);
//
//        Label picTitle = new Label("Texas Hold'em - Händeübersicht");
//        picTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
//
//        contentBox.getChildren().addAll(picTitle, imageView);
//
//        ScrollPane scroll = new ScrollPane(contentBox);
//        scroll.setFitToWidth(true);
//        scroll.setStyle("-fx-background-color: transparent;");
//
//        Button backButton = new Button("Zurück zum Spiel");
//        backButton.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
//        backButton.setOnAction(e -> hideHelp());
//
//        helpWindow.getChildren().setAll(
//                title,
//                scroll,
//                backButton
//        );
//
//        overlay.getChildren().add(helpWindow);
//
//        //HIer waren stage.scene aufrufe
//
//        //Dealer rotate
//        final int[] dealerIndex = {0};
//
//        int sbIndex = (dealerIndex[0] + 1) % players.size();
//        setSmallBlind(players.get(sbIndex));
//
//        int bbIndex = (dealerIndex[0] + 2) % players.size();
//        setBigBlind(players.get(bbIndex));
//
//        //highlight current player
//        final int[] currentPlayer = {0};
//        highlightPlayer(players.get(currentPlayer[0]), true);
//
//        raise.setOnAction(e -> {
//            System.out.println("Player raises to " + (int) raiseSlider.getValue());
//
//            int delay = 0;
//
//            Timeline timeline = new Timeline(
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(left1, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(top1, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(right1, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(h1, deckImage, animationLayer)),
//
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(left2, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(top2, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(right2, deckImage, animationLayer)),
//                    new KeyFrame(Duration.millis(delay += 300), ev -> dealCardTo(h2, deckImage, animationLayer))
//            );
//
//            timeline.play();
//
//            timeline.setOnFinished(ev -> {
//
//                PauseTransition pause = new PauseTransition(Duration.millis(400));
//
//                pause.setOnFinished(e2 -> {
//
//                    String cardA = deck.draw();
//                    String cardB = deck.draw();
//
//                    Image real1 = new Image(getClass().getResourceAsStream("/cards/" + cardA));
//                    Image real2 = new Image(getClass().getResourceAsStream("/cards/" + cardB));
//
//                    flipCard(h1, real1);
//                    flipCard(h2, real2);
//
//                });
//
//                pause.play();
//            });
//        });
//
//        //test buttons
//        Button flop = new Button("Flop");
//        rightControlBox.getChildren().add(flop);
//        Button turn = new Button("Turn");
//        rightControlBox.getChildren().add(turn);
//        Button river = new Button("River");
//        rightControlBox.getChildren().add(river);
//        Button newRound = new Button("New round");
//        rightControlBox.getChildren().add(newRound);
//        Button collect = new Button("Collect Cards");
//        rightControlBox.getChildren().add(collect);
//        Button playHand = new Button("Play Hand");
//        rightControlBox.getChildren().add(playHand);
//
//        flop.setOnAction(e -> dealFlop(deckImage, animationLayer, com1, com2, com3));
//        turn.setOnAction(e -> dealTurn(deckImage, animationLayer, com4));
//        river.setOnAction(e -> dealRiver(deckImage, animationLayer, com5));
//        newRound.setOnAction(e -> startNewRound(
//                players,
//                com1, com2, com3, com4, com5
//        ));
//        collect.setOnAction(e ->
//                collectAllCardsToDeck(
//                        animationLayer,
//                        deckImage,
//                        new ImageView[]{com1, com2, com3, com4, com5},
//                        players,
//                        h1, h2
//                )
//        );
//        playHand.setOnAction(e -> playFullHand(
//                deckImage,
//                players,
//                com1, com2, com3, com4, com5
//        ));
//    }

    private VBox createPlayerBox(String name, int chips, boolean showCards, Image card1, Image card2) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Circle dealer = new Circle (10);
        dealer.setFill(Color.GOLD);
        dealer.setVisible(false);

        //Small Blind
        Label sbLabel = new Label("SB");
        sbLabel.setTextFill(Color.LIGHTBLUE);
        sbLabel.setVisible(false);

        //Big Blind
        Label bbLabel = new Label("BB");
        bbLabel.setTextFill(Color.RED);
        bbLabel.setVisible(false);

        Label nameLabel = new Label (name);
        Label chipLabel = new Label("Chips: " + chips);

        Label allInLabel = new Label("ALL-IN");
        allInLabel.setTextFill(Color.RED);
        allInLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        allInLabel.setVisible(false);

        HBox cards = new HBox(5);
        cards.setAlignment(Pos.CENTER);

        Image back  = new Image(getClass().getResourceAsStream("/cards/backside.jpg"));

        ImageView c1 = new ImageView(showCards ? card1 : back);
        ImageView c2 = new ImageView(showCards ? card2 : back);

        c1.setFitWidth(60);
        c1.setFitHeight(90);
        c2.setFitWidth(60);
        c2.setFitHeight(90);

        c1.setVisible(false);
        c2.setVisible(false);

        cards.getChildren().addAll(c1, c2);

        box.getChildren().addAll(dealer, sbLabel, bbLabel, nameLabel, chipLabel, allInLabel, cards);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-padding: 5; -fx-border-radius: 5; -fx-background-radius: 5;");

        return box;
    }

    private void markCurrentPlayer(VBox playerBox, boolean active) {
        if (active) {
            playerBox.setStyle("-fx-background-color: rgba(0,255,0,0.3); -fx-padding: 5; -fx-border-radius: 5; -fx-background-radius: 5;");
        } else {
            playerBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-padding: 5; -fx-border-radius: 5; -fx-background-radius: 5;");
        }
    }

    private Image loadCardImage(String cardName) {
        return new Image(
                getClass().getResourceAsStream("/cards/" + cardName + ".jpg")
        );
    }

    private void onNewRound() {
        System.out.println("NEW_ROUND");

        resetCards(h1, h2, com1, com2, com3, com4, com5);
        setControlsEnabled(false);
    }

    private void setDealer(VBox playerBox) {
        Circle dealerChip = (Circle) playerBox.getChildren().get(0);
        dealerChip.setVisible(true);
    }

    private void clearDealer(VBox playerBox) {
        Circle dealerChip = (Circle) playerBox.getChildren().get(0);
        dealerChip.setVisible(false);
    }

    private void setSmallBlind(VBox player) {
        Label sb = (Label) player.getChildren().get(1);
        sb.setVisible(true);
    }

    private void clearSmallBlind(VBox player) {
        Label sb = (Label) player.getChildren().get(1);
        sb.setVisible(false);
    }

    private void setBigBlind(VBox player) {
        Label bb = (Label) player.getChildren().get(2);
        bb.setVisible(true);
    }

    private void clearBigBlind(VBox player) {
        Label bb = (Label) player.getChildren().get(2);
        bb.setVisible(false);
    }

    private void setAllIn(VBox playerBox, boolean value) {
        Label allInLabel = (Label) playerBox.getChildren().get(5);
        allInLabel.setVisible(value);
    }

    private void setChips(VBox playerBox, int chips) {
        Label chipLabel = (Label) playerBox.getChildren().get(4);
        chipLabel.setText("Chips: " + chips);
    }

    private void highlightPlayer(VBox playerBox, boolean active) {
        if (active) {
            playerBox.setStyle(
                    "-fx-background-color: rgba(0,255,0,0.15);" +
                            "-fx-border-color: limegreen;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;"
            );
        } else {
            playerBox.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.1);" +
                            "-fx-border-color: transparent;" +
                            "-fx-padding: 5;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;"
            );
        }
    }

    private void dealCardTo(ImageView targetCard, ImageView deckImage, Pane animationLayer) {

        ImageView flyingCard = new ImageView(
                new Image(getClass().getResourceAsStream("/cards/backside.jpg"))
        );
        flyingCard.setFitWidth(80);
        flyingCard.setFitHeight(120);

        animationLayer.getChildren().add(flyingCard);

        Platform.runLater(() -> {
            Point2D deckScene = deckImage.localToScene(0, 0);
            Point2D targetScene = targetCard.localToScene(0, 0);

            Point2D deckLocal = animationLayer.sceneToLocal(deckScene);
            Point2D targetLocal = animationLayer.sceneToLocal(targetScene);

            flyingCard.setLayoutX(deckLocal.getX());
            flyingCard.setLayoutY(deckLocal.getY());

            TranslateTransition tt = new TranslateTransition(Duration.millis(500), flyingCard);

            tt.setToX(targetLocal.getX() - deckLocal.getX());
            tt.setToY(targetLocal.getY() - deckLocal.getY());

            tt.setOnFinished(ev -> {
                animationLayer.getChildren().remove(flyingCard);
                targetCard.setVisible(true);
            });

            tt.play();
        });
    }

    public void onHandMessage(String msg) {
        String[] parts = msg.split(" ");

        Image c1 = loadCardImage(parts[0]);
        Image c2 = loadCardImage(parts[1]);

        Platform.runLater(() -> {
            h1.setVisible(true);
            h2.setVisible(true);
            flipCard(h1, c1);
            flipCard(h2, c2);
        });
    }

    private void flipCard(ImageView card, Image frontImage) {

        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), card);
        shrink.setToX(0.0);

        ScaleTransition grow = new ScaleTransition(Duration.millis(150), card);
        grow.setToX(1.0);

        shrink.setOnFinished(e -> card.setImage(frontImage));

        SequentialTransition flip = new SequentialTransition(shrink, grow);
        flip.play();
    }

//    private void dealFlop(ImageView deckImage, Pane animationLayer,
//                          ImageView c1, ImageView c2, ImageView c3) {
//
//        Image real1 = new Image(getClass().getResourceAsStream("/cards/" + deck.draw()));
//        Image real2 = new Image(getClass().getResourceAsStream("/cards/" + deck.draw()));
//        Image real3 = new Image(getClass().getResourceAsStream("/cards/" + deck.draw()));
//
//        SequentialTransition seq = new SequentialTransition();
//
//        for (ImageView target : new ImageView[]{c1, c2, c3}) {
//
//            ImageView flying = new ImageView(new Image(getClass().getResourceAsStream("/cards/backside.jpg")));
//            flying.setFitWidth(90);
//            flying.setFitHeight(130);
//
//            animationLayer.getChildren().add(flying);
//
//            Bounds deck = deckImage.localToScene(deckImage.getBoundsInLocal());
//            Bounds targetB = target.localToScene(target.getBoundsInLocal());
//
//            double startX = deck.getCenterX();
//            double startY = deck.getCenterY();
//
//            double endX = targetB.getCenterX();
//            double endY = targetB.getCenterY();
//
//            Point2D start = animationLayer.sceneToLocal(startX, startY);
//            Point2D end = animationLayer.sceneToLocal(endX, endY);
//
//
//            flying.setLayoutX(start.getX());
//            flying.setLayoutY(start.getY());
//
//            TranslateTransition tt = new TranslateTransition(Duration.millis(400), flying);
//            tt.setToX(end.getX() - start.getX());
//            tt.setToY(end.getY() - start.getY());
//
//            tt.setOnFinished(ev -> {
//                animationLayer.getChildren().remove(flying);
//                target.setVisible(true);
//            });
//
//            seq.getChildren().add(tt);
//        }
//
//        seq.setOnFinished(ev -> {
//            flipCard(c1, real1);
//            flipCard(c2, real2);
//            flipCard(c3, real3);
//        });
//
//        seq.play();
//    }
//
//    private void dealCardTo(ImageView c1, ImageView deckImage, Pane animationLayer, Object o) {
//    }
//
//    private void showAndFlip(ImageView card, Image face) {
//        card.setOpacity(1);
//
//        ScaleTransition shrink = new ScaleTransition(Duration.millis(120), card);
//        shrink.setToX(0);
//
//        ScaleTransition grow = new ScaleTransition(Duration.millis(120), card);
//        grow.setToX(1);
//
//        shrink.setOnFinished(e -> card.setImage(face));
//
//        new SequentialTransition(shrink, grow).play();
//    }
//    private void dealTurn(ImageView deckImage, Pane animationLayer, ImageView turnCard) {
//        Image realTurn = new Image(getClass().getResourceAsStream("/cards/" + deck.draw()));
//
//        ImageView flying = new ImageView(new Image(getClass().getResourceAsStream("/cards/backside.jpg")));
//        flying.setFitWidth(90);
//        flying.setFitHeight(130);
//
//        animationLayer.getChildren().add(flying);
//
//        Bounds deck = deckImage.localToScene(deckImage.getBoundsInLocal());
//        Bounds targetB = turnCard.localToScene(turnCard.getBoundsInLocal());
//
//        double startX = deck.getCenterX();
//        double startY = deck.getCenterY();
//
//        double endX = targetB.getCenterX();
//        double endY = targetB.getCenterY();
//
//        Point2D start = animationLayer.sceneToLocal(startX, startY);
//        Point2D end = animationLayer.sceneToLocal(endX, endY);
//
//        flying.setLayoutX(start.getX());
//        flying.setLayoutY(start.getY());
//
//        TranslateTransition tt = new TranslateTransition(Duration.millis(400), flying);
//        tt.setToX(end.getX() - start.getX());
//        tt.setToY(end.getY() - start.getY());
//
//        tt.setOnFinished(ev -> {
//            animationLayer.getChildren().remove(flying);
//
//            turnCard.setVisible(true);
//            turnCard.setOpacity(1);
//
//            flipCard(turnCard, realTurn);
//        });
//
//        tt.play();
//    }

//    private void dealRiver(ImageView deckImage, Pane animationLayer, ImageView riverCard) {
//        Image realRiver = new Image(getClass().getResourceAsStream("/cards/" + deck.draw()));
//
//        ImageView flying = new ImageView(new Image(getClass().getResourceAsStream("/cards/backside.jpg")));
//        flying.setFitWidth(90);
//        flying.setFitHeight(130);
//
//        animationLayer.getChildren().add(flying);
//
//        Bounds deck = deckImage.localToScene(deckImage.getBoundsInLocal());
//        Bounds targetB = riverCard.localToScene(riverCard.getBoundsInLocal());
//
//        double startX = deck.getCenterX();
//        double startY = deck.getCenterY();
//
//        double endX = targetB.getCenterX();
//        double endY = targetB.getCenterY();
//
//        Point2D start = animationLayer.sceneToLocal(startX, startY);
//        Point2D end = animationLayer.sceneToLocal(endX, endY);
//
//        flying.setLayoutX(start.getX());
//        flying.setLayoutY(start.getY());
//
//        TranslateTransition tt = new TranslateTransition(Duration.millis(400), flying);
//        tt.setToX(end.getX() - start.getX());
//        tt.setToY(end.getY() - start.getY());
//
//        tt.setOnFinished(ev -> {
//            animationLayer.getChildren().remove(flying);
//
//            riverCard.setVisible(true);
//            riverCard.setOpacity(1);
//
//            flipCard(riverCard, realRiver);
//        });
//
//        tt.play();
//    }

//    private void startNewRound(
//            java.util.List<VBox> players,
//            ImageView com1, ImageView com2, ImageView com3, ImageView com4, ImageView com5
//    ) {
//
//        deck = new Deck();
//
//        resetCards(com1, com2, com3, com4, com5, h1, h2);
//
//        for (ImageView c : new ImageView[]{com1, com2, com3, com4, com5}) {
//            c.setVisible(false);
//        }
//
//        h1.setVisible(false);
//        h2.setVisible(false);
//
//        for (VBox p : players) {
//            HBox cards = (HBox) p.getChildren().get(6);
//            ImageView c1 = (ImageView) cards.getChildren().get(0);
//            ImageView c2 = (ImageView) cards.getChildren().get(1);
//
//            resetCards(c1, c2);
//        }
//
//        rotateDealer(players);
//
//        assignBlinds(players);
//
//        dealNewHandForAllPlayers(deckImage, players);
//
//        System.out.println("Next round has started");
//
//    }
//    private int dealerIndex = 0;
//
//    private void rotateDealer(java.util.List<VBox> players) {
//
//        clearDealer(players.get(dealerIndex));
//
//        dealerIndex = (dealerIndex + 1) % players.size();
//
//        setDealer(players.get(dealerIndex));
//    }
//
//    private void assignBlinds(java.util.List<VBox> players) {
//
//        for (VBox p : players) {
//            clearSmallBlind(p);
//            clearBigBlind(p);
//        }
//
//        int sbIndex = (dealerIndex + 1) % players.size();
//        int bbIndex = (dealerIndex + 2) % players.size();
//
//        setSmallBlind(players.get(sbIndex));
//        setBigBlind(players.get(bbIndex));
//
//
//
    private void resetCards(ImageView... cards) {

        Image back = new Image(getClass().getResourceAsStream("/cards/backside.jpg"));

        for (ImageView c : cards) {
            c.setImage(back);
            c.setVisible(false);
            c.setOpacity(1.0);
        }
    }
//    private void dealNewHandForAllPlayers(ImageView deckImage, List<VBox> players) {
//        Timeline timeline = new Timeline();
//        int delay = 0;
//
//        ImageView[][] playerCards = new ImageView[players.size()][2];
//
//        for (int i = 0; i < players.size(); i++) {
//            HBox cards = (HBox) players.get(i).getChildren().get(6);
//            playerCards[i][0] = (ImageView) cards.getChildren().get(0);
//            playerCards[i][1] = (ImageView) cards.getChildren().get(1);
//        }
//
//        for (int i = 0; i < players.size(); i++) {
//            ImageView target = playerCards[i][0];
//            timeline.getKeyFrames().add(
//                    new KeyFrame(Duration.millis(delay += 300),
//                            e -> dealCardTo(target, deckImage, animationLayer))
//            );
//        }
//
//        timeline.getKeyFrames().add(
//                new KeyFrame(Duration.millis(delay += 300),
//                        e -> dealCardTo(h1, deckImage, animationLayer))
//        );
//
//        for (int i = 0; i < players.size(); i++) {
//            ImageView target = playerCards[i][1];
//            timeline.getKeyFrames().add(
//                    new KeyFrame(Duration.millis(delay += 300),
//                            e -> dealCardTo(target, deckImage, animationLayer))
//            );
//        }
//
//        timeline.getKeyFrames().add(
//                new KeyFrame(Duration.millis(delay += 300),
//                        e -> dealCardTo(h2, deckImage, animationLayer))
//        );
//
//        timeline.setOnFinished(ev -> {
//            String a = deck.draw();
//            String b = deck.draw();
//
//            Image heroA = new Image(getClass().getResourceAsStream("/cards/" + a));
//            Image heroB = new Image(getClass().getResourceAsStream("/cards/" + b));
//
//            h1.setVisible(true);
//            h2.setVisible(true);
//
//            flipCard(h1, heroA);
//            flipCard(h2, heroB);
//        });
//
//        timeline.play();
//    }
//
//    private void collectAllCardsToDeck(
//            Pane animationLayer,
//            ImageView deckImage,
//            ImageView[] boardCards,
//            java.util.List<VBox> players,
//            ImageView h1,
//            ImageView h2
//    ) {
//        SequentialTransition seq = new SequentialTransition();
//
//        Platform.runLater(() -> {
//            for (ImageView card : boardCards) {
//                if (!card.isVisible()) continue;
//
//                ImageView flying = new ImageView(card.getImage());
//                flying.setFitWidth(card.getFitWidth());
//                flying.setFitHeight(card.getFitHeight());
//
//                animationLayer.getChildren().add(flying);
//
//                Bounds fromB = card.localToScene(card.getBoundsInLocal());
//                Bounds toB = deckImage.localToScene(deckImage.getBoundsInLocal());
//
//                Point2D start = animationLayer.sceneToLocal(fromB.getCenterX(), fromB.getCenterY());
//                Point2D end = animationLayer.sceneToLocal(toB.getCenterX(), toB.getCenterY());
//
//                flying.setLayoutX(start.getX());
//                flying.setLayoutY(start.getY());
//
//                TranslateTransition tt = new TranslateTransition(Duration.millis(700), flying);
//                tt.setToX(end.getX() - start.getX());
//                tt.setToY(end.getY() - start.getY());
//
//                tt.setOnFinished(ev -> {
//                    animationLayer.getChildren().remove(flying);
//                    card.setVisible(false);
//                });
//
//                seq.getChildren().add(tt);
//            }
//
//            for (VBox p : players) {
//                HBox cards = (HBox) p.getChildren().get(6);
//                ImageView c1 = (ImageView) cards.getChildren().get(0);
//                ImageView c2 = (ImageView) cards.getChildren().get(1);
//
//                for (ImageView card : new ImageView[] {c1, c2}) {
//                    if (!card.isVisible()) continue;
//
//                    ImageView flying = new ImageView(card.getImage());
//                    flying.setFitWidth(card.getFitWidth());
//                    flying.setFitHeight(card.getFitHeight());
//
//                    animationLayer.getChildren().add(flying);
//
//                    Bounds fromB = card.localToScene(card.getBoundsInLocal());
//                    Bounds toB = deckImage.localToScene(deckImage.getBoundsInLocal());
//
//                    Point2D start = animationLayer.sceneToLocal(fromB.getCenterX(), fromB.getCenterY());
//                    Point2D end = animationLayer.sceneToLocal(toB.getCenterX(), toB.getCenterY());
//
//                    flying.setLayoutX(start.getX());
//                    flying.setLayoutY(start.getY());
//
//                    TranslateTransition tt = new TranslateTransition(Duration.millis(700), flying);
//                    tt.setToX(end.getX() - start.getX());
//                    tt.setToY(end.getY() - start.getY());
//
//                    tt.setOnFinished(ev -> {
//                        animationLayer.getChildren().remove(flying);
//                        card.setVisible(false);
//                    });
//
//                    seq.getChildren().add(tt);
//                }
//            }
//
//            for (ImageView hero : new ImageView[]{h1, h2}) {
//                if (!hero.isVisible()) continue;
//
//                ImageView flying = new ImageView(hero.getImage());
//                flying.setFitWidth(hero.getFitWidth());
//                flying.setFitHeight(hero.getFitHeight());
//
//                animationLayer.getChildren().add(flying);
//
//                Bounds fromB = hero.localToScene(hero.getBoundsInLocal());
//                Bounds toB = deckImage.localToScene(deckImage.getBoundsInLocal());
//
//                Point2D start = animationLayer.sceneToLocal(fromB.getCenterX(), fromB.getCenterY());
//                Point2D end = animationLayer.sceneToLocal(toB.getCenterX(), toB.getCenterY());
//
//                flying.setLayoutX(start.getX());
//                flying.setLayoutY(start.getY());
//
//                TranslateTransition tt = new TranslateTransition(Duration.millis(700), flying);
//                tt.setToX(end.getX() - start.getX());
//                tt.setToY(end.getY() - start.getY());
//
//                tt.setOnFinished(ev -> {
//                    animationLayer.getChildren().remove(flying);
//                    hero.setVisible(false);
//                });
//
//                seq.getChildren().add(tt);
//            }
//
//            seq.setOnFinished(ev -> {
//                deck = new Deck();
//
//                rotateDealer(players);
//
//                assignBlinds(players);
//
//                dealNewHandForAllPlayers(deckImage, players);
//
//                autoDealBoard(deckImage,
//                        boardCards[0], boardCards[1], boardCards[2], boardCards[3],
//                        boardCards[4]
//                );
//
//                System.out.println("Deck has been shuffled");
//            });
//
//            seq.play();
//        });
//    }
//
//    private void autoDealBoard(ImageView deckImage,
//                               ImageView com1, ImageView com2, ImageView com3,
//                               ImageView com4, ImageView com5) {
//
//        Timeline t = new Timeline();
//        int d = 0;
//
//        t.getKeyFrames().add(new KeyFrame(Duration.millis(d+= 1200),
//                e -> dealFlop(deckImage, animationLayer, com1, com2, com3)));
//
//        t.getKeyFrames().add(new KeyFrame(Duration.millis(d += 1600),
//                e -> dealTurn(deckImage, animationLayer, com4)));
//
//        t.getKeyFrames().add(new KeyFrame(Duration.millis(d += 1600),
//                e -> dealRiver(deckImage, animationLayer, com5)));
//
//        t.play();
//    }
//
//    private void playFullHand(ImageView deckImage, List<VBox> players,
//                              ImageView com1, ImageView com2, ImageView com3, ImageView com4,
//                              ImageView com5) {
//        dealNewHandForAllPlayers(deckImage, players);
//
//        Timeline t = new Timeline();
//        int d = 0;
//        d += 3500;
//
//        t.getKeyFrames().add(
//                new KeyFrame(Duration.millis(d),
//                        e -> dealFlop(deckImage, animationLayer, com1, com2, com3))
//        );
//
//        d += 1800;
//
//        t.getKeyFrames().add(
//                new KeyFrame(Duration.millis(d),
//                        e -> dealTurn(deckImage, animationLayer, com4))
//        );
//
//        d += 1800;
//
//        t.getKeyFrames().add(
//                new KeyFrame(Duration.millis(d),
//                        e -> dealRiver(deckImage, animationLayer, com5))
//        );
//
//        t.play();
//    }

    private void showHelp() {
        overlay.setVisible(true);
        helpWindow.setScaleX(0.6);
        helpWindow.setScaleY(0.6);
        helpWindow.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(250), helpWindow);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(250), helpWindow);
        scale.setToX(1);
        scale.setToY(1);

        new ParallelTransition(fade, scale).play();
    }

    private void hideHelp() {
        FadeTransition fade = new FadeTransition(Duration.millis(200), helpWindow);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(200), helpWindow);
        scale.setToX(0.7);
        scale.setToY(0.7);

        ParallelTransition pt = new ParallelTransition(fade, scale);

        pt.setOnFinished(e -> overlay.setVisible(false));
        pt.play();
    }

    private HBox createHandExample(String title, String imagePatch) {
        ImageView img = new ImageView(new Image(getClass().getResourceAsStream(imagePatch)));
        img.setFitHeight(70);
        img.setPreserveRatio(true);

        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        HBox box = new HBox(12, img, lbl);
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }
    private void onExitGameClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
    }

    private void setControlsEnabled(boolean enabled) {
        foldBtn.setDisable(!enabled);
        checkBtn.setDisable(!enabled);
        raiseBtn.setDisable(!enabled);
        allInBtn.setDisable(!enabled);
    }


@Override
    public void onServerMessage(String message) {
        if (message.startsWith("HAND")) {
            String[] p = message.split(" ");
            Image c1 = loadCardImage(p[1]);
            Image c2 = loadCardImage(p[2]);

            Platform.runLater(() -> {
                h1.setVisible(true);
                h2.setVisible(true);
                flipCard(h1, c1);
                flipCard(h2, c2);
            });
            return;
        }
        if (message.startsWith("GAME_STATE")) {
            handleGameState(message);
        }
    }

        private void handleGameState(String message) {
            String[] parts = message.split(" ");
            String currentPlayer = null;

            for (String p : parts) {
                if (p.startsWith("currentPlayer=")) {
                    currentPlayer = p.split("=")[1];
                }
            }

            String myName = App.getSceneController()
                    .getClient()
                    .getPlayerName();

            boolean myTurn = myName.equals(currentPlayer);

            Platform.runLater(() -> setControlsEnabled(myTurn));
        }

        private void showFlop(String[] msg) {
            Image c1 = loadCardImage(msg[1]);
            Image c2 = loadCardImage(msg[2]);
            Image c3 = loadCardImage(msg[3]);

            com1.setVisible(true);
            com2.setVisible(true);
            com3.setVisible(true);

            com1.setImage(c1);
            com2.setImage(c2);
            com3.setImage(c3);
        }

        private void showTurn(String card) {
            com4.setVisible(true);
            com4.setImage(loadCardImage(card));
        }

        private void showRiver(String card) {
            com5.setVisible(true);
            com5.setImage(loadCardImage(card));
        }
}

