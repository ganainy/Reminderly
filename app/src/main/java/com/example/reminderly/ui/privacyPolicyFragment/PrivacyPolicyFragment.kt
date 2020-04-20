package com.example.reminderly.ui.privacyPolicyFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.reminderly.R
import kotlinx.android.synthetic.main.privacy_policy_fragment.*

class PrivacyPolicyFragment : Fragment() {

    companion object {
        fun newInstance() = PrivacyPolicyFragment()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.privacy_policy_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        webView.loadUrl("https://sites.google.com/view/reminderly/%D8%A7%D9%84%D8%B5%D9%81%D8%AD%D8%A9-%D8%A7%D9%84%D8%B1%D8%A6%D9%8A%D8%B3%D9%8A%D8%A9")
        webView.settings.apply {
            setLoadWithOverviewMode(true)
            setUseWideViewPort(true)
            setAllowFileAccess(true)
            setAllowContentAccess(true)
            setAllowFileAccessFromFileURLs(true)
            setAllowUniversalAccessFromFileURLs(true)
            setDomStorageEnabled(true)
            javaScriptEnabled = true
        }


    }



}
