package com.currency.converter.app.ui

import android.content.Context
import android.graphics.Color
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.currency.converter.app.R
import com.currency.converter.app.util.Utils
import com.currency.converter.core.model.CurrencyRate
import com.currency.converter.core.util.NetworkStatus
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.marker_view.view.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import com.github.mikephil.charting.utils.Utils as ExtraUtils


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    lateinit var adapter: CustomSpinnerAdapter

    val countriesMap = Utils.getCurrencyCodeToCountryCodeMap()

    var currencyRates: MutableList<CurrencyRate> = mutableListOf()

    var fromSelectedIndex = -1

    var toSelectedIndex = -1

    var thirtyDaysHistoricalRates: List<Map<String, List<CurrencyRate>>> = emptyList()
    var ninetyDaysHistoricalRates: List<Map<String, List<CurrencyRate>>> = emptyList()
    var dates = mutableMapOf<Float, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = CustomSpinnerAdapter(this, emptyList())
        observeLiveData()

        fromSpinner.adapter = adapter
        toSpinner.adapter = adapter
        viewModel.getRates()
        setListeners()
    }

    private fun setTitleColorSpan(){
        val spannableString = SpannableString(titleTextView.text)
        spannableString.setSpan(
            ForegroundColorSpan(
                ResourcesCompat.getColor(resources, R.color.colorAccent, theme)
            )
            , titleTextView.length() - 1, titleTextView.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        titleTextView.text = spannableString
    }

    private fun observeLiveData() {
        viewModel.ratesLiveData.observe(
            this,
            {
                when (it) {
                    is NetworkStatus.Success -> {
                        progressBarLayout.visibility = GONE
                        if (!it.data.isNullOrEmpty()) {
                            currencyRates = it.data.toMutableList()
                            adapter.updateList(currencyRates)
                            enableAllButtons()
                        }
                    }
                    is NetworkStatus.Error -> {
                        progressBarLayout.visibility = GONE
                        if (adapter.count == 0) disableAllButtons()
                        Snackbar.make(progressBar, it.message, Snackbar.LENGTH_LONG)
                            .setAction("RETRY") {
                                viewModel.getRates()
                            }.show()
                    }
                    is NetworkStatus.Loading -> {
                        disableAllButtons()
                        progressBarLayout.visibility =
                            if (it.data?.isNullOrEmpty() == true) VISIBLE else GONE //blur background on cold launch
                        if (!it.data.isNullOrEmpty()) {
                            currencyRates = it.data.orEmpty().toMutableList()
                            adapter.updateList(currencyRates)
                            enableAllButtons()
                        }
                    }
                }
            })
        viewModel.historicalRatesLiveData.observe(
            this,
            {
                when (it.numberOfDays) {
                    30 -> {
                        when (it.data) {
                            is NetworkStatus.Success -> {
                                thirtyDaysHistoricalRates = it.data.data
                                if (fromSelectedIndex >= 0 && toSelectedIndex >= 0) {
                                    buildHistoricalRatesGraph(ratesTab.selectedTabPosition)
                                }
                                historicalRatesProgressBar.visibility = GONE
                            }
                            is NetworkStatus.Error -> {
                                Snackbar.make(progressBar, it.data.message, Snackbar.LENGTH_LONG)
                                    .setAction("RETRY") {
                                        viewModel.getHistoricalRates(30)
                                    }.show()
                                historicalRatesProgressBar.visibility = VISIBLE
                            }
                            is NetworkStatus.Loading -> historicalRatesProgressBar.visibility =
                                VISIBLE
                        }
                    }
                    90 -> {
                        when (it.data) {
                            is NetworkStatus.Success -> {
                                ninetyDaysHistoricalRates = it.data.data
                                if (fromSelectedIndex >= 0 && toSelectedIndex >= 0) {
                                    buildHistoricalRatesGraph(ratesTab.selectedTabPosition)
                                }
                                historicalRatesProgressBar.visibility = GONE
                            }
                            is NetworkStatus.Error -> {
                                Snackbar.make(progressBar, it.data.message, Snackbar.LENGTH_LONG)
                                    .setAction("RETRY") {
                                        viewModel.getHistoricalRates(90)
                                    }.show()
                                historicalRatesProgressBar.visibility = VISIBLE
                            }
                            is NetworkStatus.Loading -> historicalRatesProgressBar.visibility =
                                VISIBLE
                        }
                    }
                }
            })
    }

    private fun buildHistoricalRatesGraph(tabPosition: Int){
        var historicalRates = listOf<Map<String, List<CurrencyRate>>>()
        when (tabPosition){
            0 -> {
                historicalRates = thirtyDaysHistoricalRates
            }
            1 -> {
                historicalRates = ninetyDaysHistoricalRates
            }
        }
        setUpLineChart(historicalRates, tabPosition)
    }

    private fun setUpLineChart(
        historicalRates: List<Map<String, List<CurrencyRate>>>,
        tabPosition: Int
    ) {
        if (fromSelectedIndex < 0 || toSelectedIndex < 0) return
        val entries = mutableListOf<Entry>()
        dates.clear()
        if (historicalRates.isNotEmpty()) {
            historicalRates.forEachIndexed { index, entry ->
                val fromCurrencyCode = currencyRates[fromSelectedIndex].currencyCode
                val toCurrencyCode = currencyRates[toSelectedIndex].currencyCode
                val fromCurrencyRate = entry.values.toList()[0].find {
                    it.currencyCode == fromCurrencyCode
                }
                val toCurrencyRate = entry.values.toList()[0].find {
                    it.currencyCode == toCurrencyCode
                }
                if (fromCurrencyRate == null || toCurrencyRate == null) {
                    return@forEachIndexed
                }
                if (fromCurrencyRate.baseCurrencyCode == toCurrencyRate.baseCurrencyCode) {
                    val convertedValue = toCurrencyRate.rate / fromCurrencyRate.rate
                    dates[index.toFloat()] = entry.keys.toList()[0]
                    entries.add(Entry(index.toFloat(), convertedValue.toFloat()))
                }
            }
            val dataSet = LineDataSet(entries, "Last ${if (tabPosition == 0) 30 else 90} days")
            dataSet.apply {
                setDrawCircles(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawValues(false)
                setDrawFilled(true)
                setDrawHighlightIndicators(false)
                lineWidth = 0f
                fillDrawable =
                    ResourcesCompat.getDrawable(resources, R.drawable.graph_filled_area_bg, theme)
            }
            val lineData = LineData(dataSet)
            lineData.setValueTextColor(Color.WHITE)
            lineData.setValueTextSize(16f)
            lineChart.data = lineData
            val xAxis = lineChart.xAxis
            xAxis.apply {
                granularity = 1f // minimum axis-step (interval) is 1
                valueFormatter = object: ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase): String {
                        return dates[value] ?: ""
                    }
                }
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textColor = Color.WHITE
                axisLineColor = Color.WHITE
                axisLineWidth = 0.05f
                setCenterAxisLabels(false)
            }

            lineChart.axisRight.isEnabled = false
            val yAxis = lineChart.axisLeft
            yAxis.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                axisLineWidth = 0.05f
                axisLineColor = Color.WHITE
            }
            lineChart.legend.isEnabled = false
            lineChart.description = Description().apply { text = "" }
            lineChart.marker = CustomMarker(this, R.layout.marker_view)
            lineChart.invalidate()
        }
    }

    private fun setListeners() {
        fromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val currencyRate = (parent?.getItemAtPosition(position) as CurrencyRate)
                fromSelectedIndex = position
                fromCurrencyLabel.text = currencyRate.currencyCode
                buildHistoricalRatesGraph(ratesTab.selectedTabPosition)
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
                buildHistoricalRatesGraph(ratesTab.selectedTabPosition)
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
        ratesTab.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (fromSelectedIndex >= 0 && toSelectedIndex >= 0){
                    buildHistoricalRatesGraph(tab!!.position)
                }

            }
        })
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

    inner class CustomSpinnerAdapter(private val context: Context, var countries: List<CurrencyRate>) : BaseAdapter(),
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
                .transform(CenterCrop(), CircleCrop())
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

    inner class CustomMarker(context: Context, layoutResource: Int) : MarkerView(context, layoutResource
    ){
        private var mOffset: MPPointF? = null
        override fun getOffset(): MPPointF {
            if(mOffset == null) {
               // center the marker horizontally and vertically
               mOffset = MPPointF(0f, -height.toFloat() + ExtraUtils.convertDpToPixel(8f))
            }
            return mOffset!!
        }

        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            date.text = dates[e?.x]
            rate.text = "1 ${currencyRates[fromSelectedIndex].currencyCode} = ${e?.y} ${currencyRates[toSelectedIndex].currencyCode}"
            super.refreshContent(e, highlight)
        }

    }
}
