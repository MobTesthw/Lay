package com.ibs.lay

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.net.Uri

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.constraint.ConstraintLayout
import android.support.constraint.Group
import android.support.v4.view.GestureDetectorCompat
import android.view.*
import android.widget.*
import com.ibs.lay.ImageCoordinateTranslation.Companion.globalScreenToOriginX
import com.ibs.lay.ImageCoordinateTranslation.Companion.originToLocalScreenX
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
//import android.widget.textureMediaPlayer



//     Temporary Raw resource exchange
//     Offset while scroll on progressbar

// V    Delay black background and LAY to 2000
// V    After completing last section video goes to begin
//      Terms notation
//      Delete inc and inc_msg (for debug)
//      Progress bar fill whole at the end and begin (few pixels)
//      TextVew Gone
// V    Button reference
//      Backward play delete

//      <activity android:name=".MainActivity"
//      android:screenOrientation="landscape"	>


class MainActivity : AppCompatActivity() {
    private companion object {
        const val BACKWARD_UPDATE_INTERVAL=33L  //mS
        const val FORWARD_LATENCY_INTERVAL=33L  //mS
//        const val FPS=30
    }

    val progressBar = Progressbar()

    //Application stages
    val stageSet = StagesSet()
    val current=CurrentStage(stageSet.StartStage())


    //For debugging
    var inc=0
    var inc_msg=0
    //For rotate button
    var previousX:Float=0f
//    var previousY:Float=0f
    var videoOnDownPosition=0
    var currentPlayPosition=0
    var progressBarPosition=0
    var progressBarDownX=0f
    var progressBarPositionBeforeDown=0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.visibility=View.GONE

        setListeners()
        current.changeStage(stageSet.HeartBeat())


        textureMediaPlayer.feedbackMessage={msg->
            addMessage("textureMediaPlayer $msg")
        }



    }

    override fun onResume() {
        super.onResume()
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        //Prevent screen from dimming
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        current.changeStage(stageSet.HeartBeat())
    }

    override fun onPause() {
        super.onPause()
        progressBar.pause()
    }

    interface Stage{

        fun onStartStage(){}
        //Listeners at screen
        fun onDown(x:Float,y:Float){}
        fun onMove(x:Float,y:Float){}
        fun onUp(x:Float,y:Float){}
        //Listeners at progressbar
        fun onProgressbarDown(x:Float, y:Float){}
        fun onProgressbarMove(x:Float, y:Float){}
        fun onProgressbarUp(x:Float, y:Float){}
        fun onProgressbarSingleTap(x:Float, y:Float){}
        fun onProgressbarDoubleTap(x:Float, y:Float){}
        fun onProgressbarScroll(x:Float, y:Float){}

        fun onSectionPlayComplete(){}


        fun onVideoComplete(){}
        fun onCompleteStage(){}


    }


    class CurrentStage(var stage:Stage){
        lateinit var previousStage:Stage

        fun changeStage(newStage:Stage){
            previousStage=stage
            stage.onCompleteStage()
            stage=newStage
            stage.onStartStage()
        }

    }

    inner class StagesSet {
        inner class
        StartStage : Stage
        //______________________________________________________________________________________________________________
        inner class
        HeartBeat : Stage {
            private var isFirstClick=true

            override fun onStartStage() {
//                Toast.makeText(applicationContext, "Stage: ${current.stage.javaClass.simpleName}\nPrevious: ${current.previousStage.javaClass.simpleName}", Toast.LENGTH_SHORT).show()

                groupPlayer.visibility=View.GONE
                ivBackground.visibility=View.GONE
//                ibMicroscope.visibility=View.INVISIBLE
                rotateButton.visibility=View.INVISIBLE
                surfaceMediaPlayer.visibility=View.GONE
                ivBackground.setImageResource(R.drawable.title)
                textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.b_introloop))
                textureMediaPlayer.mediaPlayer.isLooping = true
                textureMediaPlayer.start()
                DecorateTransition.alphaShow(ivBackground, 2800, fun() { textureMediaPlayer.start() })
            }

            override fun onDown(x: Float, y: Float) {
                if(isFirstClick){
                    DecorateTransition.alphaHide(ivBackground, 1800, fun() {
                        current.changeStage(stageSet.FromHeartToVessel())
                    })
                    isFirstClick=false
                }



            }


        }
        //______________________________________________________________________________________________________________
        inner class
        FromHeartToVessel:Stage{

            override fun onStartStage(){
                Toast.makeText(applicationContext, "Stage: ${current.stage.javaClass.simpleName}\nPrevious: ${current.previousStage.javaClass.simpleName}", Toast.LENGTH_SHORT).show()
                textureMediaPlayer.mediaPlayer.isLooping = false
//                textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.a_introlong))
                textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.fast))
                textureMediaPlayer.start()

            }

            override fun onVideoComplete() {
                current.changeStage(stageSet.PlaySection(0))

            }
        }
        //______________________________________________________________________________________________________________
        inner class
        PlaySection(private val endSectionId:Int):Stage{
            private var isRotationVideoPrepared = false

            private var subState=SubState.Play




            override fun onStartStage(){

                if(groupPlayer.visibility!=View.VISIBLE)groupPlayer.visibility=View.VISIBLE
                currentPlayPosition=progressBar.sections[endSectionId].t2
                textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.main))
                textureMediaPlayer.start()
                textureMediaPlayer.pause()
                textureMediaPlayer.seekTo(currentPlayPosition)
                textureMediaPlayer.mediaPlayer.setOnPreparedListener {
                    it.setOnSeekCompleteListener {
                        progressBar.start(endSectionId)
                    }
                }
