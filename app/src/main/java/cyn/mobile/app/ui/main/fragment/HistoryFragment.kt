package cyn.mobile.app.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.datepicker.MaterialDatePicker
import android.content.ClipData
import android.content.ClipboardManager
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.Toast
import cyn.mobile.app.data.model.TransactionItem
import cyn.mobile.app.databinding.FragmentHistoryBinding
import cyn.mobile.app.ui.main.adapter.TransactionHistoryAdapter
import cyn.mobile.app.ui.main.viewmodel.TransactionViewModel
import cyn.mobile.app.ui.main.viewmodel.TransactionViewState
import cyn.mobile.app.utils.dialog.CommonDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()

    private lateinit var adapter: TransactionHistoryAdapter

    // Filters and paging
    private var currentPerPage = 5
    private var currentKeyword: String = ""
    private var startDate: String = ""
    private var endDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setClickListeners()
        observeTransactions()
        setupSwipeRefreshTweaks()

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchTransactions()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        fetchTransactions()
    }

    private fun setupSwipeRefreshTweaks() {
        // 1) Require a longer pull distance before triggering refresh (less sensitive)
        binding.swipeRefreshLayout.setDistanceToTriggerSync(dpToPx(160f)) // ~160dp

        // 2) Only allow refresh when RecyclerView is scrolled to top
        binding.swipeRefreshLayout.setOnChildScrollUpCallback { _, _ ->
            binding.transactionRecyclerView.canScrollVertically(-1)
        }

        // 3) If a horizontal gesture is detected on the list area, don’t let SwipeRefresh intercept
        attachDisallowInterceptOnHorizontal(binding.transactionRecyclerView)

        // If you have any horizontal-scrollable views in the same screen, call this for them too:
        // attachDisallowInterceptOnHorizontal(binding.someHorizontalScrollView)
        // attachDisallowInterceptOnHorizontal(binding.someHorizontalRecyclerView)
    }

    private fun dpToPx(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun attachDisallowInterceptOnHorizontal(target: View) {
        var startX = 0f
        var startY = 0f
        val touchSlop = ViewConfiguration.get(target.context).scaledTouchSlop

        target.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    // Allow parent to intercept until we know it’s a horizontal gesture
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = Math.abs(event.x - startX)
                    val dy = Math.abs(event.y - startY)
                    // If horizontal movement dominates and exceeds slop, don’t let SwipeRefresh intercept
                    if (dx > dy && dx > touchSlop) {
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }


    private fun setupRecyclerView() {
        adapter = TransactionHistoryAdapter(emptyList(), ::copyTransactionIdToClipboard)
        binding.transactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionRecyclerView.adapter = adapter
        binding.transactionRecyclerView.isNestedScrollingEnabled = false

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.transactionRecyclerView.addItemDecoration(divider)
    }

    private fun setClickListeners() {
        // Calendar icon -> open date range picker
        binding.calendarIconImageView.setOnClickListener {
            showDateRangePicker()
        }

        // Trigger search when user confirms action (IME action “search” or “done”)
        binding.searchEditText.setOnEditorActionListener { v, _, _ ->
            currentKeyword = v.text?.toString().orEmpty()
            // Reset page size when starting a new search
            currentPerPage = 5
            fetchTransactions()
            true
        }

        // Load more grows page size and reload
        binding.loadMoreButton.setOnClickListener {
            binding.loadMoreButton.isEnabled = false
            currentPerPage += 5
            fetchTransactions()
        }
    }

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleViewState(state)
                }
            }
        }
    }

    private fun handleViewState(state: TransactionViewState) {
        when (state) {
            is TransactionViewState.Loading -> {
                // Provide quick feedback through the “Load more” button
                binding.swipeRefreshLayout.isRefreshing = true
                binding.loadMoreButton.isEnabled = false
                binding.loadMoreButton.text = getString(cyn.mobile.app.R.string.loading)
            }
            is TransactionViewState.TransactionListLoaded -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.loadMoreButton.text = getString(cyn.mobile.app.R.string.action_load_more_transactions)
                binding.loadMoreButton.isEnabled = state.hasMorePage == true

                // Recreate adapter with latest items to avoid relying on adapter-specific mutation APIs
                val items = state.items ?: emptyList()
                adapter = TransactionHistoryAdapter(items, ::copyTransactionIdToClipboard)
                binding.transactionRecyclerView.adapter = adapter
            }
            is TransactionViewState.PopupError -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.loadMoreButton.text = getString(cyn.mobile.app.R.string.action_load_more_transactions)
                binding.loadMoreButton.isEnabled = true
                CommonDialog.openDialog(childFragmentManager, true, state.message)
            }
            else -> Unit
        }
    }

    private fun fetchTransactions() {
        viewModel.getTransactionList(
            perPage = currentPerPage.toString(),
            keyword = currentKeyword,
            startDate = startDate,
            endDate = endDate
        )
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select date range")
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val startUtc = selection.first
            val endUtc = selection.second
            onDateRangeSelected(startUtc, endUtc)
        }

        picker.show(parentFragmentManager, "date_range_picker")
    }

    private fun onDateRangeSelected(startUtcMillis: Long?, endUtcMillis: Long?) {
        // Convert to backend-expected format (e.g., yyyy-MM-dd). Adjust if API expects different shape.
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        startDate = startUtcMillis?.let { fmt.format(it) }.orEmpty()
        endDate = endUtcMillis?.let { fmt.format(it) }.orEmpty()

        // Reset paging on filter change
        currentPerPage = 5
        fetchTransactions()
    }

    private fun copyTransactionIdToClipboard(transactionId: String) {
        val ctx = requireContext()
        val clipboard = ctx.getSystemService(ClipboardManager::class.java)
        val clip = ClipData.newPlainText("Transaction ID", transactionId)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(ctx, getString(cyn.mobile.app.R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
