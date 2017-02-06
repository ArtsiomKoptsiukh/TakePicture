package bel.kaistra.takepicture;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CustomView extends View {

    private Paint canvasPaint;
    private Paint drawPaint;
    private int paintColor = 0xFF660000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private Float currentBrushSize = null;
    private Float lastBrushSize = null;
    Paint paint1;

    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private ArrayList<Path> generalUndonePaths = new ArrayList<Path>();
    private float mX;
    private float mY;
    private static final float TOUCH_TOLERANCE = 4;

    int width, height;

    private Path drawPath;
    private Path generalPath;
    private Paint generalPaint;
    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> generalPaths = new ArrayList<Path>();
    ArrayList<Float> brushSizes = new ArrayList<Float>();
    ArrayList<Float> undoneBrushSizes = new ArrayList<Float>();
    ArrayList<Integer> brushColors = new ArrayList<Integer>();
    private List<Integer> undoneColors = new ArrayList<>();


    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < paths.size(); i ++) {
            drawPaint.setStrokeWidth(brushSizes.get(i));
            drawPaint.setColor(brushColors.get(i));
            canvas.drawPath(paths.get(i), drawPaint);
        }
        canvas.drawPath(generalPath, generalPaint);
        if (paint1 != null){
            canvas.drawText("Test Android",150, 150,paint1);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void touchStart(float x, float y) {
        undonePaths.clear();
        drawPath.reset();
        drawPath.moveTo(x, y);
        drawPath.lineTo(x, y);
        generalPath.reset();
        generalPath.moveTo(x, y);
        generalPath.lineTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            generalPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        drawPath.lineTo(mX, mY);
        generalPath.lineTo(mX, mY);
        paths.add(drawPath);
        drawPaint.setStrokeWidth(currentBrushSize);
        brushSizes.add(currentBrushSize);
        brushColors.add(paintColor);
        drawPath = new Path();
        generalPath = new Path();

    }

    private void init() {
        currentBrushSize = getResources().getDimension(R.dimen.medium_size);
        lastBrushSize = currentBrushSize;

        drawPath = new Path();
        drawPaint = new Paint();
        generalPath = new Path();

        generalPaint = new Paint();
        generalPaint.setColor(paintColor);
        generalPaint.setAntiAlias(true);
        generalPaint.setStrokeWidth(currentBrushSize);
        generalPaint.setStyle(Paint.Style.STROKE);
        generalPaint.setStrokeJoin(Paint.Join.ROUND);
        generalPaint.setStrokeCap(Paint.Cap.ROUND);



        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(currentBrushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void eraseAll() {
        paths.removeAll(paths);
        brushColors.removeAll(brushColors);
        brushSizes.removeAll(brushSizes);
        undonePaths.removeAll(undonePaths);
        generalPaths.removeAll(generalPaths);
        generalUndonePaths.removeAll(generalUndonePaths);
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawPath = new Path();
        generalPath = new Path();
        this.setBackgroundColor(getResources().getColor(android.R.color.white));
        invalidate();

    }

    public void onClickUndo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
        }
        if (brushColors.size() > 0) {
            undoneColors.add(brushColors.remove(brushColors.size() - 1));
            invalidate();
        }
        if (brushSizes.size() > 0) {
            undoneBrushSizes.add(brushSizes.remove(brushSizes.size() - 1));
            invalidate();
        }
    }

    public void onClickRedo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        }
        if (undoneColors.size() > 0) {
            brushColors.add(undoneColors.remove(undoneColors.size() - 1));
            invalidate();
        }
        if (undoneBrushSizes.size() > 0) {
            brushSizes.add(undoneBrushSizes.remove(undoneBrushSizes.size() - 1));
            invalidate();
        }
    }

    public float getCurrentBrushSize() {
        return currentBrushSize;
    }

    public void setCurrentBrushSize(float newSize) {
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        lastBrushSize = currentBrushSize;
        currentBrushSize = newSize;
        generalPaint.setStrokeWidth(currentBrushSize);
        invalidate();
    }

    public void setLastBrushSize(float lastSize) {
        lastBrushSize = lastSize;
    }

    public void setColor(int color){
        paintColor = color;
        generalPaint.setColor(color);

    }

    public void setWaterMark() {
        paint1 = new Paint();
        paint1.setStrokeWidth(20);
        paint1.setColor(getResources().getColor(R.color.watermark));
        paint1.setTextSize(40);
        paint1.setFakeBoldText(true);
        paint1.setUnderlineText(true);
        paint1.setTextAlign(Paint.Align.CENTER);
        invalidate();
    }

    public void deleteWatermark() {
        paint1 = new Paint();
        paint1.setStrokeWidth(24);
        paint1.setColor(getResources().getColor(android.R.color.transparent));
        paint1.setTextSize(60);
        paint1.setTextAlign(Paint.Align.CENTER);
        invalidate();
    }
}