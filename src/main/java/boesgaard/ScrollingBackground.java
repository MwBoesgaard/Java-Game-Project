package boesgaard;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;

public class ScrollingBackground extends Character{
    public ScrollingBackground() {
        super(new Polygon(0,0,640,0,640,1600,0,1600), 0, -1120);
        super.getCharacter().setFill(new ImagePattern(new Image("file:src/main/img/GameScrollingBackground.png"))); //640 x 1600
    }

    @Override
    public void move() {
        this.getCharacter().setTranslateY(this.getCharacter().getTranslateY() + 0.2);
    }
}
