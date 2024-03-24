package com.example.studienarbeitfoxylibrary.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.studienarbeitfoxylibrary.R
import com.example.studienarbeitfoxylibrary.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null

    // RecyclerView:
    private lateinit var rv: RecyclerView
    private lateinit var adapter: BookListAdapter

    private lateinit var homeViewModel: HomeViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        homeViewModel = ViewModelProvider(requireActivity(), HomeViewModelFactory(requireActivity().application)).get(HomeViewModel::class.java)
        homeViewModel.getLiveDataBooks().observe(viewLifecycleOwner, Observer{items ->
            adapter.updateContent(ArrayList(items))
        })
    }

    private fun initRecyclerView()
    {
        rv = binding.root.findViewById(R.id.main_rv)

        adapter = BookListAdapter(ArrayList())
        rv.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}