import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static parameters.Parameters.*;
import static processing.core.PConstants.CLOSE;

public record Polygon(List<PVector> points) {
    private static PApplet pApplet;

    public static void setPApplet(PApplet pApplet) {
        Polygon.pApplet = pApplet;
    }

    public static float sign(PVector a, PVector b, PVector c) {
        return (a.x - c.x) * (b.y - c.y) - (b.x - c.x) * (a.y - c.y);
    }

    public void render() {
        List<PVector> curve = new ArrayList<>(points);
        for (int k = 0; k < CHAIKIN_DEPTH; k++) {
            if (k > 0) {
                curve = chaikin(curve);
            }
            pApplet.stroke(STROKE_COLOR.red(), STROKE_COLOR.green(), STROKE_COLOR.blue(), STROKE_COLOR.alpha());
            pApplet.fill(BACKGROUND_COLOR.red(), BACKGROUND_COLOR.green(), BACKGROUND_COLOR.blue());
            pApplet.beginShape();
            curve.forEach(point -> pApplet.vertex(point.x, point.y));
            pApplet.endShape(CLOSE);
        }
    }

    public boolean intersectsSegment(PVector p1, PVector p2) {
        for (int i = 0; i < points.size(); i++) {
            if (intersection(p1, p2, points.get(i), points.get((i + 1) % points.size())) != null) {
                return true;
            }
        }
        return false;
    }

    public List<Polygon> cut(PVector p1, PVector p2) {
        List<PVector> negativeSide = new ArrayList<>();
        List<PVector> positiveSide = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            PVector intersection = intersection(p1, p2, points.get(i), points.get((i + 1) % points.size()));
            if (sign(points.get(i), p1, p2) > 0) {
                positiveSide.add(points.get(i));
            } else {
                negativeSide.add(points.get(i));
            }
            if (intersection != null) {
                negativeSide.add(intersection.copy());
                positiveSide.add(intersection.copy());
            }
        }
        ArrayList<Polygon> newPolygons = new ArrayList<>();
        newPolygons.add(new Polygon(positiveSide));
        newPolygons.add(new Polygon(negativeSide));
        return newPolygons;
    }

    private PVector intersection(PVector p1, PVector p2, PVector q1, PVector q2) {
        float uA = ((q2.x - q1.x) * (p1.y - q1.y) - (q2.y - q1.y) * (p1.x - q1.x))
                / ((q2.y - q1.y) * (p2.x - p1.x) - (q2.x - q1.x) * (p2.y - p1.y));
        float uB = ((p2.x - p1.x) * (p1.y - q1.y) - (p2.y - p1.y) * (p1.x - q1.x))
                / ((q2.y - q1.y) * (p2.x - p1.x) - (q2.x - q1.x) * (p2.y - p1.y));

        if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
            return new PVector(p1.x + uA * (p2.x - p1.x), p1.y + uA * (p2.y - p1.y));
        }
        return null;
    }

    private List<PVector> chaikin(List<PVector> curve) {
        ArrayList<PVector> newCurve = new ArrayList<>();
        for (int i = 0; i < curve.size(); i++) {
            PVector p = curve.get(i);
            PVector q = curve.get((i + 1) % curve.size());
            newCurve.add(PVector.add(PVector.mult(p, 1 - CHAIKIN_PERCENTAGE), PVector.mult(q, CHAIKIN_PERCENTAGE)));
            newCurve.add(PVector.add(PVector.mult(p, CHAIKIN_PERCENTAGE), PVector.mult(q, 1 - CHAIKIN_PERCENTAGE)));
            pApplet.noStroke();
            pApplet.fill(0);
            pApplet.beginShape();
            {
                pApplet.vertex(p.x, p.y);
                pApplet.vertex(p.x + CHAIKIN_PERCENTAGE * (q.x - p.x), p.y + CHAIKIN_PERCENTAGE * (q.y - p.y));
                PVector r = curve.get(i > 0 ? i - 1 : curve.size() - 1);
                pApplet.vertex(p.x + CHAIKIN_PERCENTAGE * (r.x - p.x), p.y + CHAIKIN_PERCENTAGE * (r.y - p.y));
            }
            pApplet.endShape(CLOSE);
        }
        return newCurve;
    }
}
