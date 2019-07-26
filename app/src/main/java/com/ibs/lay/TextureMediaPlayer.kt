package com.ibs.lay
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.SurfaceTexture
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Looper.prepare

import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView

//  Usage:

//    <com.example.mediaplayerdemo_video.TextureMediaPlayer
//        android:layout_width="0dp"
//        android:layout_height="0dp"
//        android:id="@+id/textureMediaPlayer"
//        />

//  val textureMediaPlayer by lazy { findViewById<TextureMediaPlayer>(R.id.textureMediaPlayer) }

//  textureMediaPlayer.feedbackMessage={msg->
//     addMessage("textureMediaPlayer $msg")
//  }

//  textureMediaPlayer.setVideoUri(  Uri.parse("android.resource://" + packageName + "/" + R.raw.micro_old))
//  textureMediaPlayer.alpha=0.5f
//  textureMediaPlayer?.mediaPlayer?.isLooping=true
//  textureMediaPlayer?.mediaPlayer?.start()

class TextureMediaPlayer(context: Context, attributeSet: AttributeSet): TextureView(context,attributeSet), TextureView.SurfaceTextureListener  {


    var mediaPlayer:MediaPlayer= MediaPlayer()

    val clipPath=Path()

    //Listener to send status
    var feedbackMessage:((String)->Unit)?=null

    init {
        clipPath.addCircle(10f,10f,100f,Path.Direction.CW)
//        mediaPlayer=MediaPlayer.create(context,null)


    }

    fun start(){
        mediaPlayer.start()
        feedbackMessage?.invoke("Media player start()")
    }
    fun pause(){
        mediaPlayer.pause()
        feedbackMessage?.invoke("Media player pause()")
    }

    fun seekTo(msec:Int){
        mediaPlayer.seekTo(msec)
        feedbackMessage?.invoke("Media player seekTo: $msec")
    }



    fun setVideoURI(uri: Uri){

       mediaPlayer.apply {
            reset()
            setDataSource(context, uri)
            prepare()
//            start()
        }
            feedbackMessage?.invoke("Media player uri set up")

        this.surfaceTextureListener=this

    }


    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        mediaPlayer.setSurface(Surface(surface))
        mediaPlayer.start()
        feedbackMessage?.invoke("Surface avaliable")

//        val drawThread=DrawThread(s!!)
//        drawThread.setRunning(true)
//        drawThread.start()

//        val path= Path()
//        path.addCircle(500f,500f,800f,Path.Direction.CW)


    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
//        val handler  = Handler(Looper.getMainLooper())
//        handler.post{println("dispatch $inc")}


//        val s=Surface(surface)
//        val canvas=s.lockCanvas(Rect(0,0,400,400))
//        val clipPath= Path()
//        clipPath.addCircle(50f,50f,100f,Path.Direction.CW)
//        canvas.clipPath(clipPath)
//        s.unlockCanvasAndPost(canvas)



//        synchronized(s) {
//            val canvas = s.lockCanvas(null)
//            val clipPath=Path()
//            //to simplify clipped fixed coordinates circle
//            clipPath.addCircle(100f,100f,300f,Path.Direction.CW)
//            canvas!!.clipPath(clipPath)
//            s.unlockCanvasAndPost(canvas)
//        }



    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {

        mediaPlayer.release()
        feedbackMessage?.invoke("surface destroyed")

        return false
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.clipPath(clipPath)
        super.dispatchDraw(canvas)


    }



}




//internal class DrawThread(private val surface: Surface) : Thread() {
//    private var isProcessing = false
//    private var previousTime: Long = 0
//
//    fun setRunning(run: Boolean) {
//        isProcessing = run
//    }
//
//    override fun run() {
//        var canvas: Canvas?
//        while (isProcessing) {
//            // Perform the clipping every 30 mS
//            val now = System.currentTimeMillis()
//            val elapsedTime = now - previousTime
//            if (elapsedTime > 30) {
//                previousTime = now
//            }
//            synchronized(surface) {
//                canvas = surface.lockCanvas(Rect(0,0,400,400))
//                val clipPath=Path()
//                //to simplify clipped fixed coordinates circle
//                clipPath.addCircle(100f,100f,300f,Path.Direction.CW)
//                canvas!!.clipPath(clipPath)
//                surface.unlockCanvasAndPost(canvas)
//            }
//        }
//    }
//}