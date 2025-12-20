package at.rihnet.rihnetlogistikmde.ui.drag;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

public class PaketShadowBuilder extends View.DragShadowBuilder {
    private static final float SCALE = 1.0f;
    private static final int ALPHA  = 200;

    public PaketShadowBuilder(View view) {          //  ◄◄  NEU
        super(view);
    }

    @Override
    public void onProvideShadowMetrics(Point outSize, Point outTouch) {
        View v = getView();
        int w = (int) (v.getWidth()  * SCALE);
        int h = (int) (v.getHeight() * SCALE);

        outSize.set(w, h);
        outTouch.set(w / 2, h / 2);      // Touch‑Punkt in der Mitte
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        canvas.save();
        canvas.scale(SCALE, SCALE);
        //canvas.translate(5, 5);          // kleiner Offset = „Elevation“
        getView().setAlpha(ALPHA / 255f);
        getView().draw(canvas);
        canvas.restore();
    }
}
