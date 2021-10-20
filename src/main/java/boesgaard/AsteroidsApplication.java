package boesgaard;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AsteroidsApplication extends Application {

    public static int WIDTH = 640;
    public static int HEIGHT = 480;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Asteroids!");

        VBox outerPane = new VBox();
        Pane pane = new Pane();
        pane.setStyle("-fx-background-color: rgb(112,146,190)");
        Pane scorePane = new Pane();
        scorePane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        scorePane.setStyle("-fx-background-color: GREY");
        scorePane.setPrefSize(WIDTH, 30);
        outerPane.getChildren().addAll(scorePane, pane);
        Scene gameScene = new Scene(outerPane);

        ScrollingBackground scrollingBG = new ScrollingBackground();
        pane.getChildren().add(scrollingBG.getCharacter());
        List<Character> listWithScrollingBG = new ArrayList<>();
        //scrollingBG.getCharacter().toBack();
        listWithScrollingBG.add(scrollingBG);

        pane.setPrefSize(WIDTH, HEIGHT);
        Text text = new Text(10, 20, "Points: 0");
        Text ammoText = new Text(110, 20, "Ammo: 3");
        Text speedText = new Text(210,20,"Speed: 0");
        scorePane.getChildren().addAll(text, ammoText, speedText);

        AtomicInteger points = new AtomicInteger(0);

        List<Character> ships = new ArrayList<>();
        Character ship = new Ship(WIDTH/2, HEIGHT-40);
        ships.add(ship);
        pane.getChildren().add(ship.getCharacter());

        List<Character> asteroids = new ArrayList<>();
        List<Character> projectiles = new ArrayList<>();
        List<Character> stars = new ArrayList<>();
        List<Character> flames = new ArrayList<>();

        /** Generates the initial astroids on the screen */
        generateInitAsteroids(pane, asteroids, 5);

        /**Sets up logic for handling player input */
        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();
        gameScene.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);
        });
        gameScene.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });

        AnimationTimer gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                /**Goes through the pressedKey map to check what input has been provided since last update and performs the according action*/
                handlePlayerInput(pane, pressedKeys, ship, projectiles, flames, ammoText, speedText);

                /**Calls the move function of each Character object in each of the lists. Accepts any number of parameters*/
                moveStep(ships, asteroids, projectiles, stars, listWithScrollingBG);


                /**Has a projectile hit an asteroid, if yes, set both objects to !Alive and update the score.*/
                hasProjectileHitAsteroid(projectiles, asteroids, text, points, ammoText);

                /**Removes not Alive objects from lists and interface.*/
                checkIfStillAliveStep(pane, projectiles);
                checkIfStillAliveStep(pane, stars);
                checkIfStillAliveStep(pane, asteroids);
                checkIfStillAliveStep(pane, flames);

                boolean gameOver = hasPlayerHitAsteroid(asteroids, ship);

                if (gameOver) {
                    stop();
                    gameOverScreen(stage, pane, points);

                    Button replayButton = new Button("Play Again!");
                    replayButton.setTranslateY(HEIGHT/2+20);
                    replayButton.setTranslateX(WIDTH/2-35);
                    pane.getChildren().add(replayButton);
                    replayButton.setOnAction(event -> {
                        try {
                            AsteroidsApplication.this.start(new Stage());
                            stage.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                generateNewAsteroids(pane, asteroids, ship, 0.005);

                generateBackgroundStars(pane, stars);

                scorePane.toFront();
                scrollingBG.getCharacter().toBack();

            }
        };

        VBox menuPane = new VBox();
        Button startGameButton = new Button("Start Game");
        Button optionMenuButton = new Button ("Options");
        Button quitMenuButton = new Button("Quit Game");
        startGameButton.setStyle("-fx-base: steelblue; -fx-font: 22 Helvetica");
        optionMenuButton.setStyle("-fx-base: steelblue; -fx-font: 22 Helvetica");
        quitMenuButton.setStyle("-fx-base: steelblue; -fx-font: 22 Helvetica");
        startGameButton.setOnAction(e -> startGame(stage, gameScene, gameTimer));
        quitMenuButton.setOnAction(e -> stage.close());

        menuPane.setSpacing(50);
        menuPane.getChildren().addAll(startGameButton, optionMenuButton, quitMenuButton);
        menuPane.setAlignment(Pos.CENTER);
        menuPane.setStyle("-fx-background-image: url(file:src/main/img/GameMenuBackground.png)");
        Scene menuScene = new Scene(menuPane, 640, 480);

        stage.setScene(menuScene);
        stage.show();
    }

    public static void startGame(Stage stage, Scene gameScene, AnimationTimer gameTimer) {
        stage.setScene(gameScene);
        gameTimer.start();
    }

    public void handlePlayerInput(Pane paneOfChoice, Map<KeyCode, Boolean> mapOfPressedKeys, Character playerShip, List<Character> listOfProjectiles, List<Character> listOfFlames, Text textToUpdateAmmoCountFor, Text speedText) {
        if (mapOfPressedKeys.getOrDefault(KeyCode.LEFT, false)) {
            playerShip.turnLeft();
        }

        if (mapOfPressedKeys.getOrDefault(KeyCode.RIGHT, false)) {
            playerShip.turnRight();
        }

        if (mapOfPressedKeys.getOrDefault(KeyCode.UP, false)) {
            double playerSpeed = playerShip.getMovement().magnitude();
            if (playerSpeed < 3.0) {
                playerShip.accelerate();
            } else {
                Point2D currentVector = playerShip.getMovement();
                playerShip.setMovement(currentVector.multiply(0.999));
            }
            long speedTextValue = playerSpeed < 3.0 ? Math.round(playerSpeed/3*100) : 100;
            speedText.setText("Speed: " + speedTextValue + "%");
            addFlameDecal(paneOfChoice, playerShip, listOfFlames);
        } else {
            listOfFlames.stream().forEach(e->e.setAlive(false));
        }
        if (mapOfPressedKeys.getOrDefault(KeyCode.SPACE, false) && listOfProjectiles.size() < Projectile.maxProjectiles) {
            Projectile projectile = new Projectile((int) playerShip.getCharacter().getTranslateX(), (int) playerShip.getCharacter().getTranslateY());
            projectile.getCharacter().setRotate(playerShip.getCharacter().getRotate());
            listOfProjectiles.add(projectile);
            textToUpdateAmmoCountFor.setText("Ammo: " + (Projectile.maxProjectiles - listOfProjectiles.size()));

            projectile.accelerate();
            projectile.setMovement(projectile.getMovement().normalize().multiply(3));

            paneOfChoice.getChildren().add(projectile.getCharacter());
        }
        mapOfPressedKeys.remove(KeyCode.SPACE);
    }

    public void generateInitAsteroids(Pane paneOfChoice, List<Character> listOfAsteroids, int numberOfAsteroidsToAdd) {
        for (int i = 0; i < numberOfAsteroidsToAdd; i++) {
            Random rnd = new Random();
            Asteroid asteroid = new Asteroid(rnd.nextInt(WIDTH), rnd.nextInt(HEIGHT/4));
            listOfAsteroids.add(asteroid);
        }
        listOfAsteroids.forEach(a -> paneOfChoice.getChildren().add(a.getCharacter()));
    }

    public void generateNewAsteroids(Pane paneOfChoice, List<Character> listOfAsteroids, Character playerShip, double decimalChance) {
        if(Math.random() < decimalChance) {
            Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);
            if(!asteroid.collide(playerShip)) {
                listOfAsteroids.add(asteroid);
                paneOfChoice.getChildren().add(asteroid.getCharacter());
            }
        }
    }

    public void moveStep(List<Character>... listOfMovables) {
        for (List<Character> i : listOfMovables) {
            i.forEach(e -> e.move());
        }
    }

    public void addFlameDecal(Pane paneOfChoice, Character playerShip, List<Character> listOfFlames) {
        listOfFlames.stream().forEach(e -> e.setAlive(false));
        int playerX = (int) playerShip.getCharacter().getTranslateX();
        int playerY = (int) playerShip.getCharacter().getTranslateY();
        double playerRot = playerShip.getCharacter().getRotate();
        FlameDecal flameOuter = new FlameDecal(playerX+3, playerY+25,1.0);
        FlameDecal flameInner = new FlameDecal(playerX+3, playerY+16,0.4);
        flameOuter.getCharacter().setFill(Color.ORANGE);
        flameInner.getCharacter().setFill(Color.RED);
        listOfFlames.add(flameOuter);
        listOfFlames.add(flameInner);
        paneOfChoice.getChildren().add(flameOuter.getCharacter());
        paneOfChoice.getChildren().add(flameInner.getCharacter());
    }

    public void checkIfStillAliveStep(Pane paneOfChoice, List<Character> listOfCharacterObjects) {
        listOfCharacterObjects.stream()
                .filter(e -> !e.isAlive())
                .forEach(e -> paneOfChoice.getChildren().remove(e.getCharacter()));
        listOfCharacterObjects.removeAll(listOfCharacterObjects.stream()
                .filter(e -> !e.isAlive())
                .collect(Collectors.toList()));
    }

    public void hasProjectileHitAsteroid(List<Character> listOfProjectiles, List<Character>listOfAsteroids, Text pointFieldToUpdate, AtomicInteger pointTracker, Text ammoFieldToUpdate) {
        listOfProjectiles.forEach(projectile -> {
            listOfAsteroids.forEach(asteroid -> {
                if(projectile.collide(asteroid)) {
                    projectile.setAlive(false);
                    asteroid.setAlive(false);
                }
            });
            if(!projectile.isAlive()) {
                pointFieldToUpdate.setText("Points: " + pointTracker.addAndGet(1000));
                ammoFieldToUpdate.setText("Ammo: " + (3 - listOfProjectiles.size()));
            }
        });
    }

    public boolean hasPlayerHitAsteroid(List<Character>listOfAsteroids, Character playerShip) {
        AtomicBoolean playerHasBeenHit = new AtomicBoolean(false);
        listOfAsteroids.forEach(asteroid -> {
            if (playerShip.collide(asteroid)) {
                playerHasBeenHit.set(true);
            }
        });
    return playerHasBeenHit.get();
    }

    public void gameOverScreen(Stage stageOfChoice, Pane paneOfChoice, AtomicInteger pointFieldToUpdate) {
        Text gameOverText = new Text(WIDTH/2-85,HEIGHT/2-20,"GAME OVER!" + "\n" + "Your score was: " + pointFieldToUpdate);
        gameOverText.setTextAlignment(TextAlignment.CENTER);
        gameOverText.setFont(Font.font("arial", FontWeight.BOLD, 18));
        gameOverText.setFill(Color.DARKRED);
        paneOfChoice.getChildren().add(gameOverText);
        stageOfChoice.setTitle("GAME OVER!");
    }

    public void generateBackgroundStars(Pane paneOfChoice, List<Character> listOfStars) {
        Random rnd = new Random();
        if (rnd.nextInt(100) > 95) {
            BackgroundStar star = new BackgroundStar(rnd.nextInt(WIDTH), 0);
            listOfStars.add(star);
            paneOfChoice.getChildren().add(star.getCharacter());
            star.getCharacter().toBack();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}