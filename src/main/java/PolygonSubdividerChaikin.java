import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static parameters.Parameters.*;
import static save.SaveUtil.saveSketch;

public class PolygonSubdividerChaikin extends PApplet {
    public static void main(String[] args) {
        PApplet.main(PolygonSubdividerChaikin.class);
    }

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
        randomSeed(SEED);
        noiseSeed(floor(random(MAX_INT)));
    }

    @Override
    public void setup() {
        background(BACKGROUND_COLOR.red(), BACKGROUND_COLOR.green(), BACKGROUND_COLOR.blue());
        noLoop();

        Polygon.setPApplet(this);
    }

    @Override
    public void draw() {
        List<Polygon> polygons = new ArrayList<>();

        List<PVector> initialPolygon = new ArrayList<>();
        IntStream.range(0, NUMBER_OF_SIDES).forEach(i ->
                initialPolygon.add(PVector.fromAngle(i * TWO_PI / NUMBER_OF_SIDES)
                        .mult(POLYGON_RADIUS)
                        .add(WIDTH / 2f, HEIGHT / 2f)));
        polygons.add(new Polygon(initialPolygon));

        for (int k = 0; k < NUMBER_OF_CUTS; k++) {
            PVector p1, p2;
            if (coinFlip()) {
                p1 = new PVector(random(WIDTH), coinFlip() ? -MARGIN : HEIGHT + MARGIN);
            } else {
                p1 = new PVector(coinFlip() ? -MARGIN : WIDTH + MARGIN, random(HEIGHT));
            }
            if (coinFlip()) {
                p2 = new PVector(random(WIDTH), coinFlip() ? -MARGIN : HEIGHT + MARGIN);
            } else {
                p2 = new PVector(coinFlip() ? -MARGIN : WIDTH + MARGIN, random(HEIGHT));
            }

            for (int i = polygons.size() - 1; i >= 0; i--) {
                Polygon polygon = polygons.get(i);
                if (polygon.intersectsSegment(p1, p2)) {
                    polygons.addAll(polygon.cut(p1, p2));
                    polygons.remove(i);
                }
            }

            PVector normal = PVector.sub(p1, p2)
                    .rotate(HALF_PI)
                    .setMag(random(MINIMUM_DISPLACEMENT, MAXIMUM_DISPLACEMENT));
            for (Polygon polygon : polygons) {
                float sign = 0;
                for (PVector point : polygon.points()) {
                    float tmpSign = Polygon.sign(point, p1, p2);
                    if (abs(tmpSign) > abs(sign)) {
                        sign = tmpSign;
                    }
                }
                PVector displacement = PVector.mult(normal, sign > 0 ? 1 : -1);
                polygon.points().forEach(point -> point.sub(displacement));
            }
        }

        polygons.stream()
                .filter(polygon -> random(1) < CHANCE_OF_RENDER)
                .forEach(Polygon::render);

        saveSketch(this);
    }

    private boolean coinFlip() {
        return random(1) > .5f;
    }
}
