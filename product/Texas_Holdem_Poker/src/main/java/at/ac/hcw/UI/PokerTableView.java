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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.IOException;

public class PokerTableView implements ServerMessageListener {

    // UI Referenzen für Updates
    private ImageView h1, h2; // Hero Cards
    private ImageView o1, o2; // Opponent Cards
    private ImageView com1, com2, com3, com4, com5; // Board Cards

    private VBox heroBox;
    private VBox opponentBox;

    private Label potLabel;
    private Label heroChipLabel;
    private Label opponentChipLabel;
    private Label currentBetLabel;

    private Button foldBtn, checkBtn, raiseBtn, allInBtn;
    private Slider raiseSlider;
    private Pane animationLayer;
    private ImageView deckImage;
    private StackPane overlay;
    private VBox helpWindow;

    private PokerClient client;

    public Parent createView() {
        this.client = App.getSceneController().getClient();

        BorderPane root = new BorderPane();
        animationLayer = new Pane();
        animationLayer.setPickOnBounds(false); // Klicks gehen durch

        root.setStyle("-fx-background-color: #2b2b2b;"); // Dunklerer Hintergrund für Kontrast

        // 1. Spielfeld Setup (Tisch & Karten)
        StackPane tableArea = createTableArea();
        root.setCenter(tableArea);

        // 2. Spieler Setup (2 Spieler Modus)
        // Gegner (Oben)
        opponentBox = createPlayerBox("Gegner", 0, false); // Karten verdeckt
        HBox opponentContainer = new HBox(opponentBox);
        opponentContainer.setAlignment(Pos.CENTER);
        opponentContainer.setPadding(new Insets(20));
        root.setTop(opponentContainer);

        // Hero (Unten) + Controls
        heroBox = createPlayerBox("Du", 0, true); // Karten placeholders sichtbar
        VBox bottomContainer = new VBox(20);
        bottomContainer.setAlignment(Pos.CENTER);

        // Controls erstellen
        Node controls = createControls();
        bottomContainer.getChildren().addAll(heroBox, controls);
        bottomContainer.setPadding(new Insets(20));
        root.setBottom(bottomContainer);

        // Referenzen auf die Karten holen (Hack für einfachen Zugriff)
        extractCardReferences();

        // Overlay Menü Initialisieren
        createOverlayMenu(root);

        // Menu Button oben rechts
        Button menuButton = new Button("=");
        menuButton.setOnAction(e -> overlay.setVisible(true));
        StackPane.setAlignment(menuButton, Pos.TOP_RIGHT);
        StackPane.setMargin(menuButton, new Insets(10));

        // Root zusammenbauen
        StackPane rootStack = new StackPane(root, animationLayer, menuButton, overlay);

        // Animation Layer Größe binden
        animationLayer.prefWidthProperty().bind(rootStack.widthProperty());
        animationLayer.prefHeightProperty().bind(rootStack.heightProperty());

        // Initialzustand: Buttons deaktiviert bis Server "YOUR_TURN" sendet
        setControlsEnabled(false);

        return rootStack;
    }

    // --- Layout Helper ---

