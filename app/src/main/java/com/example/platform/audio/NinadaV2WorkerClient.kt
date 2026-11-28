package com.example.platform.audio
import android.content.*
import android.os.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
class NinadaV2WorkerClient(private val context:Context){
 suspend fun synthesize(text:String,language:String,gender:String,speed:Float):NinadaSynthesisResult?=suspendCancellableCoroutine{c->
  val id=ids.incrementAndGet();var done=false;var bound=false;val h=Handler(Looper.getMainLooper())
  lateinit var conn:ServiceConnection
  fun cleanup(){if(bound)try{context.unbindService(conn)}catch(_:Throwable){}}
  fun finish(v:NinadaSynthesisResult?){if(done)return;done=true;h.removeCallbacks(timeout);cleanup();if(c.isActive)c.resume(v)}
  val timeout=Runnable{finish(null)}
  val reply=Messenger(Handler(Looper.getMainLooper()){msg->
   val d=msg.data;if(msg.what==NinadaV2WorkerService.RESULT&&d.getLong(NinadaV2WorkerService.ID)==id){
    val pcm=d.getShortArray(NinadaV2WorkerService.PCM);val rate=d.getInt(NinadaV2WorkerService.RATE);val label=d.getString(NinadaV2WorkerService.LABEL).orEmpty()
    finish(if(d.getString(NinadaV2WorkerService.STATUS)=="ok"&&pcm!=null&&pcm.isNotEmpty()&&rate==24000)NinadaSynthesisResult(pcm,rate,language,label)else null)
   };true})
  conn=object:ServiceConnection{
   override fun onServiceConnected(n:ComponentName,b:IBinder){val m=Message.obtain(null,NinadaV2WorkerService.SYNTH);m.replyTo=reply;m.data=Bundle().apply{putLong(NinadaV2WorkerService.ID,id);putString(NinadaV2WorkerService.TEXT,text);putString(NinadaV2WorkerService.LANG,language);putString(NinadaV2WorkerService.GENDER,gender);putFloat(NinadaV2WorkerService.SPEED,speed)};try{Messenger(b).send(m)}catch(_:RemoteException){finish(null)}}
   override fun onServiceDisconnected(n:ComponentName){finish(null)}
   override fun onBindingDied(n:ComponentName){finish(null)}
   override fun onNullBinding(n:ComponentName){finish(null)}
  }
  try{bound=context.bindService(Intent(context,NinadaV2WorkerService::class.java),conn,Context.BIND_AUTO_CREATE);if(bound)h.postDelayed(timeout,45000)else finish(null)}catch(_:Throwable){finish(null)}
  c.invokeOnCancellation{h.removeCallbacks(timeout);if(bound)try{context.unbindService(conn)}catch(_:Throwable){}}
 }
 companion object{private val ids=AtomicLong()}
}