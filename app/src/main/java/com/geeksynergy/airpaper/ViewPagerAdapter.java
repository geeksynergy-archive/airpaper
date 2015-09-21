package com.geeksynergy.airpaper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    public CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    public int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumberOfTabs) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumberOfTabs;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        if (position == 0) // if the position is 0 we are returning the First tab
        {
            return new Latest();
        }
        if (position == 1) // if the position is 0 we are returning the First tab
        {
            return new Agriculture();
        }
        if (position == 2)             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
        {
            return new HealthCare();
        }
        if (position == 3) {
            return new Technology();
        }
        if (position == 4)             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
        {
            return new WeatherForeCast();
        }
        if (position == 5) {
            return new Sports();
        }
        if (position == 6) {
            return new Entertainment();
        } else {
            return new Business();
        }

    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        // This method return the Number of tabs for the tabs Strip
        return Titles[position];
    }

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}