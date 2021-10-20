package boesgaard;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;

import java.util.Random;

public class BackgroundStar extends Character {

    public BackgroundStar (int x, int y) {
        super(new Polygon(1, -1, 1, 1, -1, 1, -1, -1), x, y);
        super.getCharacter().setStyle("-fx-fill: LIGHTGRAY");

        Random rnd = new Random();

        this.setMovement(new Point2D(0,(1+rnd.nextInt(2))/1.5));
    }

    @Override
    public void move() {
        this.getCharacter().setTranslateX(this.getCharacter().getTranslateX() + this.getMovement().getX());
        this.getCharacter().setTranslateY(this.getCharacter().getTranslateY() + this.getMovement().getY());

        if (this.getCharacter().getTranslateY() > AsteroidsApplication.HEIGHT) {
            this.setAlive(false);
        }
    }
}
