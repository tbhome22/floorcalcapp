package com.example.floorboardcalculator.ui.mainpg;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.process.ControlActivity;
import com.example.floorboardcalculator.ui.addon.InternetStatus;
import com.example.floorboardcalculator.ui.mainpg.addFragment.IWizardListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AddNewActivity extends ControlActivity implements IWizardListener {
    private static final String TAG = AddNewActivity.class.getSimpleName();
    private ViewPager2 pager;
    private TabLayout tabLayout;
    private TextView mPageTitle;

    private WizardPageAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpage);

        pager = (ViewPager2) findViewById(R.id.add_viewPager);
        tabLayout = (TabLayout) findViewById(R.id.add_pagerTab);
        mPageTitle = (TextView) findViewById(R.id.add_titleText);

        adapter = new WizardPageAdapter(this, this);

        pager.setAdapter(adapter);
        pager.setPageTransformer(new ZoomAnimation());
        adapter.setListener(this);

        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        new TabLayoutMediator(tabLayout, pager,
                (tab, position) -> {
                    tab.setText(WizardViewModel.values()[position].getTitleResId());
                    tab.view.setEnabled(false);

                }).attach();

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                mPageTitle.setText(WizardViewModel.values()[position].getDescResId());

                if(position == adapter.getItemCount() - 1) {
                    adapter.verifyData();
                }
            }
        });
    }

    @Override
    public void onStateUpdated(@LayoutRes int layoutId, boolean isValid) {
        switch (layoutId) {
            case R.layout.fragment_addpage_01:
                tabLayout.getTabAt(0).setIcon(isValid? R.drawable.ic_add_success : R.drawable.ic_block_red);
                tabLayout.getTabAt(0).getIcon().setColorFilter(isValid? Color.GREEN : Color.RED, PorterDuff.Mode.SRC_IN);
                break;

            case R.layout.fragment_addpage_02:
                tabLayout.getTabAt(1).setIcon(isValid? R.drawable.ic_add_success : R.drawable.ic_block_red);
                tabLayout.getTabAt(1).getIcon().setColorFilter(isValid? Color.GREEN : Color.RED, PorterDuff.Mode.SRC_IN);
                break;

            case R.layout.fragment_addpage_03:
                tabLayout.getTabAt(2).setIcon(isValid? R.drawable.ic_add_success : R.drawable.ic_block_red);
                tabLayout.getTabAt(2).getIcon().setColorFilter(isValid? Color.GREEN : Color.RED, PorterDuff.Mode.SRC_IN);
                break;

            case R.layout.fragment_addpage_04:
                tabLayout.getTabAt(3).setIcon(isValid? R.drawable.ic_add_success : R.drawable.ic_block_red);
                tabLayout.getTabAt(3).getIcon().setColorFilter(isValid? Color.GREEN : Color.RED, PorterDuff.Mode.SRC_IN);
                break;

            case R.layout.fragment_addpage_05:
                tabLayout.getTabAt(5).setIcon(isValid? R.drawable.ic_add_success : R.drawable.ic_block_red);
                tabLayout.getTabAt(5).getIcon().setColorFilter(isValid? Color.GREEN : Color.RED, PorterDuff.Mode.SRC_IN);
                break;
        }
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(pager.getCurrentItem() > 0) {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cancel?");
            builder.setMessage("Cancel adding customer? Record will be deleted once cancelled");

            builder.setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                dialog.dismiss();
                super.onBackPressed();
            });

            builder.setNegativeButton(R.string.btn_no, (dialog, which) -> {
                dialog.cancel();
            });

            builder.show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        this.finish();
    }

    @Nullable
    @Override
    public Menu getMenu() {
        return null;
    }

    @Override
    public void tickConnectionStatus(InternetStatus status, int counter) {
        super.tickConnectionStatus(status, counter);
    }
}
