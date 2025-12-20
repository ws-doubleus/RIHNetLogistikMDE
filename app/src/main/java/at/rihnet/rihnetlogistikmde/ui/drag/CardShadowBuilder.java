package at.rihnet.rihnetlogistikmde.ui.drag;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

public class CardShadowBuilder extends View.DragShadowBuilder {

    private static final float CORNER_RADIUS  = 16f;   // px oder dp? → siehe unten
    private static final float SHADOW_RADIUS  = 18f;
    private static final int   SHADOW_ALPHA   = 40;
    private static final float PADDING_DP     = 12f;   // 12 dp Innenabstand

    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int paddingPx;                       // berechnetes Padding

    public CardShadowBuilder(@NonNull View view) {
        super(view);

        // dp → px
        paddingPx = Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        PADDING_DP,
                        view.getResources().getDisplayMetrics()));

        shadowPaint.setColor(0xFF000000);
        shadowPaint.setAlpha(SHADOW_ALPHA);
        shadowPaint.setMaskFilter(new BlurMaskFilter(SHADOW_RADIUS, BlurMaskFilter.Blur.NORMAL));

        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void onProvideShadowMetrics(Point outSize, Point outTouch) {
        View v = getView();
        int w  = v.getWidth()  + paddingPx * 2;
        int h  = v.getHeight() + paddingPx * 2;

        outSize.set(w, h);
        outTouch.set(w / 2, h / 2);   // Finger zentriert
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        /* 1) Schlagschatten */
        RectF rect = new RectF(0, 0,
                canvas.getWidth(),
                canvas.getHeight());
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, shadowPaint);

        /* 2) Original‑View – um Padding verschoben */
        canvas.save();
        canvas.translate(paddingPx, paddingPx);
        getView().draw(canvas);
        canvas.restore();
    }
}
