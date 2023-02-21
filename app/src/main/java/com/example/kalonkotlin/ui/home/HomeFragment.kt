package com.example.kalonkotlin.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kalonkotlin.client.Network
import com.example.kalonkotlin.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var maiNews: WebView? = null
    private var _binding: FragmentHomeBinding? = null


    private val binding get() = _binding!!

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        maiNews?.settings?.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        maiNews = binding.webview
        maiNews!!.settings.javaScriptEnabled = true
        maiNews!!.loadUrl(("javascript:(function() { " +
                "document.getElementsByTagName('col-lg-8 me-auto mb-7 mb-lg-0')[0].style.display=\"none\"; " +
                "})()"))
        maiNews?.loadUrl("https://mai.ru/press/news/")
        val textView: TextView = binding.textHome
        textView.text = "Добро пожаловать!"
        if (!Network.checkConnectivity(this.requireContext())) {
            Toast.makeText(this.requireContext(), "Проверьте подключение к интернету", Toast.LENGTH_LONG).show()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}