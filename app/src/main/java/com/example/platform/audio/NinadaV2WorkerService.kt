package com.example.platform.audio
import android.app.Service
import android.content.Intent
import android.os.*
import kotlinx.coroutines.*
class NinadaV2WorkerService : Service() {
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
 private val engine by lazy { NinadaNeuralVoiceEngine.getInstance(applicationContext) }
 private val messenger by lazy { Messenger(Handler(Looper.getMainLooper()){ msg-> handle(msg); true }) }
 override fun onBind(intent:Intent?):IBinder=messenger.binder
 private fun handle(msg:Message){
  val reply=msg.replyTo?:return; val d=msg.data?:Bundle.EMPTY; val id=d.getLong(ID)
  if(msg.what!=SYNTH){send(reply,id,"unsupported");return}
  val t=d.getString(TEXT).orEmpty(); val lang=d.getString(LANG).orEmpty(); val gender=d.getString(GENDER).orEmpty(); val speed=d.getFloat(SPEED,1f)
  if(t.isBlank()||t.length>4000||lang !in LANGS||gender !in setOf("MALE","FEMALE")||speed !in .5f..2f){send(reply,id,"invalid");return}
  scope.launch {
   val result=try{engine.synthesizePcm24k(t,lang,gender,speed)}catch(_:Throwable){null}
   val m=Message.obtain(null,RESULT); m.data=Bundle().apply{putLong(ID,id); if(result!=null&&result.pcm16.size<=1200000){putString(STATUS,"ok");putInt(RATE,result.sampleRate);putShortArray(PCM,result.pcm16);putString(LABEL,result.engineLabel)}else putString(STATUS,"fail")}
   try{reply.send(m)}catch(_:RemoteException){}
  }
 }
 private fun send(reply:Messenger,id:Long,status:String){val m=Message.obtain(null,RESULT);m.data=Bundle().apply{putLong(ID,id);putString(STATUS,status)};try{reply.send(m)}catch(_:RemoteException){}}
 override fun onDestroy(){engine.release();scope.cancel();super.onDestroy()}
 companion object{const val SYNTH=100;const val RESULT=101;const val ID="id";const val TEXT="text";const val LANG="lang";const val GENDER="gender";const val SPEED="speed";const val STATUS="status";const val RATE="rate";const val PCM="pcm";const val LABEL="label";val LANGS=setOf("sa","hi","mr","gu","ta","te","kn","bn","en")}
}