package bel.kaistra.takepicture;


import android.graphics.Paint;
import android.graphics.Path;

public class Drawing {
    private Paint paint;
    private Path path;


    public Drawing(Path path, Paint paint) {
        this.path = path;
        this.paint = paint;
    }



    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
