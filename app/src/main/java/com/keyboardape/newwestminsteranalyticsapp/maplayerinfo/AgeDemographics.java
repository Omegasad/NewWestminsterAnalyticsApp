package com.keyboardape.newwestminsteranalyticsapp.maplayerinfo;

import android.util.Pair;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AgeDemographics data object.
 */
public class AgeDemographics {

    private Map<Integer, Map<Integer, Long>>                   mAgeGroups;
    private Map<Integer, Map<Integer, Pair<Integer, Integer>>> mPopulation;
    private Map<Integer, Long>                                 mTotalMale;
    private Map<Integer, Long>                                 mTotalFemale;

    public AgeDemographics() {
        mAgeGroups   = new LinkedHashMap<>();
        mPopulation  = new LinkedHashMap<>();
        mTotalMale   = new HashMap<>();
        mTotalFemale = new HashMap<>();
    }

    public Long getTotalMale(int year) {
        return mTotalMale.get(year);
    }

    public Long getTotalFemale(int year) {
        return mTotalFemale.get(year);
    }

    public Map<Integer, Pair<Integer, Integer>> getDemographics(int year) {
        return mPopulation.get(year);
    }

    public Map<Integer, Long> getAgeGroups(int year) {
        return mAgeGroups.get(year);
    }

    public void addPopulation(int year, int ageFrom, int malePopulation, int femalePopulation) {

        // Update total male count
        Long totalMale = mTotalMale.get(year);
        mTotalMale.put(year, (totalMale != null) ? malePopulation + totalMale : malePopulation);

        // Update total female count
        Long totalFemale = mTotalFemale.get(year);
        mTotalMale.put(year, (totalFemale != null) ? femalePopulation + totalFemale : femalePopulation);

        // Update age groups count
        Map<Integer, Long> ageGroups = getAgeGroups(year);
        if (ageGroups == null) {
            ageGroups = new LinkedHashMap<>();
            mAgeGroups.put(year, ageGroups);
        }
        int ageGroupNum;
        if (ageFrom < 15) {
            ageGroupNum = 0; // Children
        } else if (ageFrom < 25) {
            ageGroupNum = 1; // Youth
        } else if (ageFrom < 65) {
            ageGroupNum = 2; // Adults
        } else {
            ageGroupNum = 3; // Seniors
        }
        Long ageGroupCount = ageGroups.get(ageGroupNum);
        ageGroups.put(ageGroupNum, (ageGroupCount != null)
                ? malePopulation + femalePopulation + ageGroupCount
                : malePopulation + femalePopulation);

        // New map if demographics for year not created
        Map<Integer, Pair<Integer, Integer>> demographics = getDemographics(year);
        if (demographics == null) {
            demographics = new LinkedHashMap<>();
            mPopulation.put(year, demographics);
        }
        demographics.put(ageFrom, new Pair<Integer, Integer>(malePopulation, femalePopulation));

//        // Sort age groups into decades
//        if (ageFrom != 0) {
//            ageFrom = (ageFrom / 10) * 10;
//        }
//
//        // Add to existing population data if in same age group
//        Pair<Integer, Integer> oldPopulation = demographics.get(ageFrom);
//        if (oldPopulation != null) {
//            malePopulation += oldPopulation.first;
//            femalePopulation += oldPopulation.second;
//        }
    }
}