package com.laurenyew.githubbrowser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.laurenyew.githubbrowser.ui.browser.GithubBrowserFragment
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, GithubBrowserFragment.newInstance())
                .commitNow()
        }
    }
}