package com.example.huellaapp

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResponsiveScreensTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun checkSafeArea(activity: Activity) {
        val root = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        val insets = ViewCompat.getRootWindowInsets(root)!!
        val safe = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        assertTrue("Top: " + activity.localClassName, root.paddingTop >= safe.top)
        assertTrue("Bottom: " + activity.localClassName, root.paddingBottom >= maxOf(safe.bottom, ime.bottom))
        assertTrue(root.width > root.paddingLeft + root.paddingRight)
        assertTrue(root.height > root.paddingTop + root.paddingBottom)
    }

    @Test fun allScreensRespectSystemBarsAndRotation() {
        val screens = listOf(LoginActivity::class.java, RegistroActivity::class.java,
            RecuperarAccesoActivity::class.java, MainActivity::class.java,
            CrearMascotaActivity::class.java, ListarMascotasActivity::class.java,
            AsistenteIAActivity::class.java, DirectorioVetActivity::class.java,
            MiPerfilActivity::class.java, EditarPerfilActivity::class.java, DetalleMascotaActivity::class.java)
        for (screen in screens) {
            val intent = Intent(instrumentation.targetContext, screen)
            ActivityScenario.launch<Activity>(intent).use { scenario ->
                instrumentation.waitForIdleSync()
                scenario.onActivity { checkSafeArea(it); it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
                instrumentation.waitForIdleSync()
                scenario.onActivity { checkSafeArea(it); it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
                instrumentation.waitForIdleSync()
            }
        }
    }

    @Test fun chatInputRemainsAboveKeyboard() {
        ActivityScenario.launch(AsistenteIAActivity::class.java).use { scenario ->
            scenario.onActivity {
                val input = it.findViewById<View>(R.id.etChatInput)
                input.requestFocus()
                (it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }
            // Wait for the IME animation, not a network response.
            android.os.SystemClock.sleep(800)
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                checkSafeArea(it)
                val input = it.findViewById<View>(R.id.bottomInputBar)
                val root = it.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
                assertTrue(input.bottom <= root.height - root.paddingBottom)
                val insets = ViewCompat.getRootWindowInsets(root)!!
                assertTrue("Keyboard was opened", insets.isVisible(WindowInsetsCompat.Type.ime()))
            }
        }
    }

    @Test fun draftSurvivesRecreationAndScrollsAboveKeyboardInLandscape() {
        ActivityScenario.launch(AsistenteIAActivity::class.java).use { scenario ->
            scenario.onActivity {
                val field = AsistenteIAActivity::class.java.getDeclaredField("pistaBorradorActual").apply { isAccessible = true }
                field.set(it, com.example.huellaapp.model.PistaBorrador("Collar azul", "Parque Caldas"))
                AsistenteIAActivity::class.java.getDeclaredMethod("mostrarBorrador").apply { isAccessible = true }.invoke(it)
            }
            scenario.recreate()
            scenario.onActivity {
                assertTrue(it.findViewById<android.widget.TextView>(R.id.tvDraftDetails).text.contains("Parque Caldas"))
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                val input = it.findViewById<View>(R.id.etChatInput)
                input.requestFocus()
                (it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }
            android.os.SystemClock.sleep(800)
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                checkSafeArea(it)
                assertTrue(it.findViewById<View>(R.id.scrollChat).height > 0)
                assertTrue(it.findViewById<View>(R.id.btnPublicarPista).isEnabled)
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }
}
