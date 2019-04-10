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

package pl.org.seva.mvp.presenter

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import pl.org.seva.mvp.model.ActivityDesc
import pl.org.seva.mvp.model.ar

class ActivityPresenter(
        private val desc: (String) -> Unit,
        private val conf: (Int) -> Unit) {

    fun present(act: ActivityDesc) {
        desc(act.desc)
        conf(act.conf)
    }

    @Suppress("unused")
    class Builder(private val owner: LifecycleOwner) {
        lateinit var presentDesc: (String) -> Unit
        lateinit var presentConf: (Int) -> Unit

        fun build() {
            val presenter = ActivityPresenter(presentDesc, presentConf)
            val lifecycle = owner.lifecycle
            var last: ActivityDesc? = null

            val d = ar.observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { act ->
                        println("wiktor new act")
                        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                            presenter.present(act)
                            last = null
                        }
                        else {
                            last = act
                        }
                    }
            lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(value = Lifecycle.Event.ON_RESUME)
                fun presentLast() {
                    last?.let { presenter.present(it) }
                }

                @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
                fun dispose() {
                    d.dispose()
                }
            })
        }
    }

    companion object {
        fun build(owner: LifecycleOwner, block: Builder.() -> Unit) = Builder(owner).apply(block).build()
    }
}
