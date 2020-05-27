package dev.ganainy.reminderly.ui.searchFragment

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footy.database.ReminderDatabase
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.databinding.SearchFragmentBinding
import dev.ganainy.reminderly.ui.baseFragment.BaseFragment
import dev.ganainy.reminderly.ui.baseFragment.ProvideDatabaseViewModelFactory
import dev.ganainy.reminderly.utils.MyUtils
import io.reactivex.disposables.CompositeDisposable

class SearchFragment : BaseFragment() {
    private lateinit var binding: SearchFragmentBinding
    private lateinit var viewModelFactory:ProvideDatabaseViewModelFactory
    private val disposable= CompositeDisposable()

    companion object {
        fun newInstance() = SearchFragment()
    }

    private lateinit var viewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.search_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        initAdapter()
        initRecycler()
        initViewModel()

        /**show keyboard to make search more convenient*/
        binding.searchEditText.requestFocus()
        MyUtils.showKeyboard(requireContext())

        binding.searchEditText.afterTextChanged {
            viewModel.searchWithQuery(it)
        }

        binding.backButton.setOnClickListener {
            MyUtils.hideKeyboard(requireContext(),binding.searchEditText)
            requireActivity().onBackPressed()
        }


        disposable.add(viewModel.emptyListSubject.subscribe {isFilteredListEmpty->
            if (isFilteredListEmpty)showEmptyUi()
            else hideEmptyUi()
        })

        disposable.add(viewModel.filteredListSubject.subscribe {filteredReminderList->
            adapter.submitList(filteredReminderList)
            adapter.notifyDataSetChanged()
        })

        disposable.add(viewModel.toastSubject.subscribe {stringResourceId->
            MyUtils.showCustomToast(requireContext(),stringResourceId)
        })




    }

    private fun hideEmptyUi() {
        binding.noRemindersGroup.visibility = View.GONE
        binding.reminderReycler.visibility = View.VISIBLE
    }

    private fun showEmptyUi() {
        binding.noRemindersGroup.visibility = View.VISIBLE
        binding.reminderReycler.visibility = View.GONE
    }

    private fun initViewModel() {
        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao


        viewModelFactory=
            ProvideDatabaseViewModelFactory(
                requireActivity().application,
                reminderDatabaseDao
            )
        viewModel = ViewModelProvider(this, viewModelFactory).get(SearchViewModel::class.java)
    }





    private fun initRecycler() {



        binding.reminderReycler.setHasFixedSize(true)
        binding.reminderReycler.adapter = adapter
        //Change layout manager depending on orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val gridLayoutManager = GridLayoutManager(requireActivity(), 2)
            //change span size of headers so header shows in row
            gridLayoutManager.spanSizeLookup = (object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (adapter.getItemViewType(position)) {
                        0 -> 1
                        else -> 2
                    }
                }

            })

            binding.reminderReycler.layoutManager = gridLayoutManager
        } else {
            binding.reminderReycler.layoutManager = LinearLayoutManager(requireContext())
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

}

/**extention function to simplify listening to edittext changes*/
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

