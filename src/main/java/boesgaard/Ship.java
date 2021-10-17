package boesgaard;

import javafx.scene.shape.Polygon;

public class Ship extends Character {

    public Ship(int x, int y) {
        super(new Polygon(-5, -5, 10, 0, -5, 5), x, y);

        for (int i = 0; i < 90; i++) {
            turnLeft();
        }

    }

    /*public Ship(int x, int y) {
        super(new Polygon(5, 5, 0, -10, -5, 5), x, y);
    }*/
}
