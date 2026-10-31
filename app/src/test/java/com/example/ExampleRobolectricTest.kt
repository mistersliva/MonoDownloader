package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AudioSource
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MonoSound", appName)
    }

    @Test
    fun `verify audio source conversion`() {
        assertEquals(AudioSource.YOUTUBE, AudioSource.fromString("yt"))
        assertEquals(AudioSource.SOUNDCLOUD, AudioSource.fromString("sc"))
        assertEquals(AudioSource.SOUNDCLOUD, AudioSource.fromString("soundcloud"))
    }
}
