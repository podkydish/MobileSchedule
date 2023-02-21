package com.example.kalonkotlin.ui.info

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.kalonkotlin.R
import com.example.kalonkotlin.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment(), View.OnClickListener {

    lateinit var authorText: TextView
    lateinit var authorButton: Button
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!


    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.item1) {
            activity?.onBackPressed()

            return true
        }
        return super.onOptionsItemSelected(item)

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setHasOptionsMenu(true)
        authorText = binding.authorText
        authorButton = binding.authorBtn
        authorButton.setOnClickListener(this)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(p0: View?) {
        when (p0) {
            binding.authorBtn -> {
                authorText.visibility = View.VISIBLE
                authorButton.visibility = View.INVISIBLE
                authorText.movementMethod = LinkMovementMethod.getInstance()
                authorText.text = """
                    Авторы сия творения:
                    Андреев Андрей
                    Кустиков Андрей
                    Муравьёв Иван
                    Поблагодарить авторов и оставить отзыв можно в телеграмме:
                    https://t.me/podkydish
                    https://t.me/bupbipbupbip
                    https://t.me/rufus20145
                    Заценить нашего телеграмм-бота можно тут:
                    https://t.me/maiPomogator_bot
                    """.trimIndent()
            }
        }
    }
}