//                textureMediaPlayer.setOnPreparedListener{}


            }

            override fun onProgressbarScroll(x: Float, y: Float) {
                subState=SubState.Play
                progressBar.pause()
                progressBar.moveTo(x)
            }

            override fun onProgressbarSingleTap(x: Float, y: Float) {
                subState=SubState.Play
                 progressBar.playFromTappedSectionStart(x)
            }

            override fun onProgressbarDoubleTap(x: Float, y: Float) {
                subState=SubState.Play
                progressBar.playFromTappedSectionEnd(x)
            }

            override fun onSectionPlayComplete() {
//                addMessage("section completed\n")
                progressBar.pause()
                currentPlayPosition=textureMediaPlayer.mediaPlayer.currentPosition

                rotateButton.setImageResource(R.drawable.rotate)
                moveRotateButtonToCenter()

                if(rotateButton.visibility!=View.VISIBLE)DecorateTransition.alphaShow(rotateButton,1000 )
                setLoopVideo()
            }
            override fun onVideoComplete() {
//                addMessage("video completed")
            }

            override fun onProgressbarDown(x: Float, y: Float) {
                if(ibMicroscope.visibility==View.VISIBLE)ibMicroscope.visibility=View.INVISIBLE

                progressBarDownX=x
                progressBarPositionBeforeDown=progressBarPosition
            }

            override fun onDown(x: Float, y: Float) {
                var adoptedY=y
                if(ibMicroscope.visibility==View.VISIBLE)ibMicroscope.visibility=View.INVISIBLE

                if(subState==SubState.Loop || subState==SubState.Rotate  ){
                    if(isRotationVideoPrepared){
                        previousX = x
                        videoOnDownPosition=textureMediaPlayer.mediaPlayer.currentPosition

                        if(y>frameLayout.top){
                            adoptedY=frameLayout.top.toFloat()
                    }
                        moveRotateButtonTo(x, adoptedY,300)
                    }
                    if(subState==SubState.Loop)    setRotateVideo()
                }

            }

            override fun onMove(x: Float, y: Float) {
                var adoptedY=y
                if(subState==SubState.Rotate){
                    val duration =textureMediaPlayer.mediaPlayer.duration
                    val dx= previousX-x
                    val dt=dx*duration/layout.width
                    val position= videoOnDownPosition +dt


                    if(position<0){
                        previousX=x
                    }
                    else if(position>duration){
                        previousX=x
                    }
                    else if(y>frameLayout.top){
                        adoptedY=frameLayout.top.toFloat()
                    }
                    else {
                        textureMediaPlayer.seekTo(position.toInt())
                        //For updating during move
                        textureMediaPlayer.start()
                        textureMediaPlayer.pause()
                        rotateButton.setImageResource(R.drawable.rotate)
                    }

                    //If rotate button is about center of screen - fire it
                    if (isRotateButtonAtCenter(x,adoptedY))rotateButton.setImageResource(R.drawable.rotate_centered)
                    else  rotateButton.setImageResource(R.drawable.rotate)
                    moveRotateButtonTo(x, adoptedY)
                }


            }
            override fun onUp(x: Float, y: Float) {
                if (isRotateButtonAtCenter(x,y)){
                    val handler = Handler()
                    handler.postDelayed({
                        moveRotateButtonToCenter()
                    }, BACKWARD_UPDATE_INTERVAL)
//
                    onSectionPlayComplete()
                }
            }


            private fun setLoopVideo(){
                subState=SubState.Loop
                when(progressBar.currentSectionId){
                    0->textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.cl_healthy))
                    1->textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.dl_lipid))
                    2->textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.el_damage))
                    3->textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.fl_streaks))
                    4->textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.gl_plaque))
                    5->textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.hl_foam))
                    6->{
                        textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.il_core))
                        ibMicroscope.visibility=View.VISIBLE

                    }

                    else->{
//                        isRotateSubstrate=true
//                        addMessage("${progressBar.currentSectionId} section go over loop")
                    }
                }
                textureMediaPlayer.mediaPlayer.isLooping = true
                textureMediaPlayer.start()

            }

            private fun setRotateVideo(){
                subState=SubState.Rotate
                if (textureMediaPlayer.mediaPlayer.isPlaying)textureMediaPlayer.pause()
                textureMediaPlayer.mediaPlayer.setOnPreparedListener{
                    it.isLooping=false
                    textureMediaPlayer.seekTo(textureMediaPlayer.mediaPlayer.duration/2)
                    videoOnDownPosition=textureMediaPlayer.mediaPlayer.currentPosition
                    isRotationVideoPrepared=true
                }
                textureMediaPlayer.pause()
                when(progressBar.currentSectionId){
                    0->{textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.cr_healthy))}
                    1->{textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.dr_lipid))}
                    2->{textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.er_damage))}
                    3->{textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.fr_streaks))}
                    4->{textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.gr_plaque))}
                    5->{textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.hr_foam))}
                    6->{textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.ir_core))}
                }
                textureMediaPlayer.seekTo(textureMediaPlayer.mediaPlayer.duration/2)
                textureMediaPlayer.start()
                textureMediaPlayer.pause()


            }

            private fun moveRotateButtonTo(x: Float, y: Float, duration:Long=0){
                rotateButton.animate()
                    .x(x  - rotateButton.width/2)
                    .y(y - rotateButton.height/2)
                    .setDuration(duration)
                    .start()
            }
            private fun moveRotateButtonToCenter(){
                rotateButton.x=layout.width.toFloat()/2-rotateButton.width.toFloat()/2
                rotateButton.y=layout.height.toFloat()/2-rotateButton.height.toFloat()/2
            }
        }

    }
        //______________________________________________________________________________________________________________

    private fun isRotateButtonAtCenter(x: Float, y: Float):Boolean{
        val part=2
        val deltaW=(rotateButton.width /part)
        val deltaH=(rotateButton.height/part)
        return  (x).toInt() in (layout.width /2-deltaW)..(layout.width/2 +deltaW) &&
                (y).toInt() in (layout.height/2-deltaH)..(layout.height/2+deltaH)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners(){

        layout.setOnTouchListener{_: View, m: MotionEvent ->
            val action = m.action
            val evx = m.x
            val evy = m.y

            when (action) {
                MotionEvent.ACTION_DOWN -> current.stage.onDown(evx,evy)
                MotionEvent.ACTION_UP ->   current.stage.onUp(evx,evy)
                MotionEvent.ACTION_MOVE -> current.stage.onMove(evx,evy)
            }
            true           //to keep onClick listeners working
        }

        mDetector = GestureDetectorCompat(frameLayout.context, object:GestureDetector.OnGestureListener{

            override fun onShowPress(e: MotionEvent?) {
//                addMessage("onShowPress x: ${e!!.x.toInt() }  y: ${e.y.toInt()}")
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
//                addMessage("onSingleTapUp x: ${e!!.x.toInt() }  y: ${e.y.toInt()}")
                return true
            }

            override fun onDown(e: MotionEvent?): Boolean {
//                addMessage("onDown x: ${e!!.x.toInt() }  y: ${e.y.toInt()}")
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
//                addMessage("onFling x: ${e2!!.x.toInt() }  y: ${e2!!.y.toInt()}")
                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                current.stage.onProgressbarScroll(e2!!.x,e2.y)
//                addMessage("onScroll x: ${e2!!.x.toInt() }  y: ${e2.y.toInt()}")
                return true
            }

            override fun onLongPress(e: MotionEvent?) {
//                addMessage("onLongPress x: ${e!!.x.toInt() }  y: ${e.y.toInt()}")
            }

        })
        mDetector.setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
//                addMessage("onDoubleTap x: ${e!!.x.toInt() }  y: ${e.y.toInt()}")
                current.stage.onProgressbarDoubleTap(e!!.x,e.y)
                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
//                addMessage("onDoubleTapEvent")
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                current.stage.onProgressbarSingleTap(e!!.x,e.y)
//                addMessage("onSingleTapConfirmed")
                return true
            }

        })


        frameLayout.setOnTouchListener{_: View, m: MotionEvent ->

            val action = m.action
            val evx = m.x
            val evy = m.y

            when (action) {
                MotionEvent.ACTION_DOWN -> current.stage.onProgressbarDown(evx,evy)
                MotionEvent.ACTION_UP ->   current.stage.onProgressbarUp(evx,evy)
                MotionEvent.ACTION_MOVE -> current.stage.onProgressbarMove(evx,evy)
            }

            mDetector.onTouchEvent(m)

            true


        }


        ibPlay.setOnTouchListener{_: View, m: MotionEvent ->
            val tag = ibPlay.tag as Int
            val res:Int=when(tag){
                R.drawable.play -> R.drawable.playdown
                R.drawable.playall -> R.drawable.playalldown
                R.drawable.playallpause -> R.drawable.playallpausedown
                else ->R.drawable.playpause_down
            }

            when (m.action) {
                MotionEvent.ACTION_DOWN ->{ibPlay.setImageResource(res)}
                MotionEvent.ACTION_UP ->  {
                    ibPlay.setImageResource(tag)
                    if(progressBar.isUpdating){
                        progressBar.pause()
                    }
                    else {

//                        val t1=progressBar.sections[1].t1
//                        textureMediaPlayer.seekTo(t1)
//                        currentPlayPosition=t1
                        progressBar.start(6)

                    }
                }

            }
            false
        }
