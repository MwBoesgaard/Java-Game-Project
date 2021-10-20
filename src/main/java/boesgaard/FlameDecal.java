package boesgaard;

import javafx.scene.shape.Polygon;

public class FlameDecal extends Character {

    public FlameDecal(int x, int y, double size) {
        super(new Polygon(size*-6,0,0,0,size*6,0,size*10,size*-6,size*4,size*-4,0,size*-16,size*-4,size*-4,size*-10,size*-6), x, y);
        super.getCharacter().setRotate(180);
    }
    @Override
    public void move() {
    //Nothing
    }
}