    private StackPane createTableArea() {
        StackPane tableArea = new StackPane();

        // Der grüne ovale Tisch
        Region ovalTable = new Region();
        ovalTable.setMaxSize(800, 400);
        ovalTable.setStyle(
                "-fx-background-color: #0b6623;" +
                        "-fx-background-radius: 200;" +
                        "-fx-border-color: #1a1a1a;" +
                        "-fx-border-width: 8;" +
                        "-fx-border-radius: 200;" +
                        "-fx-effect: dropshadow(gaussian, black, 50, 0.5, 0, 0);"
        );

        // Community Cards Container
        HBox boardRow = new HBox(15);
        boardRow.setAlignment(Pos.CENTER);

        Image back = new Image(getClass().getResourceAsStream("/cards/backside.jpg"));

        com1 = createCardView(back);
        com2 = createCardView(back);
        com3 = createCardView(back);
        com4 = createCardView(back);
        com5 = createCardView(back);

        // Deck Grafik (für Animationen)
        deckImage = new ImageView(back);
        deckImage.setFitWidth(80);
        deckImage.setFitHeight(120);

        boardRow.getChildren().addAll(com1, com2, com3, com4, com5, new Region(), deckImage);
        ((Region) boardRow.getChildren().get(5)).setPrefWidth(50); // Spacer vor Deck

        // Pot Label
        potLabel = new Label("Pot: 0");
        potLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 1, 0, 0);");
        currentBetLabel = new Label("Aktueller Einsatz: 0");
        currentBetLabel.setStyle("-fx-font-size: 16; -fx-text-fill: lightgreen; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 1, 1, 0, 0);");
        VBox tableContent = new VBox(10); // Abstand etwas verringert
        tableContent.setAlignment(Pos.CENTER);
        // Label zur Box hinzufügen
        tableContent.getChildren().addAll(boardRow, potLabel, currentBetLabel);