//        ibPlay.setOnLongClickListener {_:View->                 //Play All
//            if (textureMediaPlayer.mediaPlayer!!.isPlaying)textureMediaPlayer.pause()
//            textureMediaPlayer.seekTo(0)
//            progressBar.start(6)
//        true
//        }
        ibMicroscope.setOnTouchListener{_: View, m: MotionEvent ->
            when (m.action) {

                MotionEvent.ACTION_DOWN ->ibMicroscope.setImageResource(R.drawable.microscope_down)
                MotionEvent.ACTION_UP -> {
                    val res=when(microscopeOn){
                        true  -> R.drawable.microscope_on
                        false -> R.drawable.microscope_off
                    }
                    if(microscopeOn){
//                        textureMediaPlayer.visibility=View.GONE


                        surfaceMediaPlayer.setVideoUri(  Uri.parse("android.resource://" + packageName + "/" + R.raw.micro))
                        surfaceMediaPlayer?.mediaPlayer?.isLooping=true
                        surfaceMediaPlayer?.mediaPlayer?.start()
                        surfaceMediaPlayer.holder.setFormat(PixelFormat.TRANSPARENT)
//                        ivBackground.visibility=View.VISIBLE
                        ivBackground.visibility=View.INVISIBLE
                        rotateButton.visibility=View.INVISIBLE

                        ivBackground.alpha=1f
//                        textureMediaPlayer.visibility=View.INVISIBLE
//                        surfaceMediaPlayer.setZOrderOnTop(true)
//                        surfaceMediaPlayer.setZOrderMediaOverlay(true)

                        surfaceMediaPlayer.visibility=View.VISIBLE





                    }
                    microscopeOn=!microscopeOn
                    ibMicroscope.setImageResource(res)
//                    rotateButton.x=layout.width.toFloat()/2-rotateButton.width.toFloat()/2
//                    rotateButton.y=layout.height.toFloat()/2-rotateButton.height.toFloat()/2
                }
            }
            true
        }
        ibHelp.setOnTouchListener{_: View, m: MotionEvent ->
            when (m.action) {
                MotionEvent.ACTION_DOWN ->{ibHelp.setImageResource(R.drawable.help_down)}
                MotionEvent.ACTION_UP ->  {
                    ibHelp.setImageResource(R.drawable.help_on)
                    ivBackground.setImageResource(R.drawable.help_notice)
                    DecorateTransition.alphaShow(ivBackground,800)
                    ivBackground.setOnClickListener {
                        DecorateTransition.alphaHide(ivBackground,800)
                        ibHelp.setImageResource(R.drawable.help_off)
                        ivBackground.setOnClickListener(null)
                    }
                }
            }
            true
        }
        ibPrivacy.setOnTouchListener{_: View, m: MotionEvent ->
            when (m.action) {
                MotionEvent.ACTION_DOWN ->{ibPrivacy.setImageResource(R.drawable.privacy_down)}
                MotionEvent.ACTION_UP ->  {
                    ibPrivacy.setImageResource(R.drawable.privacy_on)
                    ivBackground.setImageResource(R.drawable.privacy_notice)
                    DecorateTransition.alphaShow(ivBackground,800)
                    ivBackground.setOnClickListener {
                        DecorateTransition.alphaHide(ivBackground,800)
                        ibPrivacy.setImageResource(R.drawable.privacy_off)
                        ivBackground.setOnClickListener(null)
                    }
                }
            }
            true
        }
        ibReference.setOnTouchListener{_: View, m: MotionEvent ->
            when (m.action) {
                MotionEvent.ACTION_DOWN ->{ibReference.setImageResource(R.drawable.references_down)}
                MotionEvent.ACTION_UP ->  {
                    ibReference.setImageResource(R.drawable.references_on)
                    ivBackground.setImageResource(R.drawable.reference_notice)
                    DecorateTransition.alphaShow(ivBackground,800)
                    ivBackground.setOnClickListener {
                        DecorateTransition.alphaHide(ivBackground,800)
                        ibReference.setImageResource(R.drawable.references_off)
                        ivBackground.setOnClickListener(null)
                    }
                }
            }
            true
        }
        ibTerms.setOnTouchListener{_: View, m: MotionEvent ->
            when (m.action) {
                MotionEvent.ACTION_DOWN ->{ibTerms.setImageResource(R.drawable.terms_down)}
                MotionEvent.ACTION_UP ->  {
                    ibTerms.setImageResource(R.drawable.terms_on)
                    ivBackground.setImageResource(R.drawable.terms_notice)
                    DecorateTransition.alphaShow(ivBackground,800)
                    ivBackground.setOnClickListener {
                        DecorateTransition.alphaHide(ivBackground,800)
                        ibTerms.setImageResource(R.drawable.terms_off)
                        ivBackground.setOnClickListener(null)
                    }
                }
            }
            true
        }

        textureMediaPlayer.mediaPlayer.setOnCompletionListener {
            current.stage.onVideoComplete()
        }

        //Set progressbar to start
        iv_all_down.viewTreeObserver.addOnGlobalLayoutListener {
            iv_all_down.right=progressBarPosition

        }


        textView.setOnTouchListener{_: View, _: MotionEvent ->
             false
        }
        surfaceMediaPlayer.setOnTouchListener { _: View, m: MotionEvent ->
            when (m.action) {
                MotionEvent.ACTION_DOWN -> {}
                MotionEvent.ACTION_MOVE -> {

                    surfaceMediaPlayer.clipCircle(m.x,m.y,300f)

                }
                MotionEvent.ACTION_UP -> {
//                    textureMediaPlayer.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.a_introlong))

                }
            }
            true
        }
        //End of listeners
    }

    fun changePlayButtonState(){
        val res=
            if (Math.abs(progressBar.startSectionId - progressBar.endSectionId) == 6) {//All
                when (progressBar.isUpdating) {
                    true  ->  R.drawable.playallpause
                    false ->  R.drawable.playall
                }
            } else {
                when (progressBar.isUpdating) {                                         //not All
                    true  ->  R.drawable.playpause
                    false ->  R.drawable.play
                }
            }

        ibPlay.setImageResource(res)
        ibPlay.tag=res
    }

    inner class Progressbar{
        var startSectionId=0
        var endSectionId=0
        var currentSectionId:Int=0
        var currentLeft=-1
        var currentRight=-1
        var currentStart_mS=-1
        var currentEnd_mS=-1

        private var task: TimerTask?=null
        private var timer: Timer?=null
        private var isSectionFinished=false



        var isUpdating=false
        private val handlerUI= Handler(Looper.getMainLooper())

        inner class ProgressBarSection(val t1:Int, val t2:Int, val segmentLeft:Int, val segmentRight:Int)
        val originWidth=797
        private val originHeight=75
        private val segmentTop=0
        private val segmentBottom=75

        val sections= arrayOf(
            ProgressBarSection(0    ,   3967    ,	5   ,	118     ) ,  //0
            ProgressBarSection(3968 ,   11600   ,	121 ,	229     ) ,  //1
            ProgressBarSection(11601,   19233   ,	232 ,	340     ) ,  //2
            ProgressBarSection(19234,   26867   ,	343 ,	451     ) ,  //3
            ProgressBarSection(26868,   34500   ,	454 ,	562     ) ,  //4
            ProgressBarSection(34501,   42133   ,	565 ,	677     ) ,  //5
            ProgressBarSection(42134,   49696   ,	680 ,	788     )    //6           49767 to  49696 ?
        )

        fun findCurrentSection(videoPosition:Int):Int {
            sections.forEachIndexed { index, progressBarSection ->
                if (videoPosition in progressBarSection.t1..progressBarSection.t2
//                    &&  currentSectionId != index
                ){
                    return index
//                            Toast.makeText(applicationContext, "Section: changed $currentSectionId\nleft: $currentLeft right: $currentRight\nStart: $currentStart_mS  end: $currentEnd_mS", Toast.LENGTH_SHORT).show()
                }
            }
            return -1
        }
        fun assignCurrentSection(sectionID:Int){
            currentSectionId = sectionID
            currentLeft = originToLocalScreenX(iv_all_down,sections[sectionID].segmentLeft,originWidth)
            currentRight = originToLocalScreenX(iv_all_down,sections[sectionID].segmentRight,originWidth)
            currentStart_mS = sections[sectionID].t1
            currentEnd_mS = sections[sectionID].t2
        }

        fun idByCoordinates(x:Float):Int?{
            val localX = globalScreenToOriginX(iv_all_down,x.toInt(),originWidth)
            sections.forEachIndexed { index, progressBarSection ->
                if (localX in progressBarSection.segmentLeft..progressBarSection.segmentRight){
                    return index
//                    Toast.makeText(applicationContext, "Clicked section: $index", Toast.LENGTH_SHORT).show()
                }
            }
            return null
        }
        fun playFromTappedSectionStart(x:Float){

            val section:Int?=idByCoordinates(x)
            if (section!! in 1..6){
                pause()

                endSectionId=section
                if(section<=currentSectionId){
                    currentSectionId=section
                    currentPlayPosition=sections[section].t1
                    assignCurrentSection(section)
                }

                prepareVideo()
//                textureMediaPlayer.setOnPreparedListener {
//                    it.setOnSeekCompleteListener {
//                        start(section)
//                        it.setOnSeekCompleteListener(null)
//                    }
//                }
                start(section)
                iv_all_down.right= calculateProgressBarPosition(currentPlayPosition)



            }
            else if(section==0)
                playFromTappedSectionEnd(x)

//            addMessage("Tapped Section Start $section cur: ${textureMediaPlayer.mediaPlayer!!.currentPosition} $currentPlayPosition"  )
        }
        fun playFromTappedSectionEnd(x:Float){
            val section:Int?=idByCoordinates(x)

            if (section!! in 0..6){
                pause()

                endSectionId=section
                currentSectionId=section
                currentPlayPosition=sections[section].t2
                assignCurrentSection(section)

                prepareVideo()
//                textureMediaPlayer.setOnPreparedListener {
//                    it.setOnSeekCompleteListener {
//                        start(section)
//                        it.setOnSeekCompleteListener(null)
//                    }
//                }
                start(section)

                iv_all_down.right= calculateProgressBarPosition(currentPlayPosition)
//                addMessage("playFromTappedSectionStart end sect$")
//                start(section)

            }
//            addMessage("Tapped Section End $section cur: ${textureMediaPlayer.mediaPlayer!!.currentPosition} $currentPlayPosition"  )

        }


        fun moveTo(x:Float){

            if(x>=originToLocalScreenX(iv_all_down,sections[0].segmentLeft,originWidth)
                && x<frameLayout.width) {
                if (rotateButton.visibility==View.VISIBLE) rotateButton.visibility=View.GONE

                    pause()

                    //Adaptation for offset between current progressbar position and touched
                    var adoptedX = progressBarPositionBeforeDown - progressBarDownX + x
                    val leftLimit= originToLocalScreenX(iv_all_down,sections[0].segmentRight,originWidth)
                    if(adoptedX<leftLimit)adoptedX=leftLimit.toFloat()
                    val rightLimit=originToLocalScreenX(iv_all_down,sections[6].segmentRight,originWidth)
                    if(adoptedX>rightLimit)adoptedX=rightLimit.toFloat()

                    currentPlayPosition = calculateVideoPosition(adoptedX) //calculateVideoPosition(x)
                    val curSection=findCurrentSection(currentPlayPosition)
                    if(curSection!=-1){
                        currentSectionId=curSection
                        assignCurrentSection(currentSectionId)
                        endSectionId=currentSectionId
    //                    addMessage("____Assign section: $curSection video position $currentPlayPosition")
                }

                prepareVideo()

                progressBarPosition = adoptedX.toInt() //X not correct because segments width is not equal
                iv_all_down.right = progressBarPosition

//                addMessage("____End section: $endSectionId video position $currentPlayPosition")

            }
        }
        private fun prepareVideo(){
            var mUri: Uri? = null
            try {
                val mUriField = textureMediaPlayer::class.java.getDeclaredField("mUri")
                mUriField.isAccessible = true
                mUri = mUriField.get(textureMediaPlayer) as Uri
            } catch (e: Exception) {
//                addMessage("___Exception during getting textureMediaPlayer Uri")
            }
            val mainVideoUri=Uri.parse("android.resource://" + packageName + "/" + R.raw.main)
            if(mUri?.compareTo(mainVideoUri)!=0){

                textureMediaPlayer.mediaPlayer.isLooping = false
                textureMediaPlayer.setVideoURI(mainVideoUri)
                if(textureMediaPlayer.mediaPlayer.currentPosition<sections[0].t2){
                    textureMediaPlayer.seekTo(sections[0].t2)
                    startSectionId=0
                }

                textureMediaPlayer.start()
                textureMediaPlayer.pause()


//                addMessage("____Video changed: to \n$mainVideoUri from \n$mUri\n video current position: ${textureMediaPlayer.mediaPlayer!!.currentPosition} - $currentPlayPosition")
//                Toast.makeText(applicationContext, "Stage: ${current.stage.javaClass.simpleName}\nPrevious: ${current.previousStage.javaClass.simpleName}", Toast.LENGTH_SHORT).show()
            }
            else{

                textureMediaPlayer.seekTo(currentPlayPosition)
                textureMediaPlayer.start()
                textureMediaPlayer.pause()

            }

        }
        fun start(endSectionId:Int){
            this.endSectionId=endSectionId
            startSectionId=currentSectionId
            start()
        }
        fun start(){
            if(!isUpdating){
                isUpdating=true

                if(rotateButton.visibility==View.VISIBLE)rotateButton.visibility=View.GONE

                //Check if textureMediaPlayer source already set
                prepareVideo()

                timer=Timer()
                task=object : TimerTask(){
                    override fun run() {
                        handlerUI.post {  updatePB()  }
                    }

                }

                timer?.scheduleAtFixedRate(task, 0, BACKWARD_UPDATE_INTERVAL)
            }
            else{
                //Attempt to start timer when it already runs
            }

            //Post with delay
            val handler = Handler()
            handler.postDelayed({
                changePlayButtonState()
                if (!textureMediaPlayer.mediaPlayer.isPlaying) textureMediaPlayer.start()
            }, BACKWARD_UPDATE_INTERVAL)


        }
        fun pause() {

            if(isUpdating){


                isUpdating=false
                task?.cancel()

                timer?.purge()
                timer?.cancel()

                timer=null
                task=null

                if(isSectionFinished){
                    val handler = Handler()
                    handler.postDelayed({
                        current.stage.onSectionPlayComplete()
                    }, BACKWARD_UPDATE_INTERVAL)
                }
                isSectionFinished=false

            }
            else{
                //Attempt to cancel not running timer
            }

//            //Post with delay
//            val handler = Handler()
//            handler.postDelayed({
//                if(textureMediaPlayer.mediaPlayer?.isPlaying!!) textureMediaPlayer.pause()
//                changePlayButtonState()
//            }, BACKWARD_UPDATE_INTERVAL)

            textureMediaPlayer.pause()
        }
        private fun updatePB() {
            currentPlayPosition=textureMediaPlayer.mediaPlayer.currentPosition

            val curSection=findCurrentSection(currentPlayPosition)
            if(curSection!=-1)assignCurrentSection(curSection)

            if(currentSectionId<=endSectionId){                  //Forward

                if(textureMediaPlayer.mediaPlayer.currentPosition>=sections[endSectionId].t2-FORWARD_LATENCY_INTERVAL.toInt()){        //finished

                    textureMediaPlayer.pause()
                    textureMediaPlayer.seekTo(sections[endSectionId].t2)
                    isSectionFinished=true
                    pause()

                }
                else{                                                //If not finished
                    if (!textureMediaPlayer.mediaPlayer.isPlaying)  textureMediaPlayer.start()
                }
            }
            else{                                                //Backward

//                if(currentPlayPosition<=sections[endSectionId].t2){        //finished (with latency)
//                    textureMediaPlayer.seekTo(sections[endSectionId].t2)
//                    isSectionFinished=true
//                    pause()
//                }
//                else{
//                    if (textureMediaPlayer.mediaPlayer!!.isPlaying)  textureMediaPlayer.pause()                                           //If not finished
//                    textureMediaPlayer.seekTo(currentPlayPosition - BACKWARD_UPDATE_INTERVAL.toInt())
//                }
            }

            progressBarPosition=calculateProgressBarPosition(currentPlayPosition)

            inc++
        }
        private fun calculateProgressBarPosition(videoPosition:Int):Int{
            val t_min = Math.min(currentStart_mS,currentEnd_mS)
            val t_max = Math.max(currentStart_mS,currentEnd_mS)
            val duration=t_max - t_min
            val width = Math.abs(currentRight-currentLeft)

            val pbLeft = Math.min(currentLeft,currentRight)
            val pbRight = Math.max(currentLeft,currentRight)

            val pbW = pbRight-pbLeft
            val positionPB = pbLeft +(videoPosition-t_min)*pbW/duration

            iv_all_down.right=positionPB
//            progressBarPosition=positionPB

            return positionPB
        }
        private fun calculateVideoPosition(positionPB:Float):Int{
            val t_min = Math.min(currentStart_mS,currentEnd_mS)
            val t_max = Math.max(currentStart_mS,currentEnd_mS)
            val duration=t_max - t_min
//            val width = Math.abs(currentRight-currentLeft)

            val pbLeft = Math.min(currentLeft,currentRight)
            val pbRight = Math.max(currentLeft,currentRight)

            val pbW = pbRight-pbLeft

            val videoPosition = (positionPB - pbLeft)*duration/pbW + t_min

            return videoPosition.toInt()
        }
    }

    private lateinit var mDetector: GestureDetectorCompat

    private val ivBackground by lazy {findViewById<ImageView>(R.id.ivBackgorund)}
