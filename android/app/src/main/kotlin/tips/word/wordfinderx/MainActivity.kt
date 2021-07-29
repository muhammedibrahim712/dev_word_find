package tips.word.wordfinderx

import android.graphics.Bitmap
import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.DrawableRes
import io.flutter.embedding.android.FlutterActivity
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs.imread
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.findContours

class MainActivity: FlutterActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)


    }


    fun findBoard(){
        val image = imread("image.png")
        val dstImage = image
        image.copyTo(dstImage)

        val image_w = image.cols()
        val image_h = image.cols()

        val left = arrayOf(image_w,0,0,0)
        val right = arrayOf(0,0,0,0)
        val top = arrayOf(0,image_h,0,0)
        val top_2nd = top
        val bottom = arrayOf(0,0,0,0)

        val grayImage= dstImage

        Imgproc.cvtColor(image,grayImage,Imgproc.COLOR_GRAY2BGR)


        val blurImage = dstImage

        Imgproc.GaussianBlur(grayImage, blurImage, Size(3.0, 3.0), 0.0)

        val edges = dstImage;

        Imgproc.Canny(blurImage,edges,30.0,90.0)

        val letters = null;
        edges.copyTo(letters)

        //Imgproc.adaptiveThreshold(image, dstImage, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2)


        val hierarchy = Mat()
        val contours: List<MatOfPoint> = ArrayList()

        findContours(letters, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE)

        //find biggest

        //find biggest
        for (x in contours.indices) {
            val rect : MatOfPoint? =null
            Imgproc.boundingRect(rect)
            val w = rect?.width()
            val h = rect?.height()

        }





    }
}