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

package pl.org.seva.mvp

import androidx.test.espresso.Espresso.onView
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule
import pl.org.seva.mvp.action.DelayAction
import pl.org.seva.mvp.view.MainActivity
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import pl.org.seva.mvp.mock.MockActivityRecognitionObservable

@RunWith(AndroidJUnit4::class)
class ActivityRecognitionTest {

    @Suppress("unused", "BooleanLiteralArgument")
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, true, true)

    private fun delay(millis: Long) = onView(isRoot()).perform(DelayAction.delay(millis))

    @Test
    fun testActivityRecognition() {
        delay(INITIAL_DELAY)
        onView(withId(R.id.activity_desc)).check(matches(withText(MockActivityRecognitionObservable.DESC1)))
        onView(withId(R.id.activity_conf)).check(matches(withText(MockActivityRecognitionObservable.CONF1.toString())))
        delay(INTERVAL)
        onView(withId(R.id.activity_desc)).check(matches(withText(MockActivityRecognitionObservable.DESC2)))
        onView(withId(R.id.activity_conf)).check(matches(withText(MockActivityRecognitionObservable.CONF2.toString())))
    }

    companion object {
        private const val INITIAL_DELAY = 1500L
        private const val INTERVAL = 1000L
    }
}
