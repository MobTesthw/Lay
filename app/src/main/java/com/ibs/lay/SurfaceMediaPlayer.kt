package com.ibs.lay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

//  Usage:

//  <com.example.mediaplayerdemo_video.SurfaceMediaPlayer
//   android:id="@+id/surfaceMediaPlayer"
//   android:layout_width="0dp"
//   android:layout_height="0dp"
//   />

//  val surfaceMediaPlayer by lazy { findViewById<SurfaceMediaPlayer>(R.id.surfaceMediaPlayer) }

//  surfaceMediaPlayer.feedbackMessage={msg->
//     addMessage("surfaceMediaPlayer $msg")
//  }

//  surfaceMediaPlayer.setVideoUri(  Uri.parse("android.resource://" + packageName + "/" + R.raw.micro))
//  surfaceMediaPlayer?.mediaPlayer?.isLooping=true
//  surfaceMediaPlayer?.mediaPlayer?.start()


class SurfaceMediaPlayer(context: Context, attributeSet: AttributeSet): SurfaceView(context, attributeSet), SurfaceHolder.Callback  {

    var mediaPlayer:MediaPlayer?=null

    val clipPath= Path()
    //Listener to send status
    var feedbackMessage:((String)->Unit)?=null

    init {
        holder.addCallback(this)

        clipCircle(super.getWidth().toFloat()/2,super.getHeight().toFloat()/2,200f)


    }

    fun setVideoUri(uri: Uri){
        mediaPlayer=MediaPlayer.create(context,uri)
        val holder = this.holder
        holder.addCallback(this)
        feedbackMessage?.invoke("Uri assigned")
    }

    override fun dispatchDraw(canvas: Canvas?) {

        canvas?.clipPath(clipPath)

        super.dispatchDraw(canvas)
//        feedbackMessage?.invoke("Draw dispatched")

    }

    override fun surfaceCreated(holder: SurfaceHolder?) {

        mediaPlayer?.setDisplay(holder)
        holder?.setFormat(PixelFormat.TRANSPARENT)
        mediaPlayer?.start()
//        feedbackMessage?.invoke("surfaceCreated")
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
//        feedbackMessage?.invoke("surfaceChanged")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mediaPlayer?.release()
//        feedbackMessage?.invoke("surfaceDestroyed")
    }
    fun clipCircle(x:Float,y:Float,r:Float){
        clipPath.reset()
        clipPath.addCircle(x,y,r,Path.Direction.CW)
//        holder.setFormat(PixelFormat.TRANSPARENT)
        super.setWillNotDraw(false)
        super.invalidate()
        super.setWillNotDraw(true)
    }

}