//    private val iv_all_off by lazy {findViewById<ImageView>(R.id.iv_all_off)}
    private val iv_all_down by lazy {findViewById<ImageView>(R.id.iv_all_down)}
//    private val iv_all_empty by lazy {findViewById<ImageView>(R.id.iv_all_empty)}


    private val ibPlay by lazy {findViewById<ImageButton>(R.id.ibPlay)}
    private val ibMicroscope by lazy {findViewById<ImageButton>(R.id.ibMicroscope)}
    private val ibHelp by lazy {findViewById<ImageButton>(R.id.ibHelp)}
    private val ibReference by lazy {findViewById<ImageButton>(R.id.ibReference)}
    private val ibPrivacy by lazy {findViewById<ImageButton>(R.id.ibPrivacy)}
    private val ibTerms by lazy {findViewById<ImageButton>(R.id.ibTerms)}
    private val groupPlayer by lazy {findViewById<Group>(R.id.groupPlayer)}
    private val layout by lazy {findViewById<ConstraintLayout>(R.id.layout)}
    private val rotateButton by lazy {findViewById<ImageView>(R.id.rotateButton)}
    private val frameLayout by lazy {findViewById<FrameLayout>(R.id.frameLayout)}

    val surfaceMediaPlayer by lazy { findViewById<SurfaceMediaPlayer>(R.id.surfaceMediaPlayer) }
    val textureMediaPlayer by lazy { findViewById<TextureMediaPlayer>(R.id.textureMediaPlayer) }

//    private val textureMediaPlayer by lazy {findViewById<textureMediaPlayer>(R.id.textureMediaPlayer)}
    private var microscopeOn=true

    private fun addMessage(msg: String) {
        textView.append("$inc_msg. $msg\n")
        inc_msg++

        if(textView.layout!=null){
            val scrollAmount = textView.layout.getLineTop(textView.lineCount) - textView.height
            if (scrollAmount > 0)
                textView.scrollTo(0, scrollAmount)
            else
                textView.scrollTo(0, 0)
        }
    }
    private enum class SubState{Play,Loop,Rotate}

}
