package jp.co.yahoo.appfeedback.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by taicsuzu on 2016/10/03.
 */

public class ThirdActivity extends AppCompatActivity{

    TabFragment[] fragments = new TabFragment[4];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        ViewPager viewPager = (ViewPager)findViewById(R.id.third_view_pager);
        viewPager.setAdapter(new SampleAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout)findViewById(R.id.third_tab);
        tabLayout.setupWithViewPager(viewPager);

    }

    class SampleAdapter extends FragmentStatePagerAdapter{

        public SampleAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position] == null ? (fragments[position] = new TabFragment().setPosition(position)) : fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page: "+position;
        }
    }
}
