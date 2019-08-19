package com.currency.converter.demo.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.currency.converter.demo.R
import com.currency.converter.demo.api.Resource
import com.currency.converter.demo.api.Status
import com.currency.converter.demo.models.CurrencyRate
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.content_main.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasAndroidInjector {
    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    val countriesMap: MutableMap<String, String> = mutableMapOf()

    var currencyRates: MutableList<CurrencyRate> = mutableListOf()

    var fromSelectedIndex = -1

    var toSelectedIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val spannableString = SpannableString(titleTextView.text)
        spannableString.setSpan(
            ForegroundColorSpan(
                ResourcesCompat.getColor(resources, R.color.colorAccent, theme)
            )
            , titleTextView.length() - 1, titleTextView.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        titleTextView.text = spannableString
        Locale.getISOCountries().forEach {
            try {
                countriesMap[Currency.getInstance(Locale("", it)).currencyCode] = it
            } catch (e: Exception) {
                when (e) {
                    is IllegalStateException, is NullPointerException -> e.printStackTrace()
                    else -> throw e
                }
            }

//            Log.d("Assigning", "${Locale("en", it).isO3Country} -> $it")
//            Log.d("Reading", "${countriesMap[Locale("en", it).isO3Country]}")
        }
        //Log.d("Map is set", countriesMap.toString())
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        val adapter = CustomSpinnerAdapter(this, emptyList())
        viewModel.ratesLiveData.observe(this,
            Observer<Resource<List<CurrencyRate>>> {
                when (it.status) {
                    Status.SUCCESS -> {
                        progressBarLayout.visibility = GONE
                        if (!it.data.isNullOrEmpty()) {
                            currencyRates = it.data.toMutableList()
                            adapter.updateList(currencyRates)
                            enableAllButtons()
                        }
                    }
                    Status.ERROR -> {
                        progressBarLayout.visibility = GONE
                        if (adapter.count == 0) disableAllButtons()
                        Snackbar.make(progressBar, it.message ?: "Failed to fetch rates", Snackbar.LENGTH_LONG)
                            .setAction("RETRY") {
                                viewModel.getRates()
                            }.show()
                    }
                    Status.LOADING -> {
                        disableAllButtons()
                        progressBarLayout.visibility =
                            if (it.data?.isNullOrEmpty() == true) VISIBLE else GONE //blur background on cold launch
                        if (!it.data.isNullOrEmpty()) {
                            currencyRates = it.data.toMutableList()
                            adapter.updateList(currencyRates)
                            enableAllButtons()
                        }
                    }
                }
            })
        fromSpinner.adapter = adapter
        toSpinner.adapter = adapter
        viewModel.getRates()
        setListeners()
    }

    private fun setListeners() {
        fromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val currencyRate = (parent?.getItemAtPosition(position) as CurrencyRate)
                fromSelectedIndex = position
                fromCurrencyLabel.text = currencyRate.currencyCode
                if (fromEditText.text.toString().isEmpty()) {
                    return
                }
                convertValue()
            }

        }
        toSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val currencyRate = (parent?.getItemAtPosition(position) as CurrencyRate)
                toSelectedIndex = position
                toCurrencyLabel.text = currencyRate.currencyCode
                if (fromEditText.text.toString().isEmpty()) {
                    return
                }
                convertValue()
            }

        }
        fromEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    toTextView.text = ""
                    return
                }
                if (s.toString() == formatValue(s.toString())) {
                    convertValue()
                    fromEditText.setSelection(fromEditText.length())
                    return
                }
                fromEditText.setText(formatValue(s.toString()))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
//        fromEditText.setOnKeyListener { v, keyCode, event ->
//            return when (keyCode){
//                KeyEvent.KEYCODE_PERIOD -> {
//                    fromEditText.text.toString().indexOfFirst { it == '.'} == fromEditText.length() - 1
//                }
//                else -> true
//            }
//            event.characters != "."
//        }
    }

    fun convertValue() {
        val fromCurrencyRate = currencyRates[fromSelectedIndex]
        val toCurrencyRate = currencyRates[toSelectedIndex]
        if (fromCurrencyRate.baseCurrencyCode == toCurrencyRate.baseCurrencyCode) {
            val convertedValue =
                fromEditText.text.toString().replace(",", "").toDouble() * toCurrencyRate.rate / fromCurrencyRate.rate
            toTextView.text = formatValue(convertedValue.toString())
        }
    }

    private fun formatValue(value: String): String {
        var resValue = value
        if (value[value.length - 1] == '.' && value.indexOfFirst { it == '.' } != value.length - 1) {
            resValue = value.dropLast(1)
        }
        val res = NumberFormat.getNumberInstance(Locale.getDefault())
            .format(BigDecimal.valueOf(resValue.replace(",", "").toDouble()).setScale(2, BigDecimal.ROUND_HALF_UP))
        return if (resValue[resValue.length - 1] == '.') "$res." else res
    }

    private fun disableAllButtons() {
        convertButton.isEnabled = false
        fromSpinner.isEnabled = false
        toSpinner.isEnabled = false
        signUp.isEnabled = false
    }

    private fun enableAllButtons() {
        convertButton.isEnabled = true
        fromSpinner.isEnabled = true
        toSpinner.isEnabled = true
        signUp.isEnabled = true
    }

    inner class CustomSpinnerAdapter(val context: Context, var countries: List<CurrencyRate>) : BaseAdapter(),
        SpinnerAdapter {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val pictureUrlFormat =
                "https://www.countryflags.io/${countriesMap[countries[position].currencyCode]}/flat/48.png"
            if (convertView == null) {
                val view = LayoutInflater.from(context).inflate(R.layout.view_spinner_item, parent, false)
                view.findViewById<TextView>(R.id.name).text = countries[position].currencyCode
                intoImageView(context, pictureUrlFormat, view.findViewById(R.id.flag))
                return view
            }
            convertView.findViewById<TextView>(R.id.name).text = countries[position].currencyCode
            intoImageView(context, pictureUrlFormat, convertView.findViewById(R.id.flag))
            return convertView
        }

        override fun getItem(position: Int): Any = countries[position]


        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int = countries.size

        private fun intoImageView(context: Context, url: String, view: ImageView) {
            Glide.with(context)
                .load(url)
                .apply(RequestOptions.circleCropTransform())
                .into(view)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val pictureUrlFormat =
                "https://www.countryflags.io/${countriesMap[countries[position].currencyCode]}/flat/48.png"
            if (convertView == null) {
                val view = LayoutInflater.from(context).inflate(R.layout.view_spinner_dropdown_item, parent, false)
                view.findViewById<TextView>(R.id.name).text = countries[position].currencyCode
                intoImageView(context, pictureUrlFormat, view.findViewById(R.id.flag))
                return view
            }
            convertView.findViewById<TextView>(R.id.name).text = countries[position].currencyCode
            intoImageView(context, pictureUrlFormat, convertView.findViewById(R.id.flag))
            return convertView
        }

        fun updateList(newList: List<CurrencyRate>) {
            countries = newList
            notifyDataSetChanged()
        }
    }
}
