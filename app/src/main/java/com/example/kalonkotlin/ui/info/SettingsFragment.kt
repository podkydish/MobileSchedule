package com.example.kalonkotlin.ui.info

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.kalonkotlin.R
import com.example.kalonkotlin.client.APP_PREFERENCES
import com.example.kalonkotlin.databinding.FragmentSettingsBinding


@SuppressLint("UseSwitchCompatOrMaterialCode")
class SettingsFragment : Fragment(), View.OnClickListener {

    private lateinit var authorText: TextView
    private lateinit var authorButton: Button
    lateinit var theme: Switch
    lateinit var layout: ConstraintLayout
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sPref: SharedPreferences


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

    @SuppressLint("SetTextI18n", "PrivateResource", "ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setHasOptionsMenu(true)
        theme = binding.theme
        layout = binding.settingsLayout
        authorText = binding.authorText
        authorButton = binding.authorBtn
        authorButton.setOnClickListener(this)



        theme.isChecked = AppCompatDelegate.getDefaultNightMode() != MODE_NIGHT_NO



        theme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                savePref(getString(R.string.dark))
            } else {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                savePref(getString(R.string.light))
            }
        }
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

    private fun savePref(prefValue: String?) {
        sPref = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        val ed = sPref.edit()
        ed.putString(getString(R.string.theme), prefValue)
        ed.apply()
    }
}