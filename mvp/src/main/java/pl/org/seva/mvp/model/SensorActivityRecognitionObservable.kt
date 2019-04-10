/*
 * Copyright (C) 2019 Wiktor Nizio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you like this program, consider donating bitcoin: bc1qncxh5xs6erq6w4qz3a7xl7f50agrgn3w58dsfp
 */

package pl.org.seva.mvp.model

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import pl.org.seva.mvp.R

class SensorActivityRecognitionObservable(private val ctx: Context) : ActivityRecognitionObservable {
    private val subject = PublishSubject.create<ActivityDesc>()

    override val observable: Observable<ActivityDesc> = subject

    init {
        var activityRecognitionReceiver : BroadcastReceiver? = null

        fun registerReceiver() {
            activityRecognitionReceiver = activityRecognitionReceiver?: Receiver()
            ctx.registerReceiver(activityRecognitionReceiver, IntentFilter(ACTIVITY_RECOGNITION_INTENT))
        }

        fun unregisterReceiver() {
            activityRecognitionReceiver?: return
            ctx.unregisterReceiver(activityRecognitionReceiver)
        }

        val googleApiClient = GoogleApiClient.Builder(ctx)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks{
                    override fun onConnected(p0: Bundle?) {
                        registerReceiver()
                        val intent = Intent(ACTIVITY_RECOGNITION_INTENT)
                        val pi = PendingIntent.getBroadcast(
                                ctx,
                                0,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT)
                        ActivityRecognition.getClient(ctx).requestActivityUpdates(
                                ACTIVITY_RECOGNITION_INTERVAL_MS,
                                pi)
                    }

                    override fun onConnectionSuspended(p0: Int) = unregisterReceiver()
                })
                .build()
        googleApiClient.connect()
    }

    private inner class Receiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                val result = ActivityRecognitionResult.extractResult(intent)!!
                val activity = result.mostProbableActivity!!
                val desc = context.getString(when (activity.type) {
                    DetectedActivity.IN_VEHICLE -> R.string.activity_desc_in_vehicle
                    DetectedActivity.ON_BICYCLE -> R.string.activity_desc_on_bicycle
                    DetectedActivity.ON_FOOT -> R.string.activity_desc_on_foot
                    DetectedActivity.TILTING -> R.string.activity_desc_tilting
                    DetectedActivity.WALKING -> R.string.activity_desc_walking
                    DetectedActivity.RUNNING -> R.string.activity_desc_running
                    else -> R.string.activity_desc_unknown
                })
                val confidence = activity.confidence
                val activityDesc = ActivityDesc(desc, confidence)
                subject.onNext(activityDesc)
            }
        }
    }

    companion object {
        private const val ACTIVITY_RECOGNITION_INTENT = "activity_recognition_intent"
        private const val ACTIVITY_RECOGNITION_INTERVAL_MS = 1000L
    }
}
