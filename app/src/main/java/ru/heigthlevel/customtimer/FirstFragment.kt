package ru.heigthlevel.customtimer

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import ru.heigthlevel.customtimer.databinding.FragmentFirstBinding
import java.sql.Time

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.goFragment.setOnClickListener {
            if(binding.startButton.text == "Стоп") {
                binding.timerView.stop()
            }
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        binding.resetButton.setOnClickListener {
            binding.timerView.reset()
            binding.startButton.text = "Старт"
        }
        binding.startButton.text = "Старт"

        binding.startButton.setOnClickListener {
            if(binding.startButton.text == "Старт") {
                binding.timerView.start(0L)
                binding.startButton.text = "Стоп"
            } else {
                binding.timerView.stop()
                binding.startButton.text = "Старт"
            }
        }
        binding.timerView.addListener { timeState ->
            setTime(timeState)
        }
        setTime(binding.timerView.currentTime())
    }

    private fun setTime(timeState: TimeState) {
        val time = Time(timeState.time)
        binding.timeView.text = "${time.hours} : ${time.minutes} : ${time.seconds}"
    }
    //перегрузка функции

    private fun setTime(timeLong: Long) {
        val time = Time(timeLong)
        binding.timeView.text = "${time.hours} : ${time.minutes} : ${time.seconds}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}