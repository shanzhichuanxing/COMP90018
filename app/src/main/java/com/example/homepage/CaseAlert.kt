package com.example.homepage

import android.content.Context
import com.example.homepage.model.Case
import android.util.Log
import com.example.homepage.model.Place
import com.example.homepage.utils.addressToLocation
import java.io.IOException
import java.net.URL
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CaseAlert {
    private var cases: ArrayList<Case>? = null

    fun getPlaces(context: Context): ArrayList<Place>? {
        var places = ArrayList<Place>()
        cases = getCases()

        cases?.forEach{ case ->
            var alert_level = 0
            when (case.adviceTitle) {
                "Tier1" -> { alert_level = 1 }
                "Tier2" -> { alert_level = 2 }
                "Tier3" -> { alert_level = 3 }
            }
            places.add(
                Place(
                    case.siteTitle,
                    addressToLocation.getLocationFromAddress(context, case.siteTitle + ", " + case.siteStreet),
                    case.siteStreet + ", " + case.siteState + " " + case.sitePostcode,
                    alert_level
                )
            )
        }
        return places
    }

    fun getCases(): ArrayList<Case>? {

        if (cases == null) {
            initializeCasesAddress()
        }

        return cases
    }

    private fun initializeCasesAddress() {

        var a = fetchData();
        // use scheduled Executed service to reduce sleeping tasks
        val scheduler = Executors.newScheduledThreadPool(1)
        scheduler.scheduleAtFixedRate(fetchData(), 0, 1, TimeUnit.DAYS)
        if (cases == null) {
            Log.i(TAG, "fetchData start")
            a.start()
        }

        try {
            a.join()
        } catch (e: Exception) {
            e.stackTrace
        }
        Log.i(TAG, "fetchData end: " + cases!!.size);

    }

    internal inner class fetchData : Thread() {
        override fun run() {
            try {
                cases = ArrayList<Case>();
                val request =
                    "https://drive.google.com/uc?export=download&id=1hULHQeuuMQwndvKy1_ScqObgX0NRUv1A"
                val tier1 =
                    "Anyone who has visited this location during these times must get tested immediately and quarantine for 14 days from the exposure."
                val tier2 =
                    "\"Anyone who has visited this location during these times should urgently get tested, then isolate until confirmation of a negative result. Continue to monitor for symptoms, get tested again if symptoms appear.\""
                val tier3 =
                    "\"Anyone who has visited this location during  these times should monitor for symptoms - If symptoms develop, immediately get tested and isolate until you receive a negative result.\""
                var header = 0
                var i: Int

                val text = URL(request).readText();
                val lines = text.split('\n')
                lines.forEach {
                    var line = it;


                    if (header == 0) {
                        header = 1
                    } else {
                        line = line.replace(tier1, "tier1").replace(tier2, "tier2")
                            .replace(tier3, "tier3")
                        var parts = line.split(",").toTypedArray()

                        // pre-process the case data
                        if (parts[2].contains("\"")) {
                            parts[2] = parts[2] + ", " + parts[3]
                            i = 3
                            while (i < parts.size - 1) {
                                parts[i] = parts[i + 1]
                                i++
                            }
                        }

                        if (parts[13] == "tier1") {
                            parts[12] = "Tier1"
                            parts[13] = tier1
                        } else if (parts[13] == "tier2") {
                            parts[12] = "Tier2"
                            parts[13] = tier2
                        } else {
                            parts[12] = "Tier3"
                            parts[13] = tier3
                        }

                        var c = Case(
                            parts[0],
                            parts[1].replace(" ", ""),
                            parts[2],
                            parts[3],
                            parts[4],
                            parts[5],
                            parts[7],
                            parts[8],
                            parts[9],
                            parts[11],
                            parts[12],
                            parts[13],
                            parts[14],
                            parts[15],
                            parts[16],
                        );
                        cases!!.add(c)
                    }
                }

                if (cases!!.size == 0) {
                    Log.e(TAG, "Failed to retrieve case information")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val TAG = "CaseAlert"
    }
}