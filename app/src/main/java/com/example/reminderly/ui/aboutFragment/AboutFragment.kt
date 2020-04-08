package com.example.reminderly.ui.aboutFragment

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.example.reminderly.R
import com.example.reminderly.Utils.MyUtils
import kotlinx.android.synthetic.main.about_fragment.*

class AboutFragment : Fragment() {

    companion object {
        fun newInstance() = AboutFragment()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.about_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        messageCard.setOnClickListener { whatsApp() }

    }

    fun whatsApp() {
        val isWhatsappInstalled = whatsappInstalledOrNot("com.whatsapp")
        if (isWhatsappInstalled) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://api.whatsapp.com/send?phone=+2001094095356&text=Hey")
                )
            )
        } else {
            MyUtils.showCustomToast(requireContext(),R.string.dont_have_whatsapp)
        }
    }

    private fun whatsappInstalledOrNot(uri: String): Boolean {
        val pm: PackageManager? = activity?.packageManager
        var app_installed = false
        app_installed = try {
            pm?.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return app_installed
    }

}
