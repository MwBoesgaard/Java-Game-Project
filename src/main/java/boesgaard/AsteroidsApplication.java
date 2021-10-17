package boesgaard;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
        Pane scorePane = new Pane();
        scorePane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        outerPane.getChildren().addAll(scorePane, pane);
        Scene scene = new Scene(outerPane);

        pane.setPrefSize(WIDTH, HEIGHT);
        Text text = new Text(10, 20, "Points: 0");
        Text ammoText = new Text(80, 20, "Ammo: 3");
        scorePane.getChildren().addAll(text, ammoText);

        AtomicInteger points = new AtomicInteger(0);

        List<Character> ships = new ArrayList<>();
        Ship ship = new Ship(WIDTH/2, HEIGHT-40);
        ships.add(ship);
        pane.getChildren().add(ship.getCharacter());

        List<Character> asteroids = new ArrayList<>();
        List<Character> projectiles = new ArrayList<>();
        List<Character> stars = new ArrayList<>();

        /** Generates the initial astroids on the screen */
        generateInitAsteroids(pane, asteroids, 5);

        /**Sets up logic for handling player input */
        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();
        scene.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);
        });
        scene.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });


        new AnimationTimer() {
            @Override
            public void handle(long now) {
                /**Goes through the pressedKey map to check what input has been provided since last update and performs the according action*/
                handlePlayerInput(pane, pressedKeys, ship, projectiles, ammoText);

                /**Calls the move function of each Character object in each of the lists. Accepts any number of parameters*/
                moveStep(ships, asteroids, projectiles, stars);

                /**Has a projectile hit an asteroid, if yes, set both objects to !Alive and update the score.*/
                hasProjectileHitAsteroid(projectiles, asteroids, text, points, ammoText);

                /**Removes not Alive objects from lists and interface.*/
                checkIfStillAliveStep(pane, projectiles);
                checkIfStillAliveStep(pane, stars);
                checkIfStillAliveStep(pane, asteroids);

                boolean gameOver = hasPlayerHitAsteroid(stage, pane, asteroids, ship, points);

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

            }
        }.start();

        stage.setScene(scene);
        stage.show();
    }
    public void handlePlayerInput(Pane paneOfChoice, Map<KeyCode, Boolean> mapOfPressedKeys, Ship playerShip, List<Character> listOfProjectiles, Text textToUpdateAmmoCountFor) {
        if (mapOfPressedKeys.getOrDefault(KeyCode.LEFT, false)) {
            playerShip.turnLeft();
        }

        if (mapOfPressedKeys.getOrDefault(KeyCode.RIGHT, false)) {
            playerShip.turnRight();
        }

        if (mapOfPressedKeys.getOrDefault(KeyCode.UP, false)) {
            playerShip.accelerate();
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
            Asteroid asteroid = new Asteroid(rnd.nextInt(WIDTH / 3), rnd.nextInt(HEIGHT));
            listOfAsteroids.add(asteroid);
        }
        listOfAsteroids.forEach(a -> paneOfChoice.getChildren().add(a.getCharacter()));
    }

    public void generateNewAsteroids(Pane paneOfChoice, List<Character> listOfAsteroids, Ship playerShip, double decimalChance) {
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

    public void checkIfStillAliveStep(Pane paneOfChoice, List<Character> listOfCharacterObjects) {
        listOfCharacterObjects.stream()
                .filter(projectile -> !projectile.isAlive())
                .forEach(projectile -> paneOfChoice.getChildren().remove(projectile.getCharacter()));
        listOfCharacterObjects.removeAll(listOfCharacterObjects.stream()
                .filter(projectile -> !projectile.isAlive())
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

    public boolean hasPlayerHitAsteroid(Stage stageOfChoice, Pane paneOfChoice, List<Character>listOfAsteroids, Ship playerShip, AtomicInteger pointFieldToUpdate) {
        AtomicBoolean playerHasBeenHit = new AtomicBoolean(false);
        listOfAsteroids.forEach(asteroid -> {
            if (playerShip.collide(asteroid)) {
                playerHasBeenHit.set(true);
            }
        });
    return playerHasBeenHit.get();
    } //(stage, pane, asteroids, ship, points);

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
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}