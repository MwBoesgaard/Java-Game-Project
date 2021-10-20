package boesgaard;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import java.awt.*;
import java.util.Random;

public class Asteroid extends Character {

    private double rotationalMovement;

    public Asteroid(int x, int y) {
        super(new PolygonFactory().createPolygon(), x, y);
        super.getCharacter().setFill(new ImagePattern(new Image("file:src/main/img/AsteroidTexture.png")));
        //super.getCharacter().setStyle("-fx-fill: GREY");

        Random rnd = new Random();

        super.getCharacter().setRotate(rnd.nextInt(360));

        int accelerationAmount = 1 + rnd.nextInt(10);
        for (int i = 0; i < accelerationAmount; i++) {
            accelerate();
        }

        this.rotationalMovement = 0.5 - rnd.nextDouble();
    }

    @Override
    public void move() {
        super.move();
        super.getCharacter().setRotate(super.getCharacter().getRotate() + rotationalMovement);
    }
}