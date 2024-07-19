package com.lelestacia.nutapostest2.ui.uang_masuk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.lelestacia.nutapostest2.R
import com.lelestacia.nutapostest2.databinding.FragmentUangMasukBinding
import com.lelestacia.nutapostest2.ui.adapter.TransactionGroupAdapter
import com.lelestacia.nutapostest2.ui.tambah_transaksi.TambahTransaksiFragment
import com.parassidhu.simpledate.toDateStandard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.navigation.koinNavGraphViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class UangMasukFragment : Fragment() {

    private var binding: FragmentUangMasukBinding? = null
    private val vm by koinNavGraphViewModel<UangMasukViewModel>(R.id.nav_graph)
    private val adapter = TransactionGroupAdapter(
        onDeleteTransaction = {
            vm.deleteTransaction(it)
        },
        onEditTransaction = {
            val bundle = Bundle()
            bundle.putParcelable(TambahTransaksiFragment.KEY, it)
            findNavController().navigate(R.id.action_UangMasukFragment_to_TambahTransaksiFragment, bundle)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUangMasukBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListener()

        binding?.rvTransaction?.adapter = adapter
        binding?.rvTransaction?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        viewLifecycleOwner.lifecycleScope.launch {
            vm.viewState.collectLatest { state ->

                binding?.tvSelectionDate?.text = getString(
                    R.string.tv_date_range,
                    Date(state.dateRange.first).toDateStandard(),
                    Date(state.dateRange.second).toDateStandard()
                )

                if (state.dateRange.first == 0L || state.dateRange.second == 0L) {
                    val start = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault())
                    val end = LocalDate.now().atTime(23, 59, 59).atZone(ZoneId.systemDefault())

                    vm.setDateRange(
                        kotlin.Pair(
                            start.toInstant().toEpochMilli(),
                            end.toInstant().toEpochMilli()
                        )
                    )
                }

                val groupedByDate = state.transactions
                    .filter { it.type == getString(R.string.tv_another_income) }
                    .groupBy {
                        val instant = Instant.ofEpochMilli(it.date)
                        instant.atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    .values
                    .toList()

                adapter.submitList(groupedByDate)
            }
        }
    }

    private fun setOnClickListener() {
        binding?.btnSelectDate?.setOnClickListener {
            val state = vm.viewState.value
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Pilih rentang tanggal transaksi")
                .setSelection(
                    Pair(
                        state.dateRange.first,
                        state.dateRange.second
                    )
                )
                .build()

            dateRangePicker.addOnPositiveButtonClickListener {
                val localStartDate = Instant.ofEpochMilli(it.first)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val startOfDay = localStartDate.atStartOfDay()

                val localEndDate = Instant.ofEpochMilli(it.second)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val endOfDay = localEndDate.atTime(23, 59, 59)

                vm.setDateRange(
                    kotlin.Pair(
                        Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant())
                            .toInstant()
                            .toEpochMilli(),

                        Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant())
                            .toInstant()
                            .toEpochMilli()
                    )
                )
            }

            dateRangePicker.show(requireActivity().supportFragmentManager, null)
        }

        binding?.btnAddTransaction?.setOnClickListener {
            findNavController().navigate(R.id.action_UangMasukFragment_to_TambahTransaksiFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}