        tableArea.getChildren().addAll(ovalTable, tableContent);
        return tableArea;
    }

    private Node createControls() {
        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(10));
        controlBox.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 15;");
        controlBox.setMaxWidth(800);

        foldBtn = new Button("FOLD");
        checkBtn = new Button("CHECK / CALL");
        raiseBtn = new Button("RAISE");
        allInBtn = new Button("ALL-IN");

        // Styling
        String btnStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-base: #444; -fx-text-fill: white;";
        for(Button b : new Button[]{foldBtn, checkBtn, raiseBtn, allInBtn}) {
            b.setStyle(btnStyle);
            b.setMinWidth(100);
            b.setPrefHeight(40);
        }

        // Fold Aktion
        foldBtn.setOnAction(e -> client.sendMessage("FOLD"));

        // Check/Call Aktion
        checkBtn.setOnAction(e -> client.sendMessage("CHECK"));
        // Anmerkung: Server sollte unterscheiden, ob es Check oder Call ist basierend auf GameState,
        // oder man sendet "CALL", wenn es eine Bet gibt. Hier vereinfacht.

        // All-In
        allInBtn.setOnAction(e -> client.sendMessage("ALLIN"));

        // Raise Slider
        raiseSlider = new Slider(0, 1000, 0);
        Label betValLabel = new Label("0");
        betValLabel.setTextFill(Color.WHITE);

        raiseSlider.valueProperty().addListener((obs, o, n) -> {
            betValLabel.setText(String.valueOf(n.intValue()));
        });

        raiseBtn.setOnAction(e -> {
            int amount = (int) raiseSlider.getValue();
            client.sendMessage("RAISE " + amount);
        });

        VBox raiseBox = new VBox(5, new Label("Raise Amount"), raiseSlider, betValLabel);
        raiseBox.setAlignment(Pos.CENTER);
        ((Label)raiseBox.getChildren().get(0)).setTextFill(Color.LIGHTGRAY);

        controlBox.getChildren().addAll(foldBtn, checkBtn, raiseBox, raiseBtn, allInBtn);

        return controlBox;
    }

    private VBox createPlayerBox(String name, int chips, boolean isHero) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        // Hintergrund für aktiven Spieler
        box.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10;");

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        Label chipLbl = new Label("Chips: " + chips);
        chipLbl.setStyle("-fx-text-fill: gold; -fx-font-size: 14;");

        // Karten Container
        HBox cards = new HBox(5);
        cards.setAlignment(Pos.CENTER);

        Image back = new Image(getClass().getResourceAsStream("/cards/backside.jpg"));

        ImageView c1 = createCardView(back);
        ImageView c2 = createCardView(back);

        // Für Gegner Karten initial sichtbar (Rückseite), für Hero unsichtbar bis Deal
        if (!isHero) {
            c1.setVisible(true);
            c2.setVisible(true);
        } else {
            c1.setVisible(false);
            c2.setVisible(false);
        }

        cards.getChildren().addAll(c1, c2);

        // Dealer Button (Initial unsichtbar)
        Circle dealerBtn = new Circle(8, Color.RED);
        dealerBtn.setStroke(Color.BLACK);
        dealerBtn.setVisible(false);

        HBox topInfo = new HBox(10, dealerBtn, nameLbl);
        topInfo.setAlignment(Pos.CENTER);

        box.getChildren().addAll(topInfo, chipLbl, cards);

        return box;
    }

    private ImageView createCardView(Image img) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(80);
        iv.setFitHeight(120);
        iv.setPreserveRatio(true);
        return iv;
    }

    /**
     * Extrahiert die Referenzen aus den dynamisch erzeugten VBoxen,
     * damit wir sie in onServerMessage ansprechen können.
     */
    private void extractCardReferences() {
        HBox heroCardsBox = (HBox) heroBox.getChildren().get(2);
        h1 = (ImageView) heroCardsBox.getChildren().get(0);
        h2 = (ImageView) heroCardsBox.getChildren().get(1);
        heroChipLabel = (Label) heroBox.getChildren().get(1);

        HBox oppCardsBox = (HBox) opponentBox.getChildren().get(2);
        o1 = (ImageView) oppCardsBox.getChildren().get(0);
        o2 = (ImageView) oppCardsBox.getChildren().get(1);
        opponentChipLabel = (Label) opponentBox.getChildren().get(1);
    }

    // --- SERVER MESSAGE HANDLING ---

    /**
     * Zentraler Einstiegspunkt für alle Server-Events.
     * Erwartetes Protokoll (Beispiele):
     * - HAND 5_of_clubs 6_of_hearts
     * - FLOP A_of_spades 10_of_diamonds 2_of_hearts
     * - TURN K_of_clubs
     * - RIVER Q_of_hearts
     * - POT 500
     * - CHIPS HERO 1500
     * - CHIPS OPPONENT 2000
     * - TURN_ACTIVE HERO
     * - TURN_ACTIVE OPPONENT
     * - OPPONENT_FOLD
     * - NEW_ROUND
     */
    @Override
    public void onServerMessage(String message) {
        if (message == null || message.trim().isEmpty()) return;

        Platform.runLater(() -> {
            String[] parts = message.split(" ");
            String cmd = parts[0];

            switch (cmd) {
                case "CURRENT_BET":
                    // Nachricht Format: CURRENT_BET <amount>
                    if (parts.length >= 2) {
                        currentBetLabel.setText("Aktueller Einsatz: " + parts[1]);
                    }
                    break;

                case "HAND":
                    // Format: HAND card1 card2
                    if (parts.length >= 3) {
                        dealPlayerHand(parts[1], parts[2]);
                    }
                    break;

                case "OPPONENT_HAND":
                    // Gegner bekommt Karten (Animation Rückseite)
                    dealOpponentHand();
                    break;

                case "FLOP":
                    // Format: FLOP c1 c2 c3
                    if (parts.length >= 4) {
                        dealCommunityCards(new String[]{parts[1], parts[2], parts[3]}, com1, com2, com3);
                    }
                    break;

                case "TURN":
                    // Format: TURN c1
                    if (parts.length >= 2) {
                        dealCommunityCards(new String[]{parts[1]}, com4);
                    }
                    break;

                case "RIVER":
                    // Format: RIVER c1
                    if (parts.length >= 2) {
                        dealCommunityCards(new String[]{parts[1]}, com5);
                    }
                    break;

                case "POT":
                    if (parts.length >= 2) potLabel.setText("Pot: " + parts[1]);
                    break;

                case "CHIPS":
                    // Format: CHIPS <WHO> <AMOUNT>
                    if (parts.length >= 3) {
                        updateChips(parts[1], parts[2]);
                    }
                    break;

                case "TURN_ACTIVE":
                    // Format: TURN_ACTIVE <WHO> (HERO oder OPPONENT)
                    boolean isMyTurn = parts.length >= 2 && parts[1].equals("HERO");
                    updateActivePlayer(isMyTurn);
                    break;

                case "SHOWDOWN":
                    // Gegner Karten aufdecken: SHOWDOWN c1 c2
                    if (parts.length >= 3) {
                        revealOpponent(parts[1], parts[2]);
                    }
                    break;

                case "NEW_ROUND":
                    resetTable();
                    break;

                case "WINNER":
                    // Format: WINNER HERO 500
                    showWinnerAnimation(parts.length >= 2 ? parts[1] : "Unbekannt");
                    break;
            }
        });
    }

    // --- Animation & Logic Methods ---

    private void dealPlayerHand(String card1Name, String card2Name) {
        h1.setVisible(true); // Container sichtbar machen (Karten bleiben noch kurz unsichtbar bis Animation startet)
        h2.setVisible(true);

        // Wir übergeben 'true', weil wir unsere Karten sehen wollen
        animateCardDeal(deckImage, h1, loadCardImage(card1Name), 200, true);
        animateCardDeal(deckImage, h2, loadCardImage(card2Name), 300, true);
    }

    private void dealOpponentHand() {
        o1.setVisible(true);
        o2.setVisible(true);
        Image back = new Image(getClass().getResourceAsStream("/cards/backside.jpg"));

        // Wir übergeben 'false', weil Gegnerkarten verdeckt bleiben
        animateCardDeal(deckImage, o1, back, 200, false);
        animateCardDeal(deckImage, o2, back, 300, false);
    }

    private void dealCommunityCards(String[] cardNames, ImageView... targets) {
        int delay = 300;
        for (int i = 0; i < targets.length; i++) {
            // targets[i].setVisible(true); // Das macht animateCardDeal jetzt im finish
            Image front = loadCardImage(cardNames[i]);

            // Community Cards sollen aufgedeckt werden -> true
            animateCardDeal(deckImage, targets[i], front, delay, true);
            delay += 300;
        }
    }

    private void revealOpponent(String c1, String c2) {
        flipCard(o1, loadCardImage(c1));
        flipCard(o2, loadCardImage(c2));
    }

    private void updateChips(String who, String amount) {
        if ("HERO".equals(who)) {
            heroChipLabel.setText("Chips: " + amount);
        } else {
            opponentChipLabel.setText("Chips: " + amount);
        }
    }

    private void updateActivePlayer(boolean isMyTurn) {
        setControlsEnabled(isMyTurn);

        // Visuelles Feedback wer dran ist
        if (isMyTurn) {
            heroBox.setStyle("-fx-background-color: rgba(0,255,0,0.2); -fx-background-radius: 10; -fx-border-color: green; -fx-border-radius: 10;");
            opponentBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10;");
        } else {
            heroBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10;");
            opponentBox.setStyle("-fx-background-color: rgba(255,0,0,0.2); -fx-background-radius: 10; -fx-border-color: red; -fx-border-radius: 10;");
        }
    }

    private void resetTable() {
        // Karten resetten
        Image back = new Image(getClass().getResourceAsStream("/cards/backside.jpg"));
        for (ImageView iv : new ImageView[]{h1, h2, o1, o2, com1, com2, com3, com4, com5}) {
            iv.setVisible(false);
            iv.setImage(back);
        }
        potLabel.setText("Pot: 0");
    }

    private void showWinnerAnimation(String winnerName) {
        Label winLabel = new Label(winnerName + " WINS!");
        winLabel.setStyle("-fx-font-size: 40; -fx-font-weight: bold; -fx-text-fill: gold; -fx-effect: dropshadow(gaussian, black, 10, 0.8, 0, 0);");

        StackPane root = (StackPane) animationLayer.getParent();
        root.getChildren().add(winLabel);

        FadeTransition ft = new FadeTransition(Duration.seconds(3), winLabel);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> root.getChildren().remove(winLabel));
        ft.play();
    }

    // Neuer Parameter am Ende: boolean reveal (true = aufdecken, false = verdeckt lassen)
    private void animateCardDeal(ImageView source, ImageView target, Image finalImage, int delayMillis, boolean reveal) {
        // Hilfs-ImageView für den Flug
        ImageView flying = new ImageView(new Image(getClass().getResourceAsStream("/cards/backside.jpg")));
        flying.setFitWidth(target.getFitWidth());
        flying.setFitHeight(target.getFitHeight());

        // Positionierung
        Bounds sourceBounds = source.localToScene(source.getBoundsInLocal());
        Bounds targetBounds = target.localToScene(target.getBoundsInLocal());

        // Umrechnung auf AnimationLayer Koordinaten
        Point2D startP = animationLayer.sceneToLocal(sourceBounds.getCenterX(), sourceBounds.getCenterY());
        Point2D endP = animationLayer.sceneToLocal(targetBounds.getCenterX(), targetBounds.getCenterY());

        flying.setLayoutX(startP.getX() - flying.getFitWidth()/2);
        flying.setLayoutY(startP.getY() - flying.getFitHeight()/2);
        flying.setVisible(false);

        animationLayer.getChildren().add(flying);

        Timeline t = new Timeline(new KeyFrame(Duration.millis(delayMillis), e -> {
            flying.setVisible(true);

            TranslateTransition tt = new TranslateTransition(Duration.millis(400), flying);
            tt.setToX(endP.getX() - startP.getX());
            tt.setToY(endP.getY() - startP.getY());

            tt.setOnFinished(ev -> {
                animationLayer.getChildren().remove(flying);

                // HIER WAR DER FEHLER: Wir nutzen jetzt das boolean Flag
                if (reveal) {
                    // Karte ist unsichtbar oder Rückseite -> Flip zur Vorderseite
                    target.setVisible(true);
                    flipCard(target, finalImage);
                } else {
                    // Karte bleibt Rückseite (für Gegner)
                    target.setImage(finalImage);
                    target.setVisible(true);
                }
            });
            tt.play();
        }));
        t.play();
    }

    private void flipCard(ImageView card, Image frontImage) {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), card);
        shrink.setToX(0.0);

        shrink.setOnFinished(e -> {
            card.setImage(frontImage);
            ScaleTransition grow = new ScaleTransition(Duration.millis(150), card);
            grow.setToX(1.0);
            grow.play();
        });
        shrink.play();
    }

    private void setControlsEnabled(boolean enabled) {
        foldBtn.setDisable(!enabled);
        checkBtn.setDisable(!enabled);
        raiseBtn.setDisable(!enabled);
        allInBtn.setDisable(!enabled);
        raiseSlider.setDisable(!enabled);
    }

    private Image loadCardImage(String name) {
        // Versucht Bild zu laden, Fallback auf Rückseite bei Fehler
        try {
            return new Image(getClass().getResourceAsStream("/cards/" + name + ".jpg"));
        } catch (Exception e) {
            System.err.println("Card image not found: " + name);
            return new Image(getClass().getResourceAsStream("/cards/backside.jpg"));
        }
    }

    private void createOverlayMenu(BorderPane root) {
        overlay = new StackPane();
        overlay.setVisible(false);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.8);");

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setStyle("-fx-background-color: #333; -fx-padding: 30; -fx-background-radius: 10; -fx-border-color: white;");
        menuBox.setMaxSize(300, 200);

        Button exitBtn = new Button("Spiel Beenden");
        Button resumeBtn = new Button("Zurück");

        String style = "-fx-font-size: 16; -fx-min-width: 150;";
        exitBtn.setStyle(style);
        resumeBtn.setStyle(style);

        resumeBtn.setOnAction(e -> overlay.setVisible(false));
        exitBtn.setOnAction(e -> {
            try {
                App.getSceneController().switchToMainMenu();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        menuBox.getChildren().addAll(new Label("Menü"){{setStyle("-fx-text-fill:white; -fx-font-size:20;");}}, resumeBtn, exitBtn);
        overlay.getChildren().add(menuBox);
    }
}