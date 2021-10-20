package boesgaard;

import javafx.scene.shape.Polygon;

public class Projectile extends Character {

    public static int maxProjectiles = 3;

    public Projectile(int x, int y) {
        super(new Polygon(2, -2, 2, 2, -2, 2, -2, -2), x, y);
        super.getCharacter().setStyle("-fx-fill: red");
    }

}