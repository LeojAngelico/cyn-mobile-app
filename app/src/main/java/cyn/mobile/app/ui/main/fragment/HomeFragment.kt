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
import cyn.mobile.app.data.model.RecentVerificationItem
import cyn.mobile.app.databinding.FragmentHomeBinding
import cyn.mobile.app.ui.main.activity.MainActivity
import cyn.mobile.app.ui.main.adapter.RecentVerificationAdapter
import cyn.mobile.app.ui.main.viewmodel.DashboardViewModel
import cyn.mobile.app.ui.main.viewmodel.DashboardViewState
import cyn.mobile.app.utils.dialog.CommonDialog
import cyn.mobile.app.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private val activity get() = requireActivity() as MainActivity

    private lateinit var adapter: RecentVerificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeDashboard()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getDashboardInfo()
            viewModel.getLatestTransaction()
        }

        setupRecyclerView()
        setClickListeners()

    }

    override fun onResume() {
        super.onResume()
        viewModel.getDashboardInfo()
        viewModel.getLatestTransaction()
    }

    private fun setClickListeners(){
        binding.testTransactionCard.setOnSingleClickListener {
            activity.navigateToTest()
        }
        binding.transactionHistoryCard.setOnSingleClickListener {
            activity.navigateToHistory()
        }
        binding.viewAllTextView.setOnSingleClickListener {
            activity.navigateToHistory()
        }

    }

    private fun setupRecyclerView(){
        adapter = RecentVerificationAdapter()
        binding.recentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recentRecyclerView.adapter = adapter
        binding.recentRecyclerView.isNestedScrollingEnabled = false

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recentRecyclerView.addItemDecoration(divider)
    }

    private fun observeDashboard(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleViewState(state)
                }
            }
        }
    }

    private fun handleViewState(viewState: DashboardViewState){
            when (viewState) {
                is DashboardViewState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                is DashboardViewState.DashboardLoaded -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    val data = viewState.data
                    val totalTransaction = (data?.sucess_transactions?.toIntOrNull() ?: 0) +
                            (data?.failed_transactions?.toIntOrNull() ?: 0)
                    binding.totalValueTextView.text = totalTransaction.toString()
                    binding.successValueTextView.text = data?.sucess_transactions.orEmpty()
                    binding.failedValueTextView.text = data?.failed_transactions.orEmpty()
                }
                is DashboardViewState.LatestTransactionLoaded -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    val data = viewState.data
                    val items = listOf(
                        RecentVerificationItem("Phone Verification", data?.phone_number.orEmpty(), data?.auth_code.orEmpty()),
                        RecentVerificationItem("SMS Authentication", data?.phone_number.orEmpty(), data?.token_status.orEmpty()),
                        RecentVerificationItem("Token Generation", data?.phone_number.orEmpty(), data?.number_verification.orEmpty())
                    )
                    adapter.replaceAll(items)
                }
                is DashboardViewState.PopupError -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    CommonDialog.openDialog(childFragmentManager, true, viewState.message)
                }
                else -> Unit
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
