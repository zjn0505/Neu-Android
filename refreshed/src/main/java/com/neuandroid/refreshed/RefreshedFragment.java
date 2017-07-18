package com.neuandroid.refreshed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jienanzhang on 17/07/2017.
 */

public class RefreshedFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refreshed, container, false);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setVisibility(View.VISIBLE);
        return view;


    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        for (String source : SourcesList.sources) {
            NewsListFragment fragment = new NewsListFragment();
            fragment.setSource(source);
            adapter.addFragment(fragment, source);
        }
        viewPager.setAdapter(adapter);
    }

    private